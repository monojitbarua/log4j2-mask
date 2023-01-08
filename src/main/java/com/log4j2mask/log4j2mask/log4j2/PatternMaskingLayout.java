package com.log4j2mask.log4j2mask.log4j2;


import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.log4j2mask.log4j2mask.util.MaskUtil;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component("patternMaskingLayout")
public class PatternMaskingLayout extends PatternLayout {

    private Pattern multilinePattern;
    private List<String> maskPatterns = new ArrayList<>();
    private static final String XML ="XML";
    private static final String JSON ="JSON";

    public void addMaskPattern(String maskPattern) { // invoked for every single entry in the xml
        maskPatterns.add(maskPattern);
        multilinePattern = Pattern.compile(String.join("|", maskPatterns), // build pattern using logical OR
                Pattern.MULTILINE);
    }

    @Override
    public String doLayout(ILoggingEvent event) {
        try {
            return maskMessage(super.doLayout(event)); // calling superclass method is required
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String maskMessage(String message) throws Exception {
        String finalMaskedString = null;
        if (multilinePattern == null) {
            return message;
        }
        StringBuilder sb = new StringBuilder(message);
        Matcher matcher = multilinePattern.matcher(sb);

        for (String pattern : maskPatterns) {
            String finalPattern;
            if (pattern.contains(XML)) {
                String[] split = pattern.split(":");
                finalPattern = split[1];
                if (sb.toString().contains(finalPattern)){
                    finalMaskedString = maskXML(sb.toString(), finalPattern);
                    return finalMaskedString;
                }
            } else if (pattern.contains(JSON)) {
                String[] split = pattern.split(":");
                finalPattern = split[1];
                if (sb.toString().contains(finalPattern)){
                    JsonFactory factory = new JsonFactory();
                    ObjectMapper mapper = new ObjectMapper(factory);
                    JsonNode rootNode = mapper.readTree(sb.toString());
                    maskJson(rootNode, finalPattern);
                    return rootNode.toString();
                }
            } else if (matcher.group().contains("creditCard") || matcher.group().contains("password")) {
                finalMaskedString =  maskFieldData(sb, matcher);;
                return finalMaskedString;
            }
        }
        return finalMaskedString;
    }
    private String mask(String pattern, StringBuilder sb) {
        Matcher matcher = Pattern.compile(pattern).matcher(sb);
        while (matcher.find()) {
            if (matcher.group().contains("creditCard") || matcher.group().contains("password")) {
                maskFieldData(sb, matcher);
            }
        }
        return sb.toString();
    }

    private String maskFieldData(StringBuilder sb, Matcher matcher) {
        // here is our main logic for masking sensitive data
        String targetExpression = matcher.group();
        String[] split = null;
        if (targetExpression.contains("=")) {
            split = targetExpression.split("=");
        } else if (targetExpression.contains(" ")) {
            split = targetExpression.split(" ");
        } else {
            split = targetExpression.split(":");
        }
        if (split != null) {
            String pan = split[1];
            String maskedPan = getMaskedPan(pan);
            int start = matcher.start() + split[0].length() + 1;
            int end = matcher.end();
            sb.replace(start, end, maskedPan);
        }
        return sb.toString();
    }

    public static String getMaskedPan(String pan) {
        pan = pan.replaceAll(pan, "*******");
        return pan;
    }












    private static String maskXML(String xmlString, String finaPattern) {
        String str = null;
        try {
            // Convert string to XML document
            Document document = toXmlDocument(xmlString);
            // Now mask the required fields in the XML
            maskElements(document.getDocumentElement(), finaPattern);
            // Convert document object to string
            xmlString = toXmlString(document).replace("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>","");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return xmlString;

    }

    private static Document toXmlDocument(String str) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        Document document = docBuilder.parse(new InputSource(new StringReader(str)));
        return document;
    }

    private static String toXmlString(Document document) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(document);
        StringWriter strWriter = new StringWriter();
        StreamResult result = new StreamResult(strWriter);
        transformer.transform(source, result);
        return strWriter.getBuffer().toString();
    }

    public static void maskElements(Node node, String finaPattern) {
        NodeList nodeList = node.getChildNodes();

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node currentNode = nodeList.item(i);
            // recursively call maskElements until you find a Leaf node
            if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                maskElements(currentNode,finaPattern);
            } else if (currentNode.getNodeType() == Node.TEXT_NODE) {
                // leaf node.. apply masking logic
                String name = currentNode.getParentNode().getNodeName();
                if (name != null && finaPattern.equals(name)) {
                    currentNode.setTextContent("********");
                }
            }
        }
    }







    public static void maskJson(JsonNode rootNode, String key) throws Exception {
        Iterator<Map.Entry<String, JsonNode>> fieldsIterator = rootNode.fields();
        while (fieldsIterator.hasNext()) {
            Map.Entry<String, JsonNode> field = fieldsIterator.next();
            if (field.getKey().equals(key)) {
                JsonFactory factory = new JsonFactory();
                ObjectMapper mapper = new ObjectMapper(factory);
                JSONObject obj = new JSONObject();
                obj.put(field.getKey(), "***********");
                field.setValue(mapper.readTree(obj.toString()));
                return;
            }
            maskJson(field.getValue(), key);
        }
    }


}