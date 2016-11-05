package test.edu.upenn.cis455;

import edu.upenn.cis455.storage.User;
import junit.framework.TestCase;

import java.util.Arrays;
import java.util.Collections;

public class UserTest extends TestCase {
    public void testGetChannelsAsList() throws Exception {
        User user = new User("piotr", "pwd");
        assertTrue(user.getChannelsAsList().isEmpty());
    }

    public void testAddChannel() throws Exception {
        User user = new User("piotr", "pwd");
        user.addChannel("foo");
        user.addChannel("bar");
        assertEquals(Arrays.asList("foo", "bar"), user.getChannelsAsList());
    }

    public void testRemoveChannel() throws Exception {
        User user = new User("piotr", "pwd");
        user.addChannel("foo");
        user.addChannel("bar");
        user.removeChannel("foo");
        assertEquals(Collections.singletonList("bar"), user.getChannelsAsList());
    }

}
