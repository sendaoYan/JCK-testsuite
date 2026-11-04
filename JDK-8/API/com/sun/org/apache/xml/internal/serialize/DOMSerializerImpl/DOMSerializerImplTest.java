/*
 * @test
 * @summary com.sun.org.apache.xml.internal.serialize.DOMSerializerImpl conformance: class hierarchy and members.
 * @run main DOMSerializerImplTest
 */
public class DOMSerializerImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.serialize.DOMSerializerImpl", false, DOMSerializerImplTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "org.w3c.dom.ls.LSSerializer"), "has interface org.w3c.dom.ls.LSSerializer");
        check(hasInterface(c, "org.w3c.dom.DOMConfiguration"), "has interface org.w3c.dom.DOMConfiguration");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getDomConfig"), "method getDomConfig");
        check(mn.contains("setParameter"), "method setParameter");
        check(mn.contains("canSetParameter"), "method canSetParameter");
        check(mn.contains("getParameterNames"), "method getParameterNames");
        check(mn.contains("getParameter"), "method getParameter");
        check(mn.contains("writeToString"), "method writeToString");
        check(mn.contains("setNewLine"), "method setNewLine");
        check(mn.contains("getNewLine"), "method getNewLine");
        check(mn.contains("getFilter"), "method getFilter");
        check(mn.contains("setFilter"), "method setFilter");
        check(mn.contains("write"), "method write");
        check(mn.contains("writeToURI"), "method writeToURI");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
