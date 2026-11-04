/*
 * @test
 * @summary java.sql.ClientInfoStatus conformance: enum constants and members.
 * @run main ClientInfoStatusTest
 */


public class ClientInfoStatusTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.sql.ClientInfoStatus");
        check(c.isEnum(), "is enum");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == java.lang.Enum.class, "enum superclass");
        Object[] ec = c.getEnumConstants();
        check(ec.length == 4, "enum constant count");
        check(((java.lang.Enum<?>) ec[0]).name().equals("REASON_UNKNOWN"), "constant REASON_UNKNOWN");
        check(((java.lang.Enum<?>) ec[1]).name().equals("REASON_UNKNOWN_PROPERTY"), "constant REASON_UNKNOWN_PROPERTY");
        check(((java.lang.Enum<?>) ec[2]).name().equals("REASON_VALUE_INVALID"), "constant REASON_VALUE_INVALID");
        check(((java.lang.Enum<?>) ec[3]).name().equals("REASON_VALUE_TRUNCATED"), "constant REASON_VALUE_TRUNCATED");        System.out.println("PASS (" + passed + " assertions)");
    }
}
