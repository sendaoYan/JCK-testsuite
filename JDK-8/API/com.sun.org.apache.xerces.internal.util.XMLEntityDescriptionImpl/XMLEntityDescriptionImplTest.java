/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.util.XMLEntityDescriptionImpl conformance: class hierarchy and members.
 * @run main XMLEntityDescriptionImplTest
 */
public class XMLEntityDescriptionImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.util.XMLEntityDescriptionImpl", false, XMLEntityDescriptionImplTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.util.XMLResourceIdentifierImpl"), "superclass com.sun.org.apache.xerces.internal.util.XMLResourceIdentifierImpl");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.impl.XMLEntityDescription"), "has interface com.sun.org.apache.xerces.internal.impl.XMLEntityDescription");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("setEntityName"), "method setEntityName");
        check(mn.contains("getEntityName"), "method getEntityName");
        check(mn.contains("setDescription"), "method setDescription");
        check(mn.contains("clear"), "method clear");
        check(mn.contains("hashCode"), "method hashCode");
        check(mn.contains("toString"), "method toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
