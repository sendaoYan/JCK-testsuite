/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.xni.parser.XMLErrorHandler conformance: interface hierarchy and members.
 * @run main XMLErrorHandlerTest
 */
public class XMLErrorHandlerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.xni.parser.XMLErrorHandler", false, XMLErrorHandlerTest.class.getClassLoader());
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("warning"), "method warning");
        check(mn.contains("error"), "method error");
        check(mn.contains("fatalError"), "method fatalError");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
