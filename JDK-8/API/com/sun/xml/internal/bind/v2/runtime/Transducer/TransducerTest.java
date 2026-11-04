/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.runtime.Transducer conformance: interface hierarchy and members.
 * @run main TransducerTest
 */

public class TransducerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.runtime.Transducer", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.runtime.Transducer"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
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
