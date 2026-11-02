/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.jaxp.SAXParserImpl conformance: class hierarchy and members.
 * @run main SAXParserImplTest
 */
public class SAXParserImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.jaxp.SAXParserImpl", false, SAXParserImplTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("javax.xml.parsers.SAXParser"), "superclass javax.xml.parsers.SAXParser");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.jaxp.JAXPConstants"), "has interface com.sun.org.apache.xerces.internal.jaxp.JAXPConstants");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xs.PSVIProvider"), "has interface com.sun.org.apache.xerces.internal.xs.PSVIProvider");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getParser"), "method getParser");
        check(mn.contains("getXMLReader"), "method getXMLReader");
        check(mn.contains("isNamespaceAware"), "method isNamespaceAware");
        check(mn.contains("isValidating"), "method isValidating");
        check(mn.contains("isXIncludeAware"), "method isXIncludeAware");
        check(mn.contains("setProperty"), "method setProperty");
        check(mn.contains("getProperty"), "method getProperty");
        check(mn.contains("parse"), "method parse");
        check(mn.contains("getSchema"), "method getSchema");
        check(mn.contains("reset"), "method reset");
        check(mn.contains("getElementPSVI"), "method getElementPSVI");
        check(mn.contains("getAttributePSVI"), "method getAttributePSVI");
        check(mn.contains("getAttributePSVIByName"), "method getAttributePSVIByName");
        check(mn.contains("access$000"), "method access$000");
        check(mn.contains("access$100"), "method access$100");
        check(mn.contains("access$202"), "method access$202");
        check(mn.contains("access$200"), "method access$200");
        check(mn.contains("access$300"), "method access$300");
        check(mn.contains("access$400"), "method access$400");
        check(mn.contains("access$500"), "method access$500");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
