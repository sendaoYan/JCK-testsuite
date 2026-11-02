/*
 * @test
 * @summary com.sun.org.apache.xml.internal.res.XMLMessages conformance: class hierarchy and members.
 * @run main XMLMessagesTest
 */
public class XMLMessagesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.res.XMLMessages", false, XMLMessagesTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("setLocale"), "method setLocale");
        check(mn.contains("getLocale"), "method getLocale");
        check(mn.contains("createXMLMessage"), "method createXMLMessage");
        check(mn.contains("createMsg"), "method createMsg");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
