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

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        check(UserDataHandler.class.isInterface(), "UserDataHandler is an interface");
        check(UserDataHandler.class.getMethod("handle", short.class, String.class,
                Object.class, Node.class, Node.class) != null,
              "handle(short,String,Object,Node,Node)");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(org.w3c.dom.UserDataHandler.NODE_CLONED == 1, "NODE_CLONED == 1");
        check(org.w3c.dom.UserDataHandler.NODE_IMPORTED == 2, "NODE_IMPORTED == 2");
        check(org.w3c.dom.UserDataHandler.NODE_DELETED == 3, "NODE_DELETED == 3");
        check(org.w3c.dom.UserDataHandler.NODE_RENAMED == 4, "NODE_RENAMED == 4");
        check(org.w3c.dom.UserDataHandler.NODE_ADOPTED == 5, "NODE_ADOPTED == 5");
        org.w3c.dom.UserDataHandler a = new org.w3c.dom.UserDataHandler(){
            public void handle(short p0, java.lang.String p1, java.lang.Object p2, org.w3c.dom.Node p3, org.w3c.dom.Node p4) { }
        };
        check(a != null, "anon impl of UserDataHandler");
        a.handle((short)2, "s", (java.lang.Object) null, (org.w3c.dom.Node) null, (org.w3c.dom.Node) null);
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
