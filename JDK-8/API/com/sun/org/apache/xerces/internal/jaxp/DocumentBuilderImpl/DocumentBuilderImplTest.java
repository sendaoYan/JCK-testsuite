/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderImpl conformance: class hierarchy and members.
 * @run main DocumentBuilderImplTest
 */
public class DocumentBuilderImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderImpl", false, DocumentBuilderImplTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("javax.xml.parsers.DocumentBuilder"), "superclass javax.xml.parsers.DocumentBuilder");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.jaxp.JAXPConstants"), "has interface com.sun.org.apache.xerces.internal.jaxp.JAXPConstants");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("newDocument"), "method newDocument");
        check(mn.contains("getDOMImplementation"), "method getDOMImplementation");
        check(mn.contains("parse"), "method parse");
        check(mn.contains("isNamespaceAware"), "method isNamespaceAware");
        check(mn.contains("isValidating"), "method isValidating");
        check(mn.contains("isXIncludeAware"), "method isXIncludeAware");
        check(mn.contains("setEntityResolver"), "method setEntityResolver");
        check(mn.contains("setErrorHandler"), "method setErrorHandler");
        check(mn.contains("getSchema"), "method getSchema");
        check(mn.contains("reset"), "method reset");
        check(mn.contains("getDOMParser"), "method getDOMParser");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
