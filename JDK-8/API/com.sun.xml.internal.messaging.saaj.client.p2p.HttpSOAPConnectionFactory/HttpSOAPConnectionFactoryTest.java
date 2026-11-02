/*
 * @test
 * @summary com.sun.xml.internal.messaging.saaj.client.p2p.HttpSOAPConnectionFactory conformance: class hierarchy and members.
 * @run main HttpSOAPConnectionFactoryTest
 */

public class HttpSOAPConnectionFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.messaging.saaj.client.p2p.HttpSOAPConnectionFactory", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.messaging.saaj.client.p2p.HttpSOAPConnectionFactory"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("javax.xml.soap.SOAPConnectionFactory".equals(c.getSuperclass().getName()), "extends javax.xml.soap.SOAPConnectionFactory");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("createConnection"), "has method createConnection");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
