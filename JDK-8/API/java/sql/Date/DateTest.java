/*
 * @test
 * @summary java.sql.Date conformance: valueOf/getTime/toString.
 * @run main DateTest
 */


public class DateTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        java.sql.Date d = java.sql.Date.valueOf("2020-01-02");
        check("2020-01-02".equals(d.toString()), "toString");
        check(d.getTime() == java.sql.Date.valueOf("2020-01-02").getTime(), "getTime");
        java.sql.Date d2 = new java.sql.Date(d.getTime());
        check(d.equals(d2), "equals by millis");        System.out.println("PASS (" + passed + " assertions)");
    }
}
