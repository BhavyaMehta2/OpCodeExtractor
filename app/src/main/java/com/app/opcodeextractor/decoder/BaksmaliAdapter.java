package com.app.opcodeextractor.decoder;

import org.jf.baksmali.Main;


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
