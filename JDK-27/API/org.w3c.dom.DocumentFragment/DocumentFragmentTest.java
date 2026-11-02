/*
 * @test
 * @summary org.w3c.dom.DocumentFragment conformance: interface extends Node.
 * @run main DocumentFragmentTest
 */

import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;

public class DocumentFragmentTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check(DocumentFragment.class.isInterface(), "DocumentFragment is an interface");
        check(Node.class.isAssignableFrom(DocumentFragment.class), "extends Node");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
