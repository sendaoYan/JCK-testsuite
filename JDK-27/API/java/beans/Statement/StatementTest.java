/*
 * @test
 * @summary java.beans.Statement conformance: white-box structure + black-box behavior.
 * @run main StatementTest
 */

import java.beans.Statement;

public class StatementTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.Statement");
        check(!c.isInterface(), "is a class");
    }

    static void testBlackBox() throws Exception {
        java.util.List<String> list = new java.util.ArrayList<>();
        Statement s = new Statement(list, "add", new Object[]{"x"});
        check(list == s.getTarget(), "target");
        check("add".equals(s.getMethodName()), "methodName");
        check(s.getArguments() != null && s.getArguments().length == 1, "arguments");
        s.execute();
        check(list.contains("x"), "execute applies side effect");

        Statement s2 = new Statement(list, "clear", new Object[0]);
        s2.execute();
        check(list.isEmpty(), "execute clear");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
