package edu.upenn.cis455.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sleepycat.persist.model.PrimaryKey;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Channel {

    @PrimaryKey
    private String name;

    private String xpath;
    private String createdBy;
    private String documents = "[]";

    public Channel(String name, String xpath, String createdBy) {
        this.name = name;
        this.xpath = xpath;
        this.createdBy = createdBy;
    }

    public Channel() {
    }

    public String getName() {
        return name;
    }

    public String getXpath() {
        return xpath;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public List<String> getDocumentsAsList() {
        try {
            ObjectMapper mapper;
            mapper = new ObjectMapper();
            return Arrays.asList(mapper.readValue(this.documents, String[].class));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

//    public void setDocumentsFromList(List<String> documents) {
//        try {
//            ObjectMapper mapper = new ObjectMapper();
//            this.documents = mapper.writeValueAsString(documents);
//        } catch (JsonProcessingException e) {
//            e.printStackTrace();
//        }
//    }

    public void addDocument(String url) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<String> documents = new ArrayList<>(Arrays.asList(mapper.readValue(this.documents, String[].class)));
            documents.add(url);
            this.documents = mapper.writeValueAsString(documents);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    public void removeDocument(String url) {
//        try {
//            ObjectMapper mapper = new ObjectMapper();
//            List<String> documents = new ArrayList<>(Arrays.asList(mapper.readValue(this.documents, String[].class)));
//            documents.remove(url);
//            this.documents = mapper.writeValueAsString(documents);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}
