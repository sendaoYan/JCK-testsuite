/*
 * @test
 * @summary java.beans.Statement conformance: execute a method on a target.
 * @run main StatementTest
 */

import java.beans.*;

public class StatementTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) throws Exception {
        java.util.List<String> list = new java.util.ArrayList<>();
        Statement s = new Statement(list, "add", new Object[]{"x"});
        check(s.getTarget() == list, "target");
        check("add".equals(s.getMethodName()), "method name");
        check(s.getArguments().length == 1, "arguments");
        s.execute();
        check(list.size() == 1, "size after execute");
        check("x".equals(list.get(0)), "added value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
