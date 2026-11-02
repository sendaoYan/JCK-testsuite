/*
 * @test
 * @summary jdk.dynalink.linker.MethodHandleTransformer conformance: interface reflection.
 * @run main MethodHandleTransformerTest
 */

import java.lang.reflect.*;

public class MethodHandleTransformerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("jdk.dynalink.linker.MethodHandleTransformer");
        check(c.isInterface(), "is interface");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
