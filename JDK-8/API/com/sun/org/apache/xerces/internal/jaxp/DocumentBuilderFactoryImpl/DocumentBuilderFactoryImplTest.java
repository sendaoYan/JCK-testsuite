/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl conformance: class hierarchy and members.
 * @run main DocumentBuilderFactoryImplTest
 */
public class DocumentBuilderFactoryImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl", false, DocumentBuilderFactoryImplTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("javax.xml.parsers.DocumentBuilderFactory"), "superclass javax.xml.parsers.DocumentBuilderFactory");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("newDocumentBuilder"), "method newDocumentBuilder");
        check(mn.contains("setAttribute"), "method setAttribute");
        check(mn.contains("getAttribute"), "method getAttribute");
        check(mn.contains("getSchema"), "method getSchema");
        check(mn.contains("setSchema"), "method setSchema");
        check(mn.contains("isXIncludeAware"), "method isXIncludeAware");
        check(mn.contains("setXIncludeAware"), "method setXIncludeAware");
        check(mn.contains("getFeature"), "method getFeature");
        check(mn.contains("setFeature"), "method setFeature");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
