/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl conformance: class hierarchy and members.
 * @run main SAXParserFactoryImplTest
 */
public class SAXParserFactoryImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl", false, SAXParserFactoryImplTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("javax.xml.parsers.SAXParserFactory"), "superclass javax.xml.parsers.SAXParserFactory");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("newSAXParser"), "method newSAXParser");
        check(mn.contains("setFeature"), "method setFeature");
        check(mn.contains("getFeature"), "method getFeature");
        check(mn.contains("getSchema"), "method getSchema");
        check(mn.contains("setSchema"), "method setSchema");
        check(mn.contains("isXIncludeAware"), "method isXIncludeAware");
        check(mn.contains("setXIncludeAware"), "method setXIncludeAware");
        check(mn.contains("setValidating"), "method setValidating");
        check(mn.contains("isValidating"), "method isValidating");
        check(mn.contains("isNamespaceAware"), "method isNamespaceAware");
        check(mn.contains("setNamespaceAware"), "method setNamespaceAware");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
