package com.app.opcodeextractor;

import static com.app.opcodeextractor.Unzipper.unzip;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.app.opcodeextractor.decoder.BaksmaliAdapter;
import com.app.opcodeextractor.decoder.OPcode;

import org.xml.sax.SAXException;

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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.parsers.ParserConfigurationException;

import jakarta.xml.bind.JAXBException;

public class Extractor {

    private static final String TAG = "OpCodeExtractor";
    Context context1;
    Map<String, String> dalvikOpCodes;
    Map<String, Integer> opcodeMap;

    public Extractor(Context context2) {
        context1 = context2;
        this.dalvikOpCodes = new HashMap<>();
        this.opcodeMap = new HashMap<>();
        OPcode op = new OPcode(context1);
        this.dalvikOpCodes.putAll(op.getDalvikOpCodes());
    }

    public void main(String apkPackage, File apkFile) {
        long start = System.currentTimeMillis();
        CompletableFuture<Void> decodeApplicationFuture =  CompletableFuture.runAsync(() -> {
                    try {
                        decodeApplication(apkPackage, apkFile);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
        CompletableFuture<Void> extractApplicationFuture = decodeApplicationFuture.thenCompose((Void) -> CompletableFuture.runAsync(() ->
                {
                    ExecutorService executor = Executors.newFixedThreadPool(4);
                    Handler handler = new Handler(Looper.getMainLooper());
                    executor.execute(() -> {
                        createOpcodeSeq(apkPackage);
                        System.out.println(opcodeMap);
                        Log.e("Evaluation", "Starting");
                        Model modelEvaluation = new Model(context1);
                        try {
                            modelEvaluation.main(opcodeMap);
                        } catch (IOException | JAXBException | ParserConfigurationException |
                                 SAXException e) {
                            throw new RuntimeException(e);
                        }
                        Log.e("Evaluation", "Ending");

                        handler.post(() -> {
                            MainActivity.pText.setVisibility(View.GONE);
                            MainActivity.pBar.setVisibility(View.GONE);
                            Toast.makeText(context1, "It took "+(System.currentTimeMillis()-start)/1000.0+" seconds", Toast.LENGTH_LONG).show();
                            Log.e("COMPLETED", "It took "+(System.currentTimeMillis()-start)/1000.0+" seconds");
                        });
                    });

                    executor.shutdown();
                }
        ));
    }

    public void clearPrevFiles(String apkPackage) {
        File extractDir = new File(context1.getCacheDir(), apkPackage);
        deleteRecursive(extractDir);
    }

    void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : Objects.requireNonNull(fileOrDirectory.listFiles()))
                deleteRecursive(child);
        fileOrDirectory.deleteOnExit();
    }

    public void decodeApplication(String apkPackage, File apkFile) throws IOException {
        File currDir = unzip(context1, apkFile, apkPackage);
        decode(currDir, apkPackage);
    }

    public void decode(File currDir, String apkPackage) {
        ExecutorService executor = Executors.newFixedThreadPool(4);

        executor.execute(() -> {
            for (File dexDocFile : Objects.requireNonNull(currDir.listFiles())) {
                try {
                    File outputFile = new File(context1.getCacheDir(), apkPackage + "/smali");
                    outputFile.deleteOnExit();
                    long start = System.currentTimeMillis();
                    BaksmaliAdapter.doBaksmali(dexDocFile.getAbsolutePath(), outputFile.getPath());
                    Log.e("BAKSMALI", (System.currentTimeMillis() - start) / 1000.0 + ":" + dexDocFile.getName());
                } catch (Exception e) {
                    Log.e(TAG, "Exception occurred while processing file" + e);
                    e.printStackTrace();
                }
            }
        });
        executor.shutdown();
    }

    public void createOpcodeSeq(String apkPackage) {
        try {
            File smaliDir = new File(context1.getCacheDir() + "/" + apkPackage, "smali");
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
            if (!map.containsKey(word)) {
                if(!Objects.equals(word, ""))
                     map.put(word, 1);
            } else {
                int count = map.get(word);
                map.put(word, count + 1);
            }
        }
        return map;
    }
}
