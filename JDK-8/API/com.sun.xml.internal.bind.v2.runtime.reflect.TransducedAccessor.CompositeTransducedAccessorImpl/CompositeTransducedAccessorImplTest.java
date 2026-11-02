/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.runtime.reflect.TransducedAccessor.CompositeTransducedAccessorImpl conformance: class hierarchy and members.
 * @run main CompositeTransducedAccessorImplTest
 */

public class CompositeTransducedAccessorImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.runtime.reflect.TransducedAccessor$CompositeTransducedAccessorImpl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.runtime.reflect.TransducedAccessor$CompositeTransducedAccessorImpl"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("com.sun.xml.internal.bind.v2.runtime.reflect.TransducedAccessor".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.bind.v2.runtime.reflect.TransducedAccessor");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("hasValue"), "has method hasValue");
        check(mn.contains("parse"), "has method parse");
        check(mn.contains("print"), "has method print");
        check(mn.contains("writeLeafElement"), "has method writeLeafElement");
        check(mn.contains("writeText"), "has method writeText");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
