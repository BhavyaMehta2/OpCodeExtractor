package com.app.opcodeextractor;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.GridLayoutManager;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager recyclerViewLayoutManager;
    ActivityResultLauncher<Intent> launcher;
    private Uri baseDocumentTreeUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerViewLayoutManager = new GridLayoutManager(MainActivity.this, 1);
        recyclerView.setLayoutManager(recyclerViewLayoutManager);
        adapter = new AppsAdapter(MainActivity.this, new ApkInfoExtractor(MainActivity.this).GetAllInstalledApkInfo());
        recyclerView.setAdapter(adapter);

        SharedPreferences preferences = getSharedPreferences("com.app.opcodeextractor", Context.MODE_PRIVATE);

         launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
                 result -> {
                     if (result.getResultCode() == Activity.RESULT_OK) {
                         baseDocumentTreeUri = Objects.requireNonNull(result.getData()).getData();
                         final int takeFlags = (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                         getContentResolver().takePersistableUriPermission(result.getData().getData(), takeFlags);
                         preferences.edit().putString("filestorageuri", result.getData().getData().toString()).apply();

                         DocumentFile pickedDir = DocumentFile.fromTreeUri(this, baseDocumentTreeUri);
                         assert pickedDir != null;
                     } else {
                         Log.e("FileUtility", "Some Error Occurred : " + result);
                     }
                 }
         );
         if(preferences.getString("filestorageuri", "null").equals("null"))
            launchBaseDirectoryPicker();
         else {
             final int takeFlags = (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
             getContentResolver().takePersistableUriPermission(Uri.parse(preferences.getString("filestorageuri", "null")), takeFlags);
         }
}

    public void launchBaseDirectoryPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        launcher.launch(intent);
    }
}