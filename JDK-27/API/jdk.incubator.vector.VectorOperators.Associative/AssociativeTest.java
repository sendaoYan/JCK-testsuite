/*
 * @test
 * @summary jdk.incubator.vector.VectorOperators.Associative conformance: associative operator interface.
 * @modules jdk.incubator.vector
 * @run main AssociativeTest
 */

import java.lang.reflect.*;

public class AssociativeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("jdk.incubator.vector.VectorOperators$Associative");
        check(c.isInterface(), "is interface");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        check(jdk.incubator.vector.VectorOperators.Binary.class.isAssignableFrom(c), "extends Binary");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
