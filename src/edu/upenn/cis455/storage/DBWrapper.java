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
import java.util.Optional;

public class DBWrapper {
	
	private static String envDirectory = null;
	
	private static Environment env;
	private static EntityStore store;
    private static UserAccessor userAccessor;
    private static DocumentAccessor documentAccessor;

    public static void init(File envHome) throws DatabaseException {

        // create the directory if it doesn't exist
        try {
            //noinspection ResultOfMethodCallIgnored
            envHome.mkdirs();
        } catch (Exception e) {
            e.printStackTrace();
        }

        /* Open a transactional Berkeley DB engine environment. */
        EnvironmentConfig envConfig = new EnvironmentConfig();
        envConfig.setAllowCreate(true);
//        envConfig.setTransactional(true);
        env = new Environment(envHome, envConfig);

        /* Open a transactional entity store. */
        StoreConfig storeConfig = new StoreConfig();
        storeConfig.setAllowCreate(true);
//        storeConfig.setTransactional(true);
        store = new EntityStore(env, "CrawlerStore", storeConfig);

        /* Initialize the data access object. */
        userAccessor = new UserAccessor(store);
        documentAccessor = new DocumentAccessor(store);
    }

    public static void close() {
        if (store != null) {
            try {
                store.close();
            } catch(DatabaseException dbe) {
                System.err.println("Error closing store: " +
                        dbe.toString());
                System.exit(-1);
            }
        }

        if (env != null) {
            try {
                // Finally, close environment.
                env.close();
            } catch(DatabaseException dbe) {
                System.err.println("Error closing env: " +
                        dbe.toString());
                System.exit(-1);
            }
        }
    }

    public static void addUser(String username, String password) {
        userAccessor.userByUsername.put(new User(username, password));
    }

    public static Optional<String> getUserPassword(String username) {
        return Optional.ofNullable(userAccessor.userByUsername.get(username)).map(user -> user.password);
    }

    public static void addDocument(URL url, String text, boolean isHtml) {
        documentAccessor.documentByUrl.put(new Document(url, text, isHtml));
    }

    public static Document getDocument(URL url) {
        return getDocument(url.toString());
    }

    public static Document getDocument(String url) {
        return documentAccessor.documentByUrl.get(url);
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

    /* The data accessor class for the entity model. */
    private static class DocumentAccessor {

        PrimaryIndex<String,Document> documentByUrl;

        DocumentAccessor(EntityStore store) throws DatabaseException {
            documentByUrl = store.getPrimaryIndex(String.class, Document.class);
        }
    }
	
}
