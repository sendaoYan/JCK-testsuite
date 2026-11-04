/*
 * @test
 * @summary java.sql.Time conformance: valueOf/toString.
 * @run main TimeTest
 */


public class TimeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        java.sql.Time t = java.sql.Time.valueOf("01:02:03");
        check("01:02:03".equals(t.toString()), "toString");
        java.sql.Time t2 = new java.sql.Time(t.getTime());
        check(t.equals(t2), "equals by millis");        System.out.println("PASS (" + passed + " assertions)");
    }
}
