/*
 * @test
 * @summary org.w3c.dom.Attr conformance: interface extends Node.
 * @run main AttrTest
 */

import org.w3c.dom.Attr;
import org.w3c.dom.Node;

public class AttrTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(Attr.class.isInterface(), "Attr is an interface");
        check(Node.class.isAssignableFrom(Attr.class), "extends Node");
        check(Attr.class.getMethod("getName") != null, "getName()");
        check(Attr.class.getMethod("getValue") != null, "getValue()");
        check(Attr.class.getMethod("getOwnerElement") != null, "getOwnerElement()");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
