/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.dtd.XML11NSDTDValidator conformance: class hierarchy and members.
 * @run main XML11NSDTDValidatorTest
 */
public class XML11NSDTDValidatorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.dtd.XML11NSDTDValidator", false, XML11NSDTDValidatorTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.impl.dtd.XML11DTDValidator"), "superclass com.sun.org.apache.xerces.internal.impl.dtd.XML11DTDValidator");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("startNamespaceScope"), "method startNamespaceScope");
        check(mn.contains("endNamespaceScope"), "method endNamespaceScope");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
