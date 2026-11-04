/*
 * @test
 * @summary java.net.Authenticator.RequestorType conformance: enum constants and members.
 * @run main RequestorTypeTest
 */


public class RequestorTypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.net.Authenticator$RequestorType");
        check(c.isEnum(), "is enum");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == java.lang.Enum.class, "enum superclass");
        Object[] ec = c.getEnumConstants();
        check(ec.length == 2, "enum constant count");
        check(((java.lang.Enum<?>) ec[0]).name().equals("PROXY"), "constant PROXY");
        check(((java.lang.Enum<?>) ec[1]).name().equals("SERVER"), "constant SERVER");        System.out.println("PASS (" + passed + " assertions)");
    }
}
