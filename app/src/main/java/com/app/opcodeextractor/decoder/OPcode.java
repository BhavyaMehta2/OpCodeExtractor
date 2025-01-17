package com.app.opcodeextractor.decoder;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

public class OPcode {
    static HashMap<String, String> dalvikOpCodes = new HashMap<>();

    public OPcode(Context context1)
    {
        try {
            InputStream inputStream = context1.getAssets().open("DALVIK_OP_CODES_FILE.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream), 8192);
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ");
                dalvikOpCodes.put(parts[0], parts[1]);
            }
            reader.close();
        } catch (IOException e) {
            Log.e("DALVIK_OPCODE", "Exception occurred while reading dalvik opcodes file: " + e.getMessage());
        }
    }

    public static HashMap<String,String> getDalvikOpCodes()
    {
        return dalvikOpCodes;
    }
}
