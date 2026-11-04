/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.dom.EntityImpl conformance: class hierarchy and members.
 * @run main EntityImplTest
 */
public class EntityImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.dom.EntityImpl", false, EntityImplTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.dom.ParentNode"), "superclass com.sun.org.apache.xerces.internal.dom.ParentNode");
        check(hasInterface(c, "org.w3c.dom.Entity"), "has interface org.w3c.dom.Entity");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getNodeType"), "method getNodeType");
        check(mn.contains("getNodeName"), "method getNodeName");
        check(mn.contains("setNodeValue"), "method setNodeValue");
        check(mn.contains("setPrefix"), "method setPrefix");
        check(mn.contains("cloneNode"), "method cloneNode");
        check(mn.contains("getPublicId"), "method getPublicId");
        check(mn.contains("getSystemId"), "method getSystemId");
        check(mn.contains("getXmlVersion"), "method getXmlVersion");
        check(mn.contains("getXmlEncoding"), "method getXmlEncoding");
        check(mn.contains("getNotationName"), "method getNotationName");
        check(mn.contains("setPublicId"), "method setPublicId");
        check(mn.contains("setXmlEncoding"), "method setXmlEncoding");
        check(mn.contains("getInputEncoding"), "method getInputEncoding");
        check(mn.contains("setInputEncoding"), "method setInputEncoding");
        check(mn.contains("setXmlVersion"), "method setXmlVersion");
        check(mn.contains("setSystemId"), "method setSystemId");
        check(mn.contains("setNotationName"), "method setNotationName");
        check(mn.contains("getBaseURI"), "method getBaseURI");
        check(mn.contains("setBaseURI"), "method setBaseURI");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
