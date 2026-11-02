/*
 * @test
 * @summary org.w3c.dom.UserDataHandler conformance: interface and handle method.
 * @run main UserDataHandlerTest
 */

import org.w3c.dom.Node;
import org.w3c.dom.UserDataHandler;

public class UserDataHandlerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(UserDataHandler.class.isInterface(), "UserDataHandler is an interface");
        check(UserDataHandler.class.getMethod("handle", short.class, String.class,
                Object.class, Node.class, Node.class) != null,
              "handle(short,String,Object,Node,Node)");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
