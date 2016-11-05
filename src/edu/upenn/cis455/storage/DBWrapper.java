package edu.upenn.cis455.storage;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;

import java.io.File;
import java.net.URL;

public class DBWrapper {
	
	private static String envDirectory = null;
	
	private static Environment env;
	private static EntityStore store;
    private static UserAccessor userAccessor;
    private static DocumentAccessor documentAccessor;
    private static ChannelAccessor channelAccessor;

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
        channelAccessor = new ChannelAccessor(store);
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

    // START User

    public static void addUser(User user) {
        userAccessor.userByUsername.put(user);
    }

    public static void addUser(String username, String password) {
        userAccessor.userByUsername.put(new User(username, password));
    }

    public static User getUser(String username) {
        return userAccessor.userByUsername.get(username);
    }

    public static void addChannelToUser(String username, String channelName) {
        User user = getUser(username);
        user.addChannel(channelName);
        addUser(user);
    }

    public static void removeChannelFromUser(String username, String channelName) {
        User user = getUser(username);
        user.removeChannel(channelName);
        addUser(user);
    }

    public static void addDocument(URL url, String text, String contentType) {
        documentAccessor.documentByUrl.put(new Document(url, text, contentType));
    }

    // END User

    // START Document

    public static Document getDocument(URL url) {
        return getDocument(url.toString());
    }

    public static Document getDocument(String url) {
        return documentAccessor.documentByUrl.get(url);
    }

    public static void addChannel(Channel channel) {
        channelAccessor.channelByName.put(channel);
    }

    public static Channel getChannel(String name) {
        return channelAccessor.channelByName.get(name);
    }

    // END Document

    // START Channel

    public static void addDocumentToChannel(String channelName, String url) {
        Channel channel = getChannel(channelName);
        channel.addDocument(url);
        addChannel(channel);
    }

    public static void removeDocument(String username, String channelName) {
        User user = getUser(username);
        user.removeChannel(channelName);
        addUser(user);
    }

    private static class UserAccessor {

        PrimaryIndex<String,User> userByUsername;

        UserAccessor(EntityStore store) throws DatabaseException {
            userByUsername = store.getPrimaryIndex(String.class, User.class);
        }
    }

    private static class DocumentAccessor {

        PrimaryIndex<String,Document> documentByUrl;

        DocumentAccessor(EntityStore store) throws DatabaseException {
            documentByUrl = store.getPrimaryIndex(String.class, Document.class);
        }
    }

    private static class ChannelAccessor {

        PrimaryIndex<String, Channel> channelByName;

        ChannelAccessor(EntityStore store) throws DatabaseException {
            channelByName = store.getPrimaryIndex(String.class, Channel.class);
        }
    }

    // END Channel

}
