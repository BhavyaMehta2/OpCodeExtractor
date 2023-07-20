package com.app.intentextractor;

import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;

public class ExtractionActivity extends AppCompatActivity {
    private static final String TAG = "ExtractionActivity";

    static FrameLayout pBar;
    RecyclerView recyclerView;
    ModelRVAdapter adapter;
    String ApplicationPackageName;
    static ArrayList<String> intents;

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


        File apkFile = (File) getIntent().getExtras().get("apk");
        ApplicationPackageName = (String) getIntent().getExtras().get("package");

        intents = Extract.main(apkFile);

    }
}