/*
 * @test
 * @summary com.sun.org.apache.xml.internal.dtm.DTMManager conformance: abstract class hierarchy and members.
 * @run main DTMManagerTest
 */
public class DTMManagerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.dtm.DTMManager", false, DTMManagerTest.class.getClassLoader());
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getXMLStringFactory"), "method getXMLStringFactory");
        check(mn.contains("setXMLStringFactory"), "method setXMLStringFactory");
        check(mn.contains("newInstance"), "method newInstance");
        check(mn.contains("getDTM"), "method getDTM");
        check(mn.contains("getDTMHandleFromNode"), "method getDTMHandleFromNode");
        check(mn.contains("createDocumentFragment"), "method createDocumentFragment");
        check(mn.contains("release"), "method release");
        check(mn.contains("createDTMIterator"), "method createDTMIterator");
        check(mn.contains("getIncremental"), "method getIncremental");
        check(mn.contains("setIncremental"), "method setIncremental");
        check(mn.contains("getSource_location"), "method getSource_location");
        check(mn.contains("setSource_location"), "method setSource_location");
        check(mn.contains("overrideDefaultParser"), "method overrideDefaultParser");
        check(mn.contains("setOverrideDefaultParser"), "method setOverrideDefaultParser");
        check(mn.contains("getDTMIdentity"), "method getDTMIdentity");
        check(mn.contains("getDTMIdentityMask"), "method getDTMIdentityMask");
        check(mn.contains("getNodeIdentityMask"), "method getNodeIdentityMask");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
