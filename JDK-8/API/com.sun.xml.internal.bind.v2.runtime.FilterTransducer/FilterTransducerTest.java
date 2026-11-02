/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.runtime.FilterTransducer conformance: abstract class hierarchy and members.
 * @run main FilterTransducerTest
 */

public class FilterTransducerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.runtime.FilterTransducer", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.runtime.FilterTransducer"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "direct interface count");
        check(ifs.contains("com.sun.xml.internal.bind.v2.runtime.Transducer"), "has interface com.sun.xml.internal.bind.v2.runtime.Transducer");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("declareNamespace"), "has method declareNamespace");
        check(mn.contains("getTypeName"), "has method getTypeName");
        check(mn.contains("isDefault"), "has method isDefault");
        check(mn.contains("parse"), "has method parse");
        check(mn.contains("print"), "has method print");
        check(mn.contains("useNamespace"), "has method useNamespace");
        check(mn.contains("writeLeafElement"), "has method writeLeafElement");
        check(mn.contains("writeText"), "has method writeText");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
