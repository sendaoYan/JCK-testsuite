/*
 * @test
 * @summary jdk.internal.org.objectweb.asm.Label conformance: exception/value behavior.
 * @run main LabelTest
 */

import java.lang.reflect.Method;

public class LabelTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.org.objectweb.asm.Label", false, LabelTest.class.getClassLoader());
        Object l = c.getConstructor().newInstance();
        check(c.getMethod("toString").invoke(l) != null, "toString non-null");
        try { c.getMethod("getOffset").invoke(l); check(false, "getOffset should throw"); }
        catch (java.lang.reflect.InvocationTargetException ex) { check(ex.getCause() instanceof IllegalStateException, "getOffset throws IllegalStateException"); }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
