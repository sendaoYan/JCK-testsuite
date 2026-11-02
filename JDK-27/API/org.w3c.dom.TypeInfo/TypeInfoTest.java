/*
 * @test
 * @summary org.w3c.dom.TypeInfo conformance: interface and methods.
 * @run main TypeInfoTest
 */

import org.w3c.dom.TypeInfo;

public class TypeInfoTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(TypeInfo.class.isInterface(), "TypeInfo is an interface");
        check(TypeInfo.class.getMethod("getTypeName") != null, "getTypeName()");
        check(TypeInfo.class.getMethod("getTypeNamespace") != null, "getTypeNamespace()");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
