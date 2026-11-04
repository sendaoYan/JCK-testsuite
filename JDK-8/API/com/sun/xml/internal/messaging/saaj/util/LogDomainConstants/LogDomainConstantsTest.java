/*
 * @test
 * @summary com.sun.xml.internal.messaging.saaj.util.LogDomainConstants conformance: interface hierarchy and members.
 * @run main LogDomainConstantsTest
 */

public class LogDomainConstantsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.messaging.saaj.util.LogDomainConstants", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.messaging.saaj.util.LogDomainConstants"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
