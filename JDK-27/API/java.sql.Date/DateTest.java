/*
 * @test
 * @summary java.sql.Date conformance: valueOf, toLocalDate and time accessors
 * @run main DateTest
 */

public class DateTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        java.sql.Date d = java.sql.Date.valueOf("2026-01-01");
        check("2026-01-01".equals(d.toString()), "toString");
        check(java.time.LocalDate.of(2026, 1, 1).equals(d.toLocalDate()), "toLocalDate");
        java.sql.Date d2 = java.sql.Date.valueOf(java.time.LocalDate.of(2026, 1, 1));
        check(d2.toLocalDate().equals(java.time.LocalDate.of(2026, 1, 1)), "valueOf(LocalDate)");
        check(d2.toLocalDate().equals(d.toLocalDate()), "valueOf roundtrip same local date");
        boolean threw = false;
        try { d.toInstant(); } catch (UnsupportedOperationException ex) { threw = true; }
        check(threw, "toInstant throws UnsupportedOperationException");
        java.sql.Date d3 = new java.sql.Date(0L);
        d3.setTime(5000L);
        check(d3.getTime() == 5000L, "setTime/getTime");        System.out.println("PASS (" + passed + " assertions)");
    }
}
