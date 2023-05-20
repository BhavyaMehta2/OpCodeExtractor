package com.app.opcodeextractor;

import static com.app.opcodeextractor.decoder.OPcode.getDalvikOpCodes;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
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
        Map<String, Object> inputData = mapToCode(opcodeMap);
        JSONObject jsonObject = new JSONObject(inputData);

        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(String.valueOf(jsonObject), mediaType);
        Request request = new Request.Builder()
                .url("https://av.eninehq.com/predict")
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .addHeader("apikey", "aeshahpheel2autai1eephaith8Ohchi")
                .addHeader("model", header)
                .build();

        try (Response response = client.newCall(request).execute()) {
            assert response.body() != null;
            JSONObject json = new JSONObject(response.body().string());

            return (json.getString("classification").equalsIgnoreCase("0"))?"Benign":"Malicious";
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, Object> mapToCode(Map<String, Integer> opcodeMap){
        HashMap<String, Object> updatedMap = new HashMap<>();
        List<String> deprecated = Arrays.asList("const-string-jumbo", "execute-inline", "filled-new-array-range", "unused_FD", "iget-quick", "sub-int/lit8", "invoke-interface-range", "iput-object-quick", "sub-int/lit16", "unused_FE", "iget-object-quick", "invoke-virtual-quick/range", "iget-wide-quick", "invoke-super-quick/range", "invoke-super-quick", "invoke-virtual-quick", "unused_FC", "iput-wide-quick", "unused_FF", "iput-quick", "invoke-direct-empty");
        for(String s:deprecated)
            updatedMap.put(s,0);

        for (Map.Entry<String, String> entry : dalvikOpCodes.entrySet()) {
            String opvalue = entry.getValue();
            String opcode = entry.getKey();
            if (!opcodeMap.containsKey(String.valueOf(opvalue))) {
                updatedMap.put(String.valueOf(opcode), 0);
            } else {
                int value = opcodeMap.get(opvalue);
                updatedMap.put(String.valueOf(opcode), value);
            }
        }
        return updatedMap;
    }
}
