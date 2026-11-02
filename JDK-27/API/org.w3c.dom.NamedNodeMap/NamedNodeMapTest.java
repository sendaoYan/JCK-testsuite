/*
 * @test
 * @summary org.w3c.dom.NamedNodeMap conformance: interface and methods.
 * @run main NamedNodeMapTest
 */

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class NamedNodeMapTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(NamedNodeMap.class.isInterface(), "NamedNodeMap is an interface");
        check(NamedNodeMap.class.getMethod("getLength").getReturnType() == int.class,
              "getLength() returns int");
        check(NamedNodeMap.class.getMethod("item", int.class).getReturnType() == Node.class,
              "item(int) returns Node");
        check(NamedNodeMap.class.getMethod("getNamedItem", String.class) != null,
              "getNamedItem(String)");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
