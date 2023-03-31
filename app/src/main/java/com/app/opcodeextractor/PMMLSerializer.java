package com.app.opcodeextractor;

import org.dmg.pmml.PMML;
import org.jpmml.model.PMMLUtil;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.LocatorImpl;

import java.io.*;
import java.nio.file.Files;
import java.util.function.Predicate;

import javax.xml.parsers.ParserConfigurationException;

import jakarta.xml.bind.JAXBException;

public class PMMLSerializer {
    public static void main(String[] args) throws JAXBException, SAXException, IOException, ParserConfigurationException {
        // Load the PMML file into a PMML object
        File pmmlFile = new File("D:\\AndroidDev\\OpCodeExtractor\\app\\src\\main\\pmml\\model.pmml");
        InputStream is = Files.newInputStream(pmmlFile.toPath());
        PMML pmml = PMMLUtil.unmarshal(is);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(pmml);
        byte[] serializedPmml = baos.toByteArray();

        // Write the serialized PMML to a file
        FileOutputStream fos = new FileOutputStream("D:\\AndroidDev\\OpCodeExtractor\\app\\src\\main\\pmml\\model.ser");
        fos.write(serializedPmml);
        fos.close();

        System.out.println("PMML serialization successful!");
    }
}