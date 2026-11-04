/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.xs.XMLSchemaException conformance: class hierarchy and members.
 * @run main XMLSchemaExceptionTest
 */
public class XMLSchemaExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.xs.XMLSchemaException", false, XMLSchemaExceptionTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Exception"), "superclass java.lang.Exception");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getKey"), "method getKey");
        check(mn.contains("getArgs"), "method getArgs");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
