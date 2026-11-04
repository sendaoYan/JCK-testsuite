/*
 * @test
 * @summary java.sql.Timestamp conformance: valueOf/getNanos/compareTo.
 * @run main TimestampTest
 */


public class TimestampTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        java.sql.Timestamp t = java.sql.Timestamp.valueOf("2020-01-02 03:04:05.123456789");
        check("2020-01-02 03:04:05.123456789".equals(t.toString()), "toString");
        check(t.getNanos() == 123456789, "getNanos");
        java.sql.Timestamp t2 = java.sql.Timestamp.valueOf("2020-01-02 03:04:05.123456789");
        check(t.equals(t2) && t.compareTo(t2) == 0, "equals/compareTo");
        java.sql.Timestamp earlier = java.sql.Timestamp.valueOf("2020-01-01 00:00:00");
        check(t.after(earlier), "after");        System.out.println("PASS (" + passed + " assertions)");
    }
}
