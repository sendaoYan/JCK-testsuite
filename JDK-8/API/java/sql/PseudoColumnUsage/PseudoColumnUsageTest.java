/*
 * @test
 * @summary java.sql.PseudoColumnUsage conformance: enum constants and members.
 * @run main PseudoColumnUsageTest
 */


public class PseudoColumnUsageTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.sql.PseudoColumnUsage");
        check(c.isEnum(), "is enum");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == java.lang.Enum.class, "enum superclass");
        Object[] ec = c.getEnumConstants();
        check(ec.length == 4, "enum constant count");
        check(((java.lang.Enum<?>) ec[0]).name().equals("SELECT_LIST_ONLY"), "constant SELECT_LIST_ONLY");
        check(((java.lang.Enum<?>) ec[1]).name().equals("WHERE_CLAUSE_ONLY"), "constant WHERE_CLAUSE_ONLY");
        check(((java.lang.Enum<?>) ec[2]).name().equals("NO_USAGE_RESTRICTIONS"), "constant NO_USAGE_RESTRICTIONS");
        check(((java.lang.Enum<?>) ec[3]).name().equals("USAGE_UNKNOWN"), "constant USAGE_UNKNOWN");        System.out.println("PASS (" + passed + " assertions)");
    }
}
