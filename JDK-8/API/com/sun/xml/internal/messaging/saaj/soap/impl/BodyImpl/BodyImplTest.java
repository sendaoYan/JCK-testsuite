/*
 * @test
 * @summary com.sun.xml.internal.messaging.saaj.soap.impl.BodyImpl conformance: abstract class hierarchy and members.
 * @run main BodyImplTest
 */

public class BodyImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.messaging.saaj.soap.impl.BodyImpl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.messaging.saaj.soap.impl.BodyImpl"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check("com.sun.xml.internal.messaging.saaj.soap.impl.ElementImpl".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.messaging.saaj.soap.impl.ElementImpl");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "direct interface count");
        check(ifs.contains("javax.xml.soap.SOAPBody"), "has interface javax.xml.soap.SOAPBody");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("addBodyElement"), "has method addBodyElement");
        check(mn.contains("addDocument"), "has method addDocument");
        check(mn.contains("addElement"), "has method addElement");
        check(mn.contains("addFault"), "has method addFault");
        check(mn.contains("convertToSoapElement"), "has method convertToSoapElement");
        check(mn.contains("createBodyElement"), "has method createBodyElement");
        check(mn.contains("createFaultElement"), "has method createFaultElement");
        check(mn.contains("extractContentAsDocument"), "has method extractContentAsDocument");
        check(mn.contains("findFault"), "has method findFault");
        check(mn.contains("getDefaultFaultCode"), "has method getDefaultFaultCode");
        check(mn.contains("getFault"), "has method getFault");
        check(mn.contains("getFaultName"), "has method getFaultName");
        check(mn.contains("hasFault"), "has method hasFault");
        check(mn.contains("initializeFault"), "has method initializeFault");
        check(mn.contains("isFault"), "has method isFault");
        check(mn.contains("setElementQName"), "has method setElementQName");
        check(mn.contains("setParentElement"), "has method setParentElement");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
