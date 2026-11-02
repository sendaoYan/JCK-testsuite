/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.util.AttributesProxy conformance: class hierarchy and members.
 * @run main AttributesProxyTest
 */
public class AttributesProxyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.util.AttributesProxy", false, AttributesProxyTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "org.xml.sax.AttributeList"), "has interface org.xml.sax.AttributeList");
        check(hasInterface(c, "org.xml.sax.ext.Attributes2"), "has interface org.xml.sax.ext.Attributes2");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("setAttributes"), "method setAttributes");
        check(mn.contains("getAttributes"), "method getAttributes");
        check(mn.contains("getLength"), "method getLength");
        check(mn.contains("getQName"), "method getQName");
        check(mn.contains("getURI"), "method getURI");
        check(mn.contains("getLocalName"), "method getLocalName");
        check(mn.contains("getType"), "method getType");
        check(mn.contains("getValue"), "method getValue");
        check(mn.contains("getIndex"), "method getIndex");
        check(mn.contains("isDeclared"), "method isDeclared");
        check(mn.contains("isSpecified"), "method isSpecified");
        check(mn.contains("getName"), "method getName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
