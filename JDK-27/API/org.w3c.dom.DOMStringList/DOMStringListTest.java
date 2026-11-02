/*
 * @test
 * @summary org.w3c.dom.DOMStringList conformance: interface and methods.
 * @run main DOMStringListTest
 */

import org.w3c.dom.DOMStringList;

public class DOMStringListTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(DOMStringList.class.isInterface(), "DOMStringList is an interface");
        check(DOMStringList.class.getMethod("item", int.class)
                .getReturnType() == String.class, "item(int) returns String");
        check(DOMStringList.class.getMethod("getLength").getReturnType() == int.class,
              "getLength() returns int");
        check(DOMStringList.class.getMethod("contains", String.class)
                .getReturnType() == boolean.class, "contains(String) returns boolean");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
