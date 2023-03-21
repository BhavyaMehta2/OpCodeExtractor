package com.app.opcodeextractor;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import androidx.documentfile.provider.DocumentFile;

import com.app.opcodeextractor.decoder.BaksmaliAdapter;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class OpCodeExtractor {
    private static final String TAG = "OpCodeExtractor";
    Context context1;
    private final String folder;

    public OpCodeExtractor(Context context2) {
        context1 = context2;
        SharedPreferences preferences = context1.getSharedPreferences("com.app.opcodeextractor", Context.MODE_PRIVATE);
        folder = preferences.getString("filestorageuri", "null");
    }

    public void main(String apkPackage, File apkFile) throws IOException {
        clearPrevFiles(apkPackage);
        decodeApplication(apkPackage, apkFile);
//        Boolean result = createOpcodeSeq(apkPackage);
    }

    public void clearPrevFiles(String apkPackage) {
        DocumentFile extractDir = DocumentFile.fromTreeUri(context1, Uri.parse(folder));
        assert extractDir != null;
        Objects.requireNonNull(extractDir.findFile(apkPackage)).delete();
    }

    public void decodeApplication(String apkPackage, File apkFile) throws IOException {
        DocumentFile currDir = unzip(apkFile, apkPackage);
        decode(currDir, apkPackage);
    }

    public void decode(DocumentFile currDir, String apkPackage) throws IOException {
//        DocumentFile outputDocFile = Objects.requireNonNull(currDir.getParentFile()).createDirectory(apkPackage);
        DocumentFile dexDocFile = currDir.listFiles()[0];

//        assert outputDocFile != null;
        File outputFile = new File(context1.getCacheDir(), apkPackage);
        assert dexDocFile != null;
        File dexFile = new File(context1.getCacheDir(), Objects.requireNonNull(dexDocFile.getName()));

        try {
            InputStream inputStream = context1.getContentResolver().openInputStream(dexDocFile.getUri());
            OutputStream outputStream = Files.newOutputStream(dexFile.toPath());
            byte[] buffer = new byte[8092];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            inputStream.close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        new Thread(() -> {
            BaksmaliAdapter.doBaksmali(dexFile.getAbsolutePath(), outputFile.getPath());
//                yourActivity.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        txtview.setText("some value");
//                        edittext.setText("some new value");
//                    }
//                });
            Log.e(TAG,"SUCCESSFUL");

            Boolean result = createOpcodeSeq(apkPackage);
            Log.e(TAG,"SUCCESSFUL");
        }).start();
    }

    public boolean createOpcodeSeq(String apkPackage) {
        Map<String, String> dalvikOpCodes = new HashMap<>();
        try {
            InputStream inputStream = context1.getAssets().open("DALVIK_OP_CODES_FILE.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ");
                dalvikOpCodes.put(parts[0], parts[1]);
            }
            reader.close();
        } catch (IOException e) {
            Log.e(TAG, "Exception occurred while reading dalvik opcodes file: " + e.getMessage());
            return false;
        }

        try {
            File smaliDir = new File(context1.getCacheDir(), apkPackage);
            File opseqFile = new File(context1.getCacheDir(), apkPackage + ".txt");
            FileOutputStream opseqFileStream = new FileOutputStream(opseqFile);
            for (File smaliFile : Objects.requireNonNull(smaliDir.listFiles())) {
                if (!smaliFile.isFile()) continue;
                String opCodeSeq = getOpCodeSeq(smaliFile, dalvikOpCodes);
                opseqFileStream.write(opCodeSeq.getBytes());
            }
            opseqFileStream.close();

            return true;
        } catch (IOException e) {
            Log.e(TAG, "Exception occurred during opseq creation: " + e.getMessage());
            return false;
        }
    }

    private static String getOpCodeSeq(File smaliFile, Map<String, String> dalvikOpCodes) {
        StringBuilder opCodeSeq = new StringBuilder();
        try {
            FileInputStream fis = new FileInputStream(smaliFile);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader reader = new BufferedReader(isr);
            String line;
            boolean methodStarted = false;
            while ((line = reader.readLine()) != null) {
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
            reader.close();
        } catch (IOException e) {
            Log.e(TAG, "Exception occurred while reading smali file: " + e.getMessage());
        }
        return opCodeSeq.toString();
    }

    private void unzipFile(ZipEntry fileEntry, java.util.zip.ZipInputStream zipInputStream, DocumentFile destDir) throws IOException {
        int readLen;
        byte[] readBuffer = new byte[8096];

        DocumentFile destFile = destDir.createFile("*/*", fileEntry.getName());

        assert destFile != null;
        try (OutputStream outputStream = context1.getContentResolver().openOutputStream(destFile.getUri())) {
            while ((readLen = zipInputStream.read(readBuffer)) != -1) {
                outputStream.write(readBuffer, 0, readLen);
            }
        }
    }

    public DocumentFile unzip(File apkFile, String apkPackage) throws IOException {
        ZipInputStream zis = new ZipInputStream(
                new BufferedInputStream(Files.newInputStream(apkFile.toPath())));
        DocumentFile extractDir = DocumentFile.fromTreeUri(context1, Uri.parse(folder));
        assert extractDir != null;
        DocumentFile currDir = Objects.requireNonNull(extractDir.createDirectory(apkPackage)).createDirectory("dex");
        if (currDir == null) {
            throw new IOException("Could not create subdirectory for extraction.");
        }
        try {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory() && entry.getName().contains("class")) {
                    unzipFile(entry, zis, currDir);
                }
            }
        } finally {
            zis.close();
        }

        return currDir;
    }
}
