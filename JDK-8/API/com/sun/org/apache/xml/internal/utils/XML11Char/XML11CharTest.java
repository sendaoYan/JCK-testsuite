/*
 * @test
 * @summary com.sun.org.apache.xml.internal.utils.XML11Char conformance: class hierarchy and members.
 * @run main XML11CharTest
 */
public class XML11CharTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.utils.XML11Char", false, XML11CharTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("isXML11Space"), "method isXML11Space");
        check(mn.contains("isXML11Valid"), "method isXML11Valid");
        check(mn.contains("isXML11Invalid"), "method isXML11Invalid");
        check(mn.contains("isXML11ValidLiteral"), "method isXML11ValidLiteral");
        check(mn.contains("isXML11Content"), "method isXML11Content");
        check(mn.contains("isXML11InternalEntityContent"), "method isXML11InternalEntityContent");
        check(mn.contains("isXML11NameStart"), "method isXML11NameStart");
        check(mn.contains("isXML11Name"), "method isXML11Name");
        check(mn.contains("isXML11NCNameStart"), "method isXML11NCNameStart");
        check(mn.contains("isXML11NCName"), "method isXML11NCName");
        check(mn.contains("isXML11NameHighSurrogate"), "method isXML11NameHighSurrogate");
        check(mn.contains("isXML11ValidName"), "method isXML11ValidName");
        check(mn.contains("isXML11ValidNCName"), "method isXML11ValidNCName");
        check(mn.contains("isXML11ValidNmtoken"), "method isXML11ValidNmtoken");
        check(mn.contains("isXML11ValidQName"), "method isXML11ValidQName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
