/*
 * @test
 * @summary com.sun.xml.internal.messaging.saaj.soap.ver1_1.Fault1_1Impl conformance: class hierarchy and members.
 * @run main Fault1_1ImplTest
 */

public class Fault1_1ImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.messaging.saaj.soap.ver1_1.Fault1_1Impl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.messaging.saaj.soap.ver1_1.Fault1_1Impl"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("com.sun.xml.internal.messaging.saaj.soap.impl.FaultImpl".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.messaging.saaj.soap.impl.FaultImpl");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("addChildElement"), "has method addChildElement");
        check(mn.contains("addFaultReasonText"), "has method addFaultReasonText");
        check(mn.contains("appendFaultSubcode"), "has method appendFaultSubcode");
        check(mn.contains("checkIfStandardFaultCode"), "has method checkIfStandardFaultCode");
        check(mn.contains("createDetail"), "has method createDetail");
        check(mn.contains("createSOAPFaultElement"), "has method createSOAPFaultElement");
        check(mn.contains("finallySetFaultCode"), "has method finallySetFaultCode");
        check(mn.contains("getDefaultFaultCode"), "has method getDefaultFaultCode");
        check(mn.contains("getDetailName"), "has method getDetailName");
        check(mn.contains("getFaultActorName"), "has method getFaultActorName");
        check(mn.contains("getFaultCode"), "has method getFaultCode");
        check(mn.contains("getFaultCodeAsName"), "has method getFaultCodeAsName");
        check(mn.contains("getFaultCodeAsQName"), "has method getFaultCodeAsQName");
        check(mn.contains("getFaultCodeName"), "has method getFaultCodeName");
        check(mn.contains("getFaultNode"), "has method getFaultNode");
        check(mn.contains("getFaultReasonLocales"), "has method getFaultReasonLocales");
        check(mn.contains("getFaultReasonText"), "has method getFaultReasonText");
        check(mn.contains("getFaultReasonTexts"), "has method getFaultReasonTexts");
        check(mn.contains("getFaultRole"), "has method getFaultRole");
        check(mn.contains("getFaultString"), "has method getFaultString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
