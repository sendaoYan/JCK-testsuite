/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.runtime.reflect.opt.TransducedAccessor_field_Byte conformance: class hierarchy and members.
 * @run main TransducedAccessor_field_ByteTest
 */

public class TransducedAccessor_field_ByteTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.runtime.reflect.opt.TransducedAccessor_field_Byte", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.runtime.reflect.opt.TransducedAccessor_field_Byte"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("com.sun.xml.internal.bind.v2.runtime.reflect.DefaultTransducedAccessor".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.bind.v2.runtime.reflect.DefaultTransducedAccessor");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("hasValue"), "has method hasValue");
        check(mn.contains("parse"), "has method parse");
        check(mn.contains("print"), "has method print");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
