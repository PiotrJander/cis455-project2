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

public class DBWrapper {
	
	private static String envDirectory = null;
	
	private static Environment env;
	private static EntityStore store;
    private static UserAccessor dao;
	
	/* TODO: write object store wrapper for BerkeleyDB */

    public static void init(File envHome) throws DatabaseException {

        /* Open a transactional Berkeley DB engine environment. */
        EnvironmentConfig envConfig = new EnvironmentConfig();
//        envConfig.setAllowCreate(true);
//        envConfig.setTransactional(true);
        env = new Environment(envHome, envConfig);

        /* Open a transactional entity store. */
        StoreConfig storeConfig = new StoreConfig();
//        storeConfig.setAllowCreate(true);
//        storeConfig.setTransactional(true);
        store = new EntityStore(env, "UserStore", storeConfig);

        /* Initialize the data access object. */
        dao = new UserAccessor(store);
    }

    /* An entity class. */
    @Entity
    static class User {

        @PrimaryKey
        String username;

        String password;

        public User(String username, String password) {
            this.username = username;
            this.password = password;
        }

        private User() {} // For deserialization
    }

    /* The data accessor class for the entity model. */
    static class UserAccessor {

        PrimaryIndex<String,User> userByUsername;

        public UserAccessor(EntityStore store) throws DatabaseException {
            userByUsername = store.getPrimaryIndex(String.class, User.class);
        }
    }
	
}
