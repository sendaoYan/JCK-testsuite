/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.parsers.SAXParser conformance: class hierarchy and members.
 * @run main SAXParserTest
 */
public class SAXParserTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.parsers.SAXParser", false, SAXParserTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.parsers.AbstractSAXParser"), "superclass com.sun.org.apache.xerces.internal.parsers.AbstractSAXParser");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("setProperty"), "method setProperty");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
