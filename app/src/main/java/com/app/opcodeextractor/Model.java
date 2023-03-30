package com.app.opcodeextractor;

import android.content.Context;
import android.util.Log;

import com.app.opcodeextractor.decoder.OPcode;

import org.dmg.pmml.DataField;
import org.dmg.pmml.PMML;
import org.glassfish.jaxb.core.Utils;
import org.jpmml.evaluator.ModelEvaluator;
import org.jpmml.evaluator.ModelEvaluatorBuilder;
import org.jpmml.evaluator.ModelEvaluatorFactory;
import org.jpmml.model.visitors.LocatorNullifier;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.ParserConfigurationException;

import jakarta.xml.bind.JAXBException;

public class Model {

    Context context1;
    Map<String, String> dalvikOpCodes;

    public Model(Context context2) {
        context1 = context2;
        this.dalvikOpCodes = new HashMap<>();
        OPcode op = new OPcode(context1);
        this.dalvikOpCodes.putAll(op.getReverseDalvikOpCodes());
    }

    public void main(Map<String, Integer> opcodeMap) throws IOException, JAXBException, ParserConfigurationException, SAXException {
        InputStream ins = context1.getResources().openRawResource(
                context1.getResources().getIdentifier("model",
                        "raw", context1.getPackageName()));
        PMML model = load(ins);
        ins.close();

        LocatorNullifier nullifier = new LocatorNullifier();
        nullifier.applyTo(model);

        ModelEvaluatorFactory modelEvaluatorFactory = ModelEvaluatorFactory.newInstance();
        ModelEvaluator<?> modelEvaluator = modelEvaluatorFactory.newModelEvaluator(model, model.getModels().get(0));

        Map<String, Object> inputData = mapToCode(opcodeMap);
        Map<String, ?> result = modelEvaluator.evaluate(inputData);

        Double output = (Double) result.get("label");
        Log.e("Output", String.valueOf(output));
    }

    public PMML load(InputStream is) throws SAXException, JAXBException, ParserConfigurationException {
        return org.jpmml.model.PMMLUtil.unmarshal(is);
    }

    public Map<String, Object> mapToCode(Map<String, Integer> opcodeMap){

        HashMap<String, Object> updatedMap = new HashMap<>();
        for (Map.Entry<String, Integer> entry : opcodeMap.entrySet()) {
            String oldKey = entry.getKey();
            String newKey = dalvikOpCodes.get(oldKey);
            Integer value = entry.getValue();
            updatedMap.put(newKey, value);
        }
        System.out.println(updatedMap);

        return updatedMap;
    }
}
