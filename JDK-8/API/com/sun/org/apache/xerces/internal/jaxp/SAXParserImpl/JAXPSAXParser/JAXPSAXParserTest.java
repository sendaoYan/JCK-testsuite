/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.jaxp.SAXParserImpl.JAXPSAXParser conformance: class hierarchy and members.
 * @run main JAXPSAXParserTest
 */
public class JAXPSAXParserTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.jaxp.SAXParserImpl$JAXPSAXParser", false, JAXPSAXParserTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.parsers.SAXParser"), "superclass com.sun.org.apache.xerces.internal.parsers.SAXParser");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("setFeature"), "method setFeature");
        check(mn.contains("getFeature"), "method getFeature");
        check(mn.contains("setProperty"), "method setProperty");
        check(mn.contains("getProperty"), "method getProperty");
        check(mn.contains("restoreInitState"), "method restoreInitState");
        check(mn.contains("parse"), "method parse");
        check(mn.contains("getXMLParserConfiguration"), "method getXMLParserConfiguration");
        check(mn.contains("setFeature0"), "method setFeature0");
        check(mn.contains("getFeature0"), "method getFeature0");
        check(mn.contains("setProperty0"), "method setProperty0");
        check(mn.contains("getProperty0"), "method getProperty0");
        check(mn.contains("getLocale"), "method getLocale");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
