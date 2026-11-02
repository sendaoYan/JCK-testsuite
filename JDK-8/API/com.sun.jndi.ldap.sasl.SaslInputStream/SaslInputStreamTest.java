/*
 * @test
 * @summary com.sun.jndi.ldap.sasl.SaslInputStream conformance: hierarchy and declared members via reflection.
 * @run main SaslInputStreamTest
 */

public class SaslInputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jndi.ldap.sasl.SaslInputStream", false, null);
        check(c.getName().equals("com.sun.jndi.ldap.sasl.SaslInputStream"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.io.InputStream"), "superclass java.io.InputStream");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("available"), "declares available");
        check(ms.contains("close"), "declares close");
        check(ms.contains("fill"), "declares fill");
        check(ms.contains("networkByteOrderToInt"), "declares networkByteOrderToInt");
        check(ms.contains("read"), "declares read");
        check(ms.contains("readFully"), "declares readFully");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
