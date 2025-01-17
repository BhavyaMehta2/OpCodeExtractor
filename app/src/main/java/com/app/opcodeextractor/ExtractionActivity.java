package com.app.opcodeextractor;

import static com.app.opcodeextractor.Unzipper.unzip;
import static com.app.opcodeextractor.decoder.OPcode.getDalvikOpCodes;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.opcodeextractor.decoder.BaksmaliAdapter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExtractionActivity extends AppCompatActivity {
    private static final String TAG = "ExtractionActivity";
    static FrameLayout pBar;
    RecyclerView recyclerView;
    ModelRVAdapter adapter;
    String ApplicationPackageName;

    Map<String, String> dalvikOpCodes;
    static ConcurrentMap<String, Integer> opcodeMap;

    long start;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extraction);

        pBar = findViewById(R.id.progress_overlay);

        recyclerView = findViewById(R.id.rv_models);
        GridLayoutManager recyclerViewLayoutManager = new GridLayoutManager(ExtractionActivity.this, 1);
        recyclerView.setLayoutManager(recyclerViewLayoutManager);
        adapter = new ModelRVAdapter(ExtractionActivity.this, new ModelSelector().GetAllModels());
        recyclerView.setAdapter(adapter);

        this.dalvikOpCodes = new HashMap<>();
        opcodeMap = new ConcurrentHashMap<>();
        this.dalvikOpCodes.putAll(getDalvikOpCodes());

        File apkFile = (File) getIntent().getExtras().get("apk");
        ApplicationPackageName = (String) getIntent().getExtras().get("package");

        try {
            clearPrevFiles(ApplicationPackageName, apkFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void clearPrevFiles(String apkPackage, File apkFile) throws IOException {
        pBar.setVisibility(View.VISIBLE);
        ExecutorService executor = Executors.newFixedThreadPool(4);
        executor.execute(() -> {
            File extractDir = new File(getCacheDir(), apkPackage);
            new File(extractDir, "smali").mkdirs();
            new File(extractDir, "dex").mkdirs();
            File currDir;
            try {
                currDir = unzip(this, apkFile, apkPackage);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            decode(currDir, apkPackage);
        });
        executor.shutdown();
    }

    void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : Objects.requireNonNull(fileOrDirectory.listFiles()))
                deleteRecursive(child);
        fileOrDirectory.delete();
    }

    public void decode(File currDir, String apkPackage) {
        start = System.currentTimeMillis();
        ExecutorService executor = Executors.newFixedThreadPool(4);
        executor.execute(() -> {
            for (File dexDocFile : Objects.requireNonNull(currDir.listFiles())) {
                try {
                    File outputFile = new File(this.getCacheDir(), apkPackage + "/smali");
                    outputFile.deleteOnExit();
                    long start1 = System.currentTimeMillis();
                    BaksmaliAdapter.doBaksmali(dexDocFile.getAbsolutePath(), outputFile.getPath());
                    Log.e("BAKSMALI", (System.currentTimeMillis() - start1) / 1000.0 + ":" + dexDocFile.getName());
                } catch (Exception e) {
                    Log.e(TAG, "Exception occurred while processing file" + e);
                    e.printStackTrace();
                }
            }
            startOpcodeSeq(apkPackage);
//            runOnUiThread(() -> pBar.setVisibility(View.GONE));
        });
        executor.shutdown();
    }

    public void startOpcodeSeq(String apkPackage) {
        CompletableFuture.runAsync(() ->
                {
                    ExecutorService executor = Executors.newFixedThreadPool(4);
                    Handler handler = new Handler(Looper.getMainLooper());
                    executor.execute(() -> {
                        createOpcodeSeq(apkPackage);
                        handler.post(() -> {
                            pBar.setVisibility(View.GONE);
                            Log.e("COMPLETED", "It took " + (System.currentTimeMillis() - start) / 1000.0 + " seconds");
                        });
                    });
                    executor.shutdown();
                }
        );
    }

    public void createOpcodeSeq(String apkPackage) {
        try {
            File smaliDir = new File(getCacheDir() + "/" + apkPackage, "smali/"+apkPackage.replace(".","/"));

            ExecutorService executor = Executors.newFixedThreadPool(4);

            if (!smaliDir.exists()) {
                Log.e(TAG, "smali directory does not exist" + smaliDir.getAbsolutePath());
                return;
            }

            try (Stream<Path> smaliFiles = Files.walk(smaliDir.toPath())) {
                List<Path> files = smaliFiles.filter(p -> p.toFile().isFile())
                        .collect(Collectors.toList());

                List<Callable<Map<String, Integer>>> tasks = new ArrayList<>();
                for (Path smaliFile : files) {
                    tasks.add(() -> getOpCodeSeq(smaliFile, dalvikOpCodes));
                }

                List<Future<Map<String, Integer>>> results = executor.invokeAll(tasks);
                for (Future<Map<String, Integer>> result : results) {
                    Map<String, Integer> opcodeCountMap = result.get();
                    for (Map.Entry<String, Integer> entry : opcodeCountMap.entrySet()) {
                        String opcode = entry.getKey();
                        int count = entry.getValue();
                        opcodeMap.merge(opcode, count, Integer::sum);
                    }
                }
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } finally {
                Log.e("OPCODE", "Finished");
                executor.shutdown();
            }
        } catch (IOException | InterruptedException e) {
            Log.e(TAG, "Exception occurred during opseq creation: " + e.getMessage());
        } finally {
            File extractDir = new File(getCacheDir(), apkPackage);
            System.out.println(opcodeMap);
            Log.e(TAG, "Deleting");
            deleteRecursive(extractDir);
        }
    }

    private static Map<String, Integer> getOpCodeSeq(Path smaliFile, Map<String, String> dalvikOpCodes) {
        StringBuilder opCodeSeq = new StringBuilder();
        try (Stream<String> lines = Files.lines(smaliFile)) {
            boolean methodStarted = false;
            for (String line : (Iterable<String>) lines::iterator) {
                if (line.startsWith(".method")) {
                    methodStarted = true;
                } else if (line.startsWith(".end method")) {
                    methodStarted = false;
                } else if (methodStarted && !line.startsWith(".") && !line.startsWith("#") && !line.trim().isEmpty()) {
                    String[] methodLine = line.trim().split("\\s+");
                    if (dalvikOpCodes.containsKey(methodLine[0])) {
                        opCodeSeq.append(dalvikOpCodes.get(methodLine[0]));
                        opCodeSeq.append("\n");
                    }
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Exception occurred while reading smali file: " + e);
        }
        String[] dic = opCodeSeq.toString().split("\\W+");
        HashMap<String, Integer> map = new HashMap<>();

        for (String word : dic) {
            if (!map.containsKey(String.valueOf(word))) {
                if(!Objects.equals(word, ""))
                    map.put(word, 1);
            } else {
                Integer countObj = map.get(String.valueOf(word));
                int count = (countObj != null) ? countObj : 0;
                map.put(String.valueOf(word), count + 1);
            }
        }

        return map;
    }
}