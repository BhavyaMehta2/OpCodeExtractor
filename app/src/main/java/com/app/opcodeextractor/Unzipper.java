package com.app.opcodeextractor;

import android.content.Context;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Unzipper {
    private static final int BUFFER_SIZE = 32768;

    private static void unzipFile(ZipEntry fileEntry, ZipInputStream zipInputStream, File destDir) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int readLen;

        File destFile = new File(destDir, fileEntry.getName());

        try (OutputStream outputStream = Files.newOutputStream(destFile.toPath())) {
            while ((readLen = zipInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, readLen);
            }
        }
    }

    public static File unzip(Context context, File apkFile, String apkPackage) throws IOException {
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(Files.newInputStream(apkFile.toPath()), BUFFER_SIZE));
        File extractDir = new File(context.getCacheDir(), apkPackage);

        if (!extractDir.exists()) {
            extractDir.mkdirs();
        }

        File currDir = new File(context.getCacheDir(), apkPackage + "/dex");
        if (!currDir.exists()) {
            currDir.mkdirs();
        }

        ZipEntry entry;
        try {
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory() && entry.getName().contains("class")) {
                    try {
                        unzipFile(entry, zis, currDir);
                    }
                    catch(Exception e)
                    {
                        Log.e("Unzipping", String.valueOf(e));
                    }
                }
            }
        } finally {
            zis.close();
        }

        return currDir;
    }
}

