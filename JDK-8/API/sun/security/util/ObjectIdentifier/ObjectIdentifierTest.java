/*
 * @test
 * @summary sun.security.util.ObjectIdentifier conformance: type shape + behavioral (headless-safe).
 * @run main ObjectIdentifierTest
 */

public class ObjectIdentifierTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.util.ObjectIdentifier", false, ObjectIdentifierTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "isInterface");
        check(!c.isEnum(), "isEnum");
        check(!c.isAnnotation(), "isAnnotation");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        sun.security.util.ObjectIdentifier oid = new sun.security.util.ObjectIdentifier("1.3.6.1.4.1");
        check(oid.toString().equals("1.3.6.1.4.1"), "OID toString");
        sun.security.util.ObjectIdentifier oid2 = new sun.security.util.ObjectIdentifier("1.3.6.1.4.1");
        check(oid.equals(oid2), "OID equals");
        check(oid.hashCode() == oid2.hashCode(), "OID hashCode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
