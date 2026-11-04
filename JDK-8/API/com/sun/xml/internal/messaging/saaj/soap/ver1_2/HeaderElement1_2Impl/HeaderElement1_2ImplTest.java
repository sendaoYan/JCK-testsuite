/*
 * @test
 * @summary com.sun.xml.internal.messaging.saaj.soap.ver1_2.HeaderElement1_2Impl conformance: class hierarchy and members.
 * @run main HeaderElement1_2ImplTest
 */

public class HeaderElement1_2ImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.messaging.saaj.soap.ver1_2.HeaderElement1_2Impl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.messaging.saaj.soap.ver1_2.HeaderElement1_2Impl"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("com.sun.xml.internal.messaging.saaj.soap.impl.HeaderElementImpl".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.messaging.saaj.soap.impl.HeaderElementImpl");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getActorAttributeName"), "has method getActorAttributeName");
        check(mn.contains("getActorOrRole"), "has method getActorOrRole");
        check(mn.contains("getMustunderstandAttributeName"), "has method getMustunderstandAttributeName");
        check(mn.contains("getMustunderstandAttributeValue"), "has method getMustunderstandAttributeValue");
        check(mn.contains("getMustunderstandLiteralValue"), "has method getMustunderstandLiteralValue");
        check(mn.contains("getRelayAttributeName"), "has method getRelayAttributeName");
        check(mn.contains("getRelayAttributeValue"), "has method getRelayAttributeValue");
        check(mn.contains("getRelayLiteralValue"), "has method getRelayLiteralValue");
        check(mn.contains("getRoleAttributeName"), "has method getRoleAttributeName");
        check(mn.contains("setElementQName"), "has method setElementQName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
