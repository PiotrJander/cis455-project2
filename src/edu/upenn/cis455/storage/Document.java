package edu.upenn.cis455.storage;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

import java.net.URL;
import java.util.Date;

/* An entity class. */
@Entity
public class Document {

    @SuppressWarnings("unused")
    @PrimaryKey
    private String url;

    private Date dateRetrieved;
    private String text;
    private String contentType;

    Document(URL url, String text, String contentType) {
        this.url = url.toString();
        this.dateRetrieved = new Date();
        this.text = text;
        this.contentType = contentType;
    }

    private Document() {
    } // For deserialization

    public Date getDateRetrieved() {
        return dateRetrieved;
    }

    public String getText() {
        return text;
    }

    public String getContentType() {
        return contentType;
    }
}
