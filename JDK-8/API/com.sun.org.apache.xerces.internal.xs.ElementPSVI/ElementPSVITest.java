/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.xs.ElementPSVI conformance: interface hierarchy and members.
 * @run main ElementPSVITest
 */
public class ElementPSVITest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.xs.ElementPSVI", false, ElementPSVITest.class.getClassLoader());
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xs.ItemPSVI"), "has interface com.sun.org.apache.xerces.internal.xs.ItemPSVI");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getElementDeclaration"), "method getElementDeclaration");
        check(mn.contains("getNotation"), "method getNotation");
        check(mn.contains("getNil"), "method getNil");
        check(mn.contains("getSchemaInformation"), "method getSchemaInformation");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
