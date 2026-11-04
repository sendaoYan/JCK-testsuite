/*
 * @test
 * @summary com.sun.xml.internal.messaging.saaj.soap.impl.HeaderImpl conformance: abstract class hierarchy and members.
 * @run main HeaderImplTest
 */

public class HeaderImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.messaging.saaj.soap.impl.HeaderImpl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.messaging.saaj.soap.impl.HeaderImpl"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check("com.sun.xml.internal.messaging.saaj.soap.impl.ElementImpl".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.messaging.saaj.soap.impl.ElementImpl");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "direct interface count");
        check(ifs.contains("javax.xml.soap.SOAPHeader"), "has interface javax.xml.soap.SOAPHeader");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("addChildElement"), "has method addChildElement");
        check(mn.contains("addElement"), "has method addElement");
        check(mn.contains("addHeaderElement"), "has method addHeaderElement");
        check(mn.contains("addUpgradeHeaderElement"), "has method addUpgradeHeaderElement");
        check(mn.contains("convertToSoapElement"), "has method convertToSoapElement");
        check(mn.contains("createHeaderElement"), "has method createHeaderElement");
        check(mn.contains("examineAllHeaderElements"), "has method examineAllHeaderElements");
        check(mn.contains("examineHeaderElements"), "has method examineHeaderElements");
        check(mn.contains("examineMustUnderstandHeaderElements"), "has method examineMustUnderstandHeaderElements");
        check(mn.contains("extractAllHeaderElements"), "has method extractAllHeaderElements");
        check(mn.contains("extractHeaderElements"), "has method extractHeaderElements");
        check(mn.contains("getHeaderElements"), "has method getHeaderElements");
        check(mn.contains("getHeaderElementsForActor"), "has method getHeaderElementsForActor");
        check(mn.contains("getNotUnderstoodName"), "has method getNotUnderstoodName");
        check(mn.contains("getSupportedEnvelopeName"), "has method getSupportedEnvelopeName");
        check(mn.contains("getUpgradeName"), "has method getUpgradeName");
        check(mn.contains("iterate"), "has method iterate");
        check(mn.contains("setElementQName"), "has method setElementQName");
        check(mn.contains("setParentElement"), "has method setParentElement");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
