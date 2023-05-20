package com.app.opcodeextractor;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class PermissionsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions_activitiy);

        ListView listView = findViewById(R.id.permissions_list);

        ArrayList<String> permissions = new ArrayList<>();

        String ApplicationPackageName = (String) getIntent().getExtras().get("package");
        PackageManager pm = this.getPackageManager();

        Field[] perm = Manifest.permission.class.getDeclaredFields();
        Field[] permGroups = Manifest.permission_group.class.getDeclaredFields();

        for(Field p:perm)
        {
            int hasPerm;
            try {
                hasPerm = pm.checkPermission((String) p.get(Manifest.permission.class), ApplicationPackageName);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            if (hasPerm == PackageManager.PERMISSION_GRANTED) {
                permissions.add(p.getName());
            }
        }

        for(Field p:permGroups)
        {
            int hasPerm;
            try {
                hasPerm = pm.checkPermission((String) p.get(Manifest.permission_group.class), ApplicationPackageName);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            if (hasPerm == PackageManager.PERMISSION_GRANTED) {
                permissions.add(p.getName());
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.activity_listview, permissions);
        listView.setAdapter(adapter);
    }
}