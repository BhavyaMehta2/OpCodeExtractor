package com.app.opcodeextractor;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ModelSelector{
    HashMap<String, String> models;

    public ModelSelector(){
        models = new HashMap<>();
        models.put("Decision Tree", "DT");
        models.put("Naive Bayes", "NB");
        models.put("Support Vector Machine", "SVM");
        models.put("Random Forest", "RF");
        models.put("Extra Tree", "Extra Tree");
        models.put("Bagging", "Bagging");
        models.put("AdaBoost", "AdaBoost");
        models.put("GradientBoost", "GradientBoost");
        models.put("XGBoost", "XGBoost");
        models.put("Deep Neural Network: 1 Layer", "DNN-1");
        models.put("Deep Neural Network: 3 Layer", "DNN-3");
        models.put("Deep Neural Network: 5 Layer", "DNN-5");
    }

    public List<String> GetAllModels(){
        List<String> AllModels = new ArrayList<>(models.keySet());
        AllModels.sort(String.CASE_INSENSITIVE_ORDER);
        return AllModels;
    }

    public String GetApiHeader(String key){
        return models.get(key);
    }
}