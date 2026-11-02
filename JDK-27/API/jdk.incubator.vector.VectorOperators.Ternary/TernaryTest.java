/*
 * @test
 * @summary jdk.incubator.vector.VectorOperators.Ternary conformance: ternary operator interface.
 * @modules jdk.incubator.vector
 * @run main TernaryTest
 */

import java.lang.reflect.*;

public class TernaryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("jdk.incubator.vector.VectorOperators$Ternary");
        check(c.isInterface(), "is interface");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        check(jdk.incubator.vector.VectorOperators.Operator.class.isAssignableFrom(c), "extends Operator");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
