/*
 * @test
 * @summary com.sun.xml.internal.messaging.saaj.soap.impl.FaultImpl conformance: abstract class hierarchy and members.
 * @run main FaultImplTest
 */

public class FaultImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.messaging.saaj.soap.impl.FaultImpl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.messaging.saaj.soap.impl.FaultImpl"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check("com.sun.xml.internal.messaging.saaj.soap.impl.ElementImpl".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.messaging.saaj.soap.impl.ElementImpl");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "direct interface count");
        check(ifs.contains("javax.xml.soap.SOAPFault"), "has interface javax.xml.soap.SOAPFault");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("addDetail"), "has method addDetail");
        check(mn.contains("addElement"), "has method addElement");
        check(mn.contains("addFaultActorElement"), "has method addFaultActorElement");
        check(mn.contains("addFaultCodeElement"), "has method addFaultCodeElement");
        check(mn.contains("addFaultStringElement"), "has method addFaultStringElement");
        check(mn.contains("addSOAPFaultElement"), "has method addSOAPFaultElement");
        check(mn.contains("checkIfStandardFaultCode"), "has method checkIfStandardFaultCode");
        check(mn.contains("convertCodeToQName"), "has method convertCodeToQName");
        check(mn.contains("convertToSoapElement"), "has method convertToSoapElement");
        check(mn.contains("createDetail"), "has method createDetail");
        check(mn.contains("createSOAPFaultElement"), "has method createSOAPFaultElement");
        check(mn.contains("finallySetFaultCode"), "has method finallySetFaultCode");
        check(mn.contains("findFaultActorElement"), "has method findFaultActorElement");
        check(mn.contains("findFaultCodeElement"), "has method findFaultCodeElement");
        check(mn.contains("findFaultStringElement"), "has method findFaultStringElement");
        check(mn.contains("getDefaultFaultCode"), "has method getDefaultFaultCode");
        check(mn.contains("getDetail"), "has method getDetail");
        check(mn.contains("getDetailName"), "has method getDetailName");
        check(mn.contains("getFaultActor"), "has method getFaultActor");
        check(mn.contains("getFaultActorName"), "has method getFaultActorName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
