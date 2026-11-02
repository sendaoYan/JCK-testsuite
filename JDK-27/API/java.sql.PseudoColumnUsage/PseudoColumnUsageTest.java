/*
 * @test
 * @summary java.sql.PseudoColumnUsage conformance: enum constants and valueOf roundtrip
 * @run main PseudoColumnUsageTest
 */

import java.sql.PseudoColumnUsage;

public class PseudoColumnUsageTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = PseudoColumnUsage.class;
        check(c.isEnum(), "is enum");
        check(c.getEnumConstants().length == 4, "4 enum constants");
        check(PseudoColumnUsage.values().length == 4, "values() length 4");
        check(PseudoColumnUsage.valueOf("SELECT_LIST_ONLY") == PseudoColumnUsage.SELECT_LIST_ONLY, "valueOf == SELECT_LIST_ONLY");
        check(PseudoColumnUsage.valueOf("SELECT_LIST_ONLY").name().equals("SELECT_LIST_ONLY"), "name SELECT_LIST_ONLY");
        check(PseudoColumnUsage.valueOf("WHERE_CLAUSE_ONLY") == PseudoColumnUsage.WHERE_CLAUSE_ONLY, "valueOf == WHERE_CLAUSE_ONLY");
        check(PseudoColumnUsage.valueOf("WHERE_CLAUSE_ONLY").name().equals("WHERE_CLAUSE_ONLY"), "name WHERE_CLAUSE_ONLY");
        check(PseudoColumnUsage.valueOf("NO_USAGE_RESTRICTIONS") == PseudoColumnUsage.NO_USAGE_RESTRICTIONS, "valueOf == NO_USAGE_RESTRICTIONS");
        check(PseudoColumnUsage.valueOf("NO_USAGE_RESTRICTIONS").name().equals("NO_USAGE_RESTRICTIONS"), "name NO_USAGE_RESTRICTIONS");
        check(PseudoColumnUsage.valueOf("USAGE_UNKNOWN") == PseudoColumnUsage.USAGE_UNKNOWN, "valueOf == USAGE_UNKNOWN");
        check(PseudoColumnUsage.valueOf("USAGE_UNKNOWN").name().equals("USAGE_UNKNOWN"), "name USAGE_UNKNOWN");        System.out.println("PASS (" + passed + " assertions)");
    }
}
