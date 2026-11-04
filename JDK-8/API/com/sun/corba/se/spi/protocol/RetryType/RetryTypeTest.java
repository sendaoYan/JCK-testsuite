/*
 * @test
 * @summary com.sun.corba.se.spi.protocol.RetryType conformance: enum constants and shape (headless-safe).
 * @run main RetryTypeTest
 */

import java.lang.reflect.Modifier;

public class RetryTypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.spi.protocol.RetryType", false, RetryTypeTest.class.getClassLoader());
        check(c.isEnum(), "isEnum");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass java.lang.Enum");
        Object[] ec = c.getEnumConstants();
        check(ec != null, "enum constants non-null");
        check(ec.length == 3, "enum constants count");
        java.util.Set<String> cs = new java.util.HashSet<String>();
        for (Object o : ec) cs.add(((Enum<?>) o).name());
        check(cs.contains("AFTER_RESPONSE"), "constant AFTER_RESPONSE");
        check(cs.contains("BEFORE_RESPONSE"), "constant BEFORE_RESPONSE");
        check(cs.contains("NONE"), "constant NONE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
