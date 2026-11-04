/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.jaxp.validation.XMLSchemaFactory conformance: class hierarchy and members.
 * @run main XMLSchemaFactoryTest
 */
public class XMLSchemaFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.jaxp.validation.XMLSchemaFactory", false, XMLSchemaFactoryTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("javax.xml.validation.SchemaFactory"), "superclass javax.xml.validation.SchemaFactory");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("isSchemaLanguageSupported"), "method isSchemaLanguageSupported");
        check(mn.contains("getResourceResolver"), "method getResourceResolver");
        check(mn.contains("setResourceResolver"), "method setResourceResolver");
        check(mn.contains("getErrorHandler"), "method getErrorHandler");
        check(mn.contains("setErrorHandler"), "method setErrorHandler");
        check(mn.contains("newSchema"), "method newSchema");
        check(mn.contains("getFeature"), "method getFeature");
        check(mn.contains("getProperty"), "method getProperty");
        check(mn.contains("setFeature"), "method setFeature");
        check(mn.contains("setProperty"), "method setProperty");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
