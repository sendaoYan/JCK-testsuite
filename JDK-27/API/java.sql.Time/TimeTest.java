/*
 * @test
 * @summary java.sql.Time conformance: valueOf, toLocalTime and time accessors
 * @run main TimeTest
 */

public class TimeTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        java.sql.Time t = java.sql.Time.valueOf("12:34:56");
        check("12:34:56".equals(t.toString()), "toString");
        check(java.time.LocalTime.of(12, 34, 56).equals(t.toLocalTime()), "toLocalTime");
        java.sql.Time t2 = java.sql.Time.valueOf(java.time.LocalTime.of(12, 34, 56));
        check(t2.toLocalTime().equals(java.time.LocalTime.of(12, 34, 56)), "valueOf(LocalTime)");
        boolean threw = false;
        try { t.toInstant(); } catch (UnsupportedOperationException ex) { threw = true; }
        check(threw, "toInstant throws UnsupportedOperationException");
        java.sql.Time t3 = new java.sql.Time(0L);
        t3.setTime(7000L);
        check(t3.getTime() == 7000L, "setTime/getTime");        System.out.println("PASS (" + passed + " assertions)");
    }
}
