package com.app.opcodeextractor.decoder;

import androidx.documentfile.provider.DocumentFile;

import org.jf.baksmali.Main;
import java.io.IOException;


public class BaksmaliAdapter {

    public static void doBaksmali(String apkPath, String outputPath){
//disassemble app.apk -o app

        Main.main(new String[]{
                "disassemble",
                apkPath,
                "-o",
                outputPath,
        });

    }
}