package com.app.opcodeextractor;

import android.content.Context;
import android.util.Log;

import com.app.opcodeextractor.decoder.OPcode;

import org.dmg.pmml.PMML;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;

import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

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
//        InputStream ins = context1.getResources().openRawResource(
//                context1.getResources().getIdentifier("model",
//                        "raw", context1.getPackageName()));
//        PMML model = load(ins);
//        ins.close();
//
////        LocatorNullifier nullifier = new LocatorNullifier();
////        nullifier.applyTo(model);
//
//        ModelEvaluatorFactory modelEvaluatorFactory = ModelEvaluatorFactory.newInstance();
//        ModelEvaluator<?> modelEvaluator = modelEvaluatorFactory.newModelEvaluator(model, model.getModels().get(0));
//
//        Map<String, Object> inputData = mapToCode(opcodeMap);
//        Map<String, ?> result = modelEvaluator.evaluate(inputData);
//
//        Double output = (Double) result.get("label");
//        Log.e("Output", String.valueOf(output));
    }

    public PMML load(InputStream is) throws SAXException, JAXBException, ParserConfigurationException {
//        SAXParserFactory spf = SAXParserFactory.newInstance();
//        spf.setFeature("http://xml.org/sax/features/external-general-entities", false);
//        spf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
////        spf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
//        XMLReader xmlReader = spf.newSAXParser().getXMLReader();
//        InputSource inputSource = new InputSource(is);
//        SAXSource source = new SAXSource(xmlReader, inputSource);
//        return unmarshal(source);
        return null;
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
