package edu.upenn.cis455.storage;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

import java.net.URL;
import java.util.Date;

/* An entity class. */
@Entity
public class Document {

    @PrimaryKey
    private String url;

    private Date dateRetrieved;
    private String text;
    private boolean isHtml;

    Document(URL url, String text, boolean isHtml) {
        this.url = url.toString();
        this.dateRetrieved = new Date();
        this.text = text;
        this.isHtml = isHtml;
    }

    private Document() {
    } // For deserialization

    public Date getDateRetrieved() {
        return dateRetrieved;
    }

    public String getText() {
        return text;
    }

    public boolean isHtml() {
        return isHtml;
    }
}


