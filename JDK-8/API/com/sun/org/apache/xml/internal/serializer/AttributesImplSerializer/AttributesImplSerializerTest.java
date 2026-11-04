/*
 * @test
 * @summary com.sun.org.apache.xml.internal.serializer.AttributesImplSerializer conformance: class hierarchy and members.
 * @run main AttributesImplSerializerTest
 */
public class AttributesImplSerializerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.serializer.AttributesImplSerializer", false, AttributesImplSerializerTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("org.xml.sax.helpers.AttributesImpl"), "superclass org.xml.sax.helpers.AttributesImpl");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getIndex"), "method getIndex");
        check(mn.contains("addAttribute"), "method addAttribute");
        check(mn.contains("clear"), "method clear");
        check(mn.contains("setAttributes"), "method setAttributes");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
