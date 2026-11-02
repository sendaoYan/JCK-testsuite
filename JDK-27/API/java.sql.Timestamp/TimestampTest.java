/*
 * @test
 * @summary java.sql.Timestamp conformance: valueOf, nanos and Instant roundtrip
 * @run main TimestampTest
 */

public class TimestampTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        java.sql.Timestamp ts = java.sql.Timestamp.valueOf("2026-01-01 12:34:56");
        check(ts.getNanos() == 0, "getNanos default 0");
        check("2026-01-01 12:34:56.0".equals(ts.toString()), "toString");
        check(java.time.LocalDateTime.of(2026, 1, 1, 12, 34, 56).equals(ts.toLocalDateTime()), "toLocalDateTime");
        java.sql.Timestamp ts2 = java.sql.Timestamp.valueOf("2026-01-01 12:34:56.123456789");
        check(ts2.getNanos() == 123456789, "getNanos fractional");
        java.time.Instant inst = java.time.Instant.parse("2026-01-01T12:34:56Z");
        java.sql.Timestamp ts3 = java.sql.Timestamp.from(inst);
        check(ts3.toInstant().equals(inst), "from/toInstant roundtrip");
        check(ts3.equals(ts3), "equals self");
        check(ts3.compareTo(ts3) == 0, "compareTo self");
        ts3.setNanos(42);
        check(ts3.getNanos() == 42, "setNanos");
        check(ts3.before(java.sql.Timestamp.from(java.time.Instant.parse("2027-01-01T00:00:00Z"))), "before");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
