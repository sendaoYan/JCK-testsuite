/*
 * @test
 * @summary java.sql.RowIdLifetime conformance: enum constants and members.
 * @run main RowIdLifetimeTest
 */


public class RowIdLifetimeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.sql.RowIdLifetime");
        check(c.isEnum(), "is enum");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == java.lang.Enum.class, "enum superclass");
        Object[] ec = c.getEnumConstants();
        check(ec.length == 5, "enum constant count");
        check(((java.lang.Enum<?>) ec[0]).name().equals("ROWID_UNSUPPORTED"), "constant ROWID_UNSUPPORTED");
        check(((java.lang.Enum<?>) ec[1]).name().equals("ROWID_VALID_OTHER"), "constant ROWID_VALID_OTHER");
        check(((java.lang.Enum<?>) ec[2]).name().equals("ROWID_VALID_SESSION"), "constant ROWID_VALID_SESSION");
        check(((java.lang.Enum<?>) ec[3]).name().equals("ROWID_VALID_TRANSACTION"), "constant ROWID_VALID_TRANSACTION");
        check(((java.lang.Enum<?>) ec[4]).name().equals("ROWID_VALID_FOREVER"), "constant ROWID_VALID_FOREVER");        System.out.println("PASS (" + passed + " assertions)");
    }
}
