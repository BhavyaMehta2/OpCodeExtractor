package com.app.opcodeextractor;

import android.content.Context;
import android.content.Intent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import android.content.pm.ApplicationInfo;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;

public class ApkInfoExtractor {
    Context context1;

    public ApkInfoExtractor(Context context2){
        context1 = context2;
    }

    public List<String> GetAllInstalledApkInfo(){
        List<String> ApkPackageName = new ArrayList<>();
        Intent intent = new Intent(Intent.ACTION_MAIN,null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> resolveInfoList = context1.getPackageManager().queryIntentActivities(intent,0);

        for(ResolveInfo resolveInfo : resolveInfoList){
            ActivityInfo activityInfo = resolveInfo.activityInfo;
            if(!isSystemPackage(resolveInfo)) {
                ApkPackageName.add(activityInfo.applicationInfo.packageName);
            }
        }
        ApkPackageName.sort(String.CASE_INSENSITIVE_ORDER);
        return ApkPackageName;
    }

    public boolean isSystemPackage(ResolveInfo resolveInfo){
        return ((resolveInfo.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
    }

    public Drawable getAppIconByPackageName(String ApkTempPackageName){
        Drawable drawable;
        try{
            drawable = context1.getPackageManager().getApplicationIcon(ApkTempPackageName);
        }
        catch (PackageManager.NameNotFoundException e){
            e.printStackTrace();
            drawable = ContextCompat.getDrawable(context1, R.mipmap.ic_launcher);
        }
        return drawable;
    }

    public String GetAppName(String ApkPackageName){
        String Name = "";
        ApplicationInfo applicationInfo;

        PackageManager packageManager = context1.getPackageManager();
        try {
            applicationInfo = packageManager.getApplicationInfo(ApkPackageName, 0);
            if(applicationInfo!=null){
                Name = (String)packageManager.getApplicationLabel(applicationInfo);
            }
        }catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return Name;
    }

    public File getApk(String ApkPackageName){
        ApplicationInfo applicationInfo;
        File Apk = null;
        PackageManager packageManager = context1.getPackageManager();
        try {
            applicationInfo = packageManager.getApplicationInfo(ApkPackageName, 0);
            if(applicationInfo!=null){
                Apk =  new File(applicationInfo.publicSourceDir);
            }
        }catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return Apk;
    }
}
