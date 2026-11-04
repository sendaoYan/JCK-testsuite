/*
 * @test
 * @summary jdk.incubator.vector.VectorOperators.Operator conformance: operator interface.
 * @modules jdk.incubator.vector
 * @run main OperatorTest
 */

import java.lang.reflect.*;

public class OperatorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("jdk.incubator.vector.VectorOperators$Operator");
        check(c.isInterface(), "is interface");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
