/*
 * @test
 * @summary com.sun.xml.internal.messaging.saaj.soap.ver1_1.Body1_1Impl conformance: class hierarchy and members.
 * @run main Body1_1ImplTest
 */

public class Body1_1ImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.messaging.saaj.soap.ver1_1.Body1_1Impl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.messaging.saaj.soap.ver1_1.Body1_1Impl"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("com.sun.xml.internal.messaging.saaj.soap.impl.BodyImpl".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.messaging.saaj.soap.impl.BodyImpl");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("addSOAP12Fault"), "has method addSOAP12Fault");
        check(mn.contains("createBodyElement"), "has method createBodyElement");
        check(mn.contains("createFaultElement"), "has method createFaultElement");
        check(mn.contains("getDefaultFaultCode"), "has method getDefaultFaultCode");
        check(mn.contains("getFaultName"), "has method getFaultName");
        check(mn.contains("isFault"), "has method isFault");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
