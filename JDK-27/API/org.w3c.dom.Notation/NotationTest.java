/*
 * @test
 * @summary org.w3c.dom.Notation conformance: interface extends Node.
 * @run main NotationTest
 */

import org.w3c.dom.Node;
import org.w3c.dom.Notation;

public class NotationTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(Notation.class.isInterface(), "Notation is an interface");
        check(Node.class.isAssignableFrom(Notation.class), "extends Node");
        check(Notation.class.getMethod("getPublicId") != null, "getPublicId()");
        check(Notation.class.getMethod("getSystemId") != null, "getSystemId()");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
