/*
 * @test
 * @summary jdk.incubator.vector.VectorOperators.Test conformance: test operator interface.
 * @modules jdk.incubator.vector
 * @run main TestTest
 */

import java.lang.reflect.*;

public class TestTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("jdk.incubator.vector.VectorOperators$Test");
        check(c.isInterface(), "is interface");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        check(jdk.incubator.vector.VectorOperators.Operator.class.isAssignableFrom(c), "extends Operator");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
