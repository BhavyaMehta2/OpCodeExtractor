package com.app.opcodeextractor;

import static com.app.opcodeextractor.decoder.OPcode.getDalvikOpCodes;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Model {

    Context context1;
    Map<String, String> dalvikOpCodes;

    public Model(Context context2) {
        context1 = context2;
        this.dalvikOpCodes = new HashMap<>();
        this.dalvikOpCodes.putAll(getDalvikOpCodes());
    }

    public String main(Map<String, Integer> opcodeMap, String header) throws IOException {
        List<Integer> inputData = mapToCode(opcodeMap);

        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create("{\n\t\"type\" : \"opcode\",\n\t\"model\" : \""+header+"\",\n\t\"attributes\" :"+inputData+"\n\n}" ,mediaType);
        Request request = new Request.Builder()
                .url("https://mr.eninehq.com:5000/predict")
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .addHeader("auth", "tha9ahqu5aivelahdahhaedeihahsimab7thoo6mi4shaeCeituN9AeB0AivieF0RieThiongiexueGohNgieK5shu9ahw8aiqui")
                .build();

        try (Response response = client.newCall(request).execute()) {
            assert response.body() != null;
            JSONObject json = new JSONObject(response.body().string());

            Log.e("JSON", String.valueOf(json));

            return (json.getString("prediction").equalsIgnoreCase("0"))?"Benign":"Malicious";
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Integer> mapToCode(Map<String, Integer> opcodeMap){
        LinkedHashMap<String, Integer> updatedMap = new LinkedHashMap<>();
        List<String> order = Arrays.asList("add-double","add-double-2addr","add-float","add-float-2addr","add-int","add-int-2addr","add-int-lit16","add-int-lit8","add-long","add-long-2addr","aget","aget-boolean","aget-byte","aget-char","aget-object","aget-short","aget-wide","and-int","and-int-2addr","and-int-lit16","and-int-lit8","and-long","and-long-2addr","aput","aput-boolean","aput-byte","aput-char","aput-object","aput-short","aput-wide","array-length","check-cast","cmp-long","cmpg-double","cmpg-float","cmpl-double","cmpl-float","const","const-class","const-string","const-string-jumbo","const-wide","const-wide-16","const-wide-32","const-wide-high16","const-16","const-4","const-high16","div-double","div-double-2addr","div-float","div-float-2addr","div-int","div-int-2addr","div-int-lit16","div-int-lit8","div-long","div-long-2addr","double-to-float","double-to-int","double-to-long","execute-inline","fill-array-data","filled-new-array","filled-new-array-range","float-to-double","float-to-int","float-to-long","goto","goto-16","goto-32","if-eq","if-eqz","if-ge","if-gez","if-gt","if-gtz","if-le","if-lez","if-lt","if-ltz","if-ne","if-nez","iget","iget-boolean","iget-byte","iget-char","iget-object","iget-object-quick","iget-quick","iget-short","iget-wide","iget-wide-quick","instance-of","int-to-byte","int-to-char","int-to-double","int-to-float","int-to-long","int-to-short","invoke-direct","invoke-direct-empty","invoke-direct-range","invoke-interface","invoke-interface-range","invoke-static","invoke-static-range","invoke-super","invoke-super-quick","invoke-super-quick-range","invoke-super-range","invoke-virtual","invoke-virtual-quick","invoke-virtual-quick-range","invoke-virtual-range","iput","iput-boolean","iput-byte","iput-char","iput-object","iput-object-quick","iput-quick","iput-short","iput-wide","iput-wide-quick","long-to-double","long-to-float","long-to-int","monitor-enter","monitor-exit","move","move-exception","move-object","move-object-16","move-object-from16","move-result","move-result-object","move-result-wide","move-wide","move-wide-16","move-wide-from16","move-16","move-from16","mul-double","mul-double-2addr","mul-float","mul-float-2addr","mul-int","mul-int-2addr","mul-int-lit16","mul-int-lit8","mul-long","mul-long-2addr","neg-double","neg-float","neg-int","neg-long","new-array","new-instance","nop","not-int","not-long","or-int","or-int-2addr","or-int-lit16","or-int-lit8","or-long","or-long-2addr","packed-switch","rem-double","rem-double-2addr","rem-float","rem-float-2addr","rem-int","rem-int-2addr","rem-int-lit16","rem-int-lit8","rem-long","rem-long-2addr","return","return-object","return-void","return-wide","sget","sget-boolean","sget-byte","sget-char","sget-object","sget-short","sget-wide","shl-int","shl-int-2addr","shl-int-lit8","shl-long","shl-long-2addr","shr-int","shr-int-2addr","shr-int-lit8","shr-long","shr-long-2addr","sparse-switch","sput","sput-boolean","sput-byte","sput-char","sput-object","sput-short","sput-wide","sub-double","sub-double-2addr","sub-float","sub-float-2addr","sub-int","sub-int-2addr","sub-int-lit16","sub-int-lit8","sub-long","sub-long-2addr","throw","unused_3E","unused_3F","unused_40","unused_41","unused_42","unused_43","unused_73","unused_79","unused_7A","unused_E3","unused_E4","unused_E5","unused_E6","unused_E7","unused_E8","unused_E9","unused_EA","unused_EB","unused_EC","unused_ED","unused_EF","unused_F1","unused_FC","unused_FD","unused_FE","unused_FF","ushr-int","ushr-int-2addr","ushr-int-lit8","ushr-long","ushr-long-2addr","xor-int","xor-int-2addr","xor-int-lit16","xor-int-lit8","xor-long","xor-long-2addr");
        for(String s:order)
            updatedMap.put(s,0);

        for (Map.Entry<String, String> entry : dalvikOpCodes.entrySet()) {
            String opvalue = entry.getValue();
            String opcode = entry.getKey();
            if(order.contains(String.valueOf(opvalue).replace("/", "-"))) {
                int value = opcodeMap.get(opvalue);
                updatedMap.put(String.valueOf(opcode).replace("/", "-"), value);
            }
        }

        Log.d("TAG", String.valueOf(updatedMap));
        return new ArrayList<>(updatedMap.values());
    }
}
