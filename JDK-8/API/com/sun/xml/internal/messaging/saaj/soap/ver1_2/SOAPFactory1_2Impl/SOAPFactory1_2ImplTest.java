/*
 * @test
 * @summary com.sun.xml.internal.messaging.saaj.soap.ver1_2.SOAPFactory1_2Impl conformance: class hierarchy and members.
 * @run main SOAPFactory1_2ImplTest
 */

public class SOAPFactory1_2ImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.messaging.saaj.soap.ver1_2.SOAPFactory1_2Impl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.messaging.saaj.soap.ver1_2.SOAPFactory1_2Impl"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("com.sun.xml.internal.messaging.saaj.soap.SOAPFactoryImpl".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.messaging.saaj.soap.SOAPFactoryImpl");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("createDetail"), "has method createDetail");
        check(mn.contains("createDocument"), "has method createDocument");
        check(mn.contains("createFault"), "has method createFault");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
