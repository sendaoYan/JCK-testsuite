/*
 * @test
 * @summary java.sql.RowIdLifetime conformance: enum constants and valueOf roundtrip
 * @run main RowIdLifetimeTest
 */

import java.sql.RowIdLifetime;

public class RowIdLifetimeTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = RowIdLifetime.class;
        check(c.isEnum(), "is enum");
        check(c.getEnumConstants().length == 5, "5 enum constants");
        check(RowIdLifetime.values().length == 5, "values() length 5");
        check(RowIdLifetime.valueOf("ROWID_UNSUPPORTED") == RowIdLifetime.ROWID_UNSUPPORTED, "valueOf == ROWID_UNSUPPORTED");
        check(RowIdLifetime.valueOf("ROWID_UNSUPPORTED").name().equals("ROWID_UNSUPPORTED"), "name ROWID_UNSUPPORTED");
        check(RowIdLifetime.valueOf("ROWID_VALID_OTHER") == RowIdLifetime.ROWID_VALID_OTHER, "valueOf == ROWID_VALID_OTHER");
        check(RowIdLifetime.valueOf("ROWID_VALID_OTHER").name().equals("ROWID_VALID_OTHER"), "name ROWID_VALID_OTHER");
        check(RowIdLifetime.valueOf("ROWID_VALID_SESSION") == RowIdLifetime.ROWID_VALID_SESSION, "valueOf == ROWID_VALID_SESSION");
        check(RowIdLifetime.valueOf("ROWID_VALID_SESSION").name().equals("ROWID_VALID_SESSION"), "name ROWID_VALID_SESSION");
        check(RowIdLifetime.valueOf("ROWID_VALID_TRANSACTION") == RowIdLifetime.ROWID_VALID_TRANSACTION, "valueOf == ROWID_VALID_TRANSACTION");
        check(RowIdLifetime.valueOf("ROWID_VALID_TRANSACTION").name().equals("ROWID_VALID_TRANSACTION"), "name ROWID_VALID_TRANSACTION");
        check(RowIdLifetime.valueOf("ROWID_VALID_FOREVER") == RowIdLifetime.ROWID_VALID_FOREVER, "valueOf == ROWID_VALID_FOREVER");
        check(RowIdLifetime.valueOf("ROWID_VALID_FOREVER").name().equals("ROWID_VALID_FOREVER"), "name ROWID_VALID_FOREVER");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
