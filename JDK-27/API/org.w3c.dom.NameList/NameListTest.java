/*
 * @test
 * @summary org.w3c.dom.NameList conformance: interface and methods.
 * @run main NameListTest
 */

import org.w3c.dom.NameList;

public class NameListTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(NameList.class.isInterface(), "NameList is an interface");
        check(NameList.class.getMethod("getName", int.class)
                .getReturnType() == String.class, "getName(int) returns String");
        check(NameList.class.getMethod("getNamespaceURI", int.class)
                .getReturnType() == String.class, "getNamespaceURI(int) returns String");
        check(NameList.class.getMethod("getLength").getReturnType() == int.class,
              "getLength() returns int");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
