package com.app.opcodeextractor;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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

        File apkFile = (File) getIntent().getExtras().get("apk");
        ApplicationPackageName = (String) getIntent().getExtras().get("package");

        ListView listView = findViewById(R.id.intents_list);
        ArrayList<String> permissions = Extract.main(apkFile);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.activity_listview, permissions);
        listView.setAdapter(adapter);
    }
}