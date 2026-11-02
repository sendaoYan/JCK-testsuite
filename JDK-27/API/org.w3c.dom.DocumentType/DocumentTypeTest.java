/*
 * @test
 * @summary org.w3c.dom.DocumentType conformance: interface extends Node.
 * @run main DocumentTypeTest
 */

import org.w3c.dom.DocumentType;
import org.w3c.dom.Node;

public class DocumentTypeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(DocumentType.class.isInterface(), "DocumentType is an interface");
        check(Node.class.isAssignableFrom(DocumentType.class), "extends Node");
        check(DocumentType.class.getMethod("getName") != null, "getName()");
        check(DocumentType.class.getMethod("getEntities") != null, "getEntities()");
        check(DocumentType.class.getMethod("getNotations") != null, "getNotations()");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
