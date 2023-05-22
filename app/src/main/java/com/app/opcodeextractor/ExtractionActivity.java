package com.app.opcodeextractor;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class ExtractionActivity extends AppCompatActivity {
    private static final String TAG = "ExtractionActivity";
    RecyclerView recyclerView;
    String ApplicationPackageName;
    TextView xmlDisplay;

    long start;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extraction);

        xmlDisplay = findViewById(R.id.xml);

        xmlDisplay.setMovementMethod(new ScrollingMovementMethod());

        File apkFile = (File) getIntent().getExtras().get("apk");
        ApplicationPackageName = (String) getIntent().getExtras().get("package");

        try {
            clearPrevFiles(ApplicationPackageName, apkFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void clearPrevFiles(String apkPackage, File apkFile) throws IOException {
        xmlDisplay.setText(Extract.main(apkFile));
    }

    void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : Objects.requireNonNull(fileOrDirectory.listFiles()))
                deleteRecursive(child);
        fileOrDirectory.delete();
    }
}