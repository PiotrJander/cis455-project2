package edu.upenn.cis455.storage;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;
import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

import java.io.File;
import java.net.URL;
import java.util.Date;
import java.util.Optional;

public class DBWrapper {
	
	private static String envDirectory = null;
	
	private static Environment env;
	private static EntityStore store;
    private static UserAccessor userAccessor;
    private static DocumentAccessor documentAccessor;

	/* TODO: write object store wrapper for BerkeleyDB */

    public static void init(File envHome) throws DatabaseException {

        /* Open a transactional Berkeley DB engine environment. */
        EnvironmentConfig envConfig = new EnvironmentConfig();
        envConfig.setAllowCreate(true);
//        envConfig.setTransactional(true);
        env = new Environment(envHome, envConfig);

        /* Open a transactional entity store. */
        StoreConfig storeConfig = new StoreConfig();
        storeConfig.setAllowCreate(true);
//        storeConfig.setTransactional(true);
        store = new EntityStore(env, "UserStore", storeConfig);

        /* Initialize the data access object. */
        userAccessor = new UserAccessor(store);
        documentAccessor = new DocumentAccessor(store);
    }

    public static void addUser(String username, String password) {
        userAccessor.userByUsername.put(new User(username, password));
    }

    public static Optional<String> getUserPassword(String username) {
        return Optional.ofNullable(userAccessor.userByUsername.get(username)).map(user -> user.password);
    }

    public static void addDocument(URL url, String text) {
        documentAccessor.documentByUrl.put(new Document(url, text));
    }

    public static Date getDocumentDate(URL url) {
        Document document = documentAccessor.documentByUrl.get(url.toString());
        if (document != null) {
            return document.dateRetrieved;
        } else {
            return null;
        }
    }

    public static String getDocumentText(URL url) {
        Document document = documentAccessor.documentByUrl.get(url.toString());
        if (document != null) {
            return document.text;
        } else {
            return null;
        }
    }

    /* An entity class. */
    @Entity
    private static class User {

        @PrimaryKey
        String username;

        String password;

        User(String username, String password) {
            this.username = username;
            this.password = password;
        }

        private User() {} // For deserialization
    }

    /* The data accessor class for the entity model. */
    private static class UserAccessor {

        PrimaryIndex<String,User> userByUsername;

        UserAccessor(EntityStore store) throws DatabaseException {
            userByUsername = store.getPrimaryIndex(String.class, User.class);
        }
    }

    /* An entity class. */
    @Entity
    private static class Document {

        @PrimaryKey
        String url;

        Date dateRetrieved;
        String text;

        Document(URL url, String text) {
            this.url = url.toString();
            this.dateRetrieved = new Date();
            this.text = text;
        }

        private Document() {} // For deserialization
    }

    /* The data accessor class for the entity model. */
    private static class DocumentAccessor {

        PrimaryIndex<String,Document> documentByUrl;

        DocumentAccessor(EntityStore store) throws DatabaseException {
            documentByUrl = store.getPrimaryIndex(String.class, Document.class);
        }
    }
	
}
