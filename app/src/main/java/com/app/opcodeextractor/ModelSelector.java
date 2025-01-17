package com.app.opcodeextractor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ModelSelector{
    HashMap<String, String> models;

    public ModelSelector(){
        models = new HashMap<>();
        models.put("Decision Tree", "dt");
        models.put("Naive Bayes", "nb");
        models.put("Support Vector Machine", "svm");
        models.put("Random Forest", "rf");
        models.put("Extra Tree", "extra");
        models.put("Bagging", "bagging");
        models.put("AdaBoost", "ada");
        models.put("GradientBoost", "gradboost");
        models.put("Deep Neural Network: 1 Layer", "dnn1");
        models.put("Deep Neural Network: 3 Layer", "dnn3");
        models.put("Deep Neural Network: 5 Layer", "dnn5");
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
