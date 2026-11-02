/*
 * @test
 * @summary org.w3c.dom.Entity conformance: interface extends Node.
 * @run main EntityTest
 */

import org.w3c.dom.Entity;
import org.w3c.dom.Node;

public class EntityTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(Entity.class.isInterface(), "Entity is an interface");
        check(Node.class.isAssignableFrom(Entity.class), "extends Node");
        check(Entity.class.getMethod("getPublicId") != null, "getPublicId()");
        check(Entity.class.getMethod("getSystemId") != null, "getSystemId()");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
