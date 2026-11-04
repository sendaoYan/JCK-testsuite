/*
 * @test
 * @summary com.sun.xml.internal.messaging.saaj.soap.impl.HeaderElementImpl conformance: abstract class hierarchy and members.
 * @run main HeaderElementImplTest
 */

public class HeaderElementImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.messaging.saaj.soap.impl.HeaderElementImpl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.messaging.saaj.soap.impl.HeaderElementImpl"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check("com.sun.xml.internal.messaging.saaj.soap.impl.ElementImpl".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.messaging.saaj.soap.impl.ElementImpl");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "direct interface count");
        check(ifs.contains("javax.xml.soap.SOAPHeaderElement"), "has interface javax.xml.soap.SOAPHeaderElement");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getActor"), "has method getActor");
        check(mn.contains("getActorAttributeName"), "has method getActorAttributeName");
        check(mn.contains("getActorOrRole"), "has method getActorOrRole");
        check(mn.contains("getMustUnderstand"), "has method getMustUnderstand");
        check(mn.contains("getMustunderstandAttributeName"), "has method getMustunderstandAttributeName");
        check(mn.contains("getMustunderstandAttributeValue"), "has method getMustunderstandAttributeValue");
        check(mn.contains("getMustunderstandLiteralValue"), "has method getMustunderstandLiteralValue");
        check(mn.contains("getRelay"), "has method getRelay");
        check(mn.contains("getRelayAttributeName"), "has method getRelayAttributeName");
        check(mn.contains("getRelayAttributeValue"), "has method getRelayAttributeValue");
        check(mn.contains("getRelayLiteralValue"), "has method getRelayLiteralValue");
        check(mn.contains("getRole"), "has method getRole");
        check(mn.contains("getRoleAttributeName"), "has method getRoleAttributeName");
        check(mn.contains("setActor"), "has method setActor");
        check(mn.contains("setMustUnderstand"), "has method setMustUnderstand");
        check(mn.contains("setParentElement"), "has method setParentElement");
        check(mn.contains("setRelay"), "has method setRelay");
        check(mn.contains("setRole"), "has method setRole");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
