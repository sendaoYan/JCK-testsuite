/*
 * @test
 * @summary jdk.internal.org.objectweb.asm.Type conformance: exception/value behavior.
 * @run main TypeTest
 */

import java.lang.reflect.Method;

public class TypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.org.objectweb.asm.Type", false, TypeTest.class.getClassLoader());
        Object tI = c.getMethod("getType", String.class).invoke(null, "I");
        check(((Integer) c.getMethod("getSort").invoke(tI)).intValue() == ((Integer) c.getField("INT").get(null)).intValue(), "sort INT");
        Object tO = c.getMethod("getObjectType", String.class).invoke(null, "java/lang/String");
        check("java.lang.String".equals(c.getMethod("getClassName").invoke(tO)), "getClassName");
        check("java/lang/String".equals(c.getMethod("getInternalName").invoke(tO)), "getInternalName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
