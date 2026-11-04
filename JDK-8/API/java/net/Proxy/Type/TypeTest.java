/*
 * @test
 * @summary java.net.Proxy.Type conformance: enum constants and members.
 * @run main TypeTest
 */


public class TypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.net.Proxy$Type");
        check(c.isEnum(), "is enum");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == java.lang.Enum.class, "enum superclass");
        Object[] ec = c.getEnumConstants();
        check(ec.length == 3, "enum constant count");
        check(((java.lang.Enum<?>) ec[0]).name().equals("DIRECT"), "constant DIRECT");
        check(((java.lang.Enum<?>) ec[1]).name().equals("HTTP"), "constant HTTP");
        check(((java.lang.Enum<?>) ec[2]).name().equals("SOCKS"), "constant SOCKS");        System.out.println("PASS (" + passed + " assertions)");
    }
}
