/*
 * @test
 * @summary com.sun.xml.internal.messaging.saaj.soap.SOAPFactoryImpl conformance: abstract class hierarchy and members.
 * @run main SOAPFactoryImplTest
 */

public class SOAPFactoryImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.messaging.saaj.soap.SOAPFactoryImpl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.messaging.saaj.soap.SOAPFactoryImpl"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check("javax.xml.soap.SOAPFactory".equals(c.getSuperclass().getName()), "extends javax.xml.soap.SOAPFactory");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("convertToSoapElement"), "has method convertToSoapElement");
        check(mn.contains("createDetail"), "has method createDetail");
        check(mn.contains("createDocument"), "has method createDocument");
        check(mn.contains("createElement"), "has method createElement");
        check(mn.contains("createFault"), "has method createFault");
        check(mn.contains("createName"), "has method createName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
