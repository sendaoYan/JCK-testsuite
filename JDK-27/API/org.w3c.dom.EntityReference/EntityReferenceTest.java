/*
 * @test
 * @summary org.w3c.dom.EntityReference conformance: interface extends Node.
 * @run main EntityReferenceTest
 */

import org.w3c.dom.EntityReference;
import org.w3c.dom.Node;

public class EntityReferenceTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check(EntityReference.class.isInterface(), "EntityReference is an interface");
        check(Node.class.isAssignableFrom(EntityReference.class), "extends Node");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
