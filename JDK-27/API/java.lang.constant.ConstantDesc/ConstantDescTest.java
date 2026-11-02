/*
 * @test
 * @summary java.lang.constant.ConstantDesc conformance: resolveConstantDesc on
 *          built-in ConstantDesc types.
 * @run main ConstantDescTest
 */

import java.lang.constant.ConstantDesc;
import java.lang.invoke.MethodHandles;

public class ConstantDescTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(ConstantDesc.class.isInterface(), "ConstantDesc is an interface");

        ConstantDesc s = "constant";
        check("constant".equals(s.resolveConstantDesc(MethodHandles.lookup())),
              "String resolves to itself");

        ConstantDesc n = Integer.valueOf(42);
        check(Integer.valueOf(42).equals(n.resolveConstantDesc(MethodHandles.lookup())),
              "Integer resolves to itself");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
