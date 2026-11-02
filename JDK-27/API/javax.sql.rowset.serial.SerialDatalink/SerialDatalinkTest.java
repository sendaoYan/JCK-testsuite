/*
 * @test
 * @summary javax.sql.rowset.serial.SerialDatalink conformance: URL storage and clone
 * @run main SerialDatalinkTest
 */

public class SerialDatalinkTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        java.net.URL url = new java.net.URL("http://example.com");
        javax.sql.rowset.serial.SerialDatalink sd = new javax.sql.rowset.serial.SerialDatalink(url);
        check(sd.getDatalink() != null, "getDatalink non-null");
        check("http://example.com".equals(sd.getDatalink().toString()), "getDatalink value");
        javax.sql.rowset.serial.SerialDatalink sd2 = new javax.sql.rowset.serial.SerialDatalink(new java.net.URL("http://example.com"));
        check(sd.equals(sd2), "equals");
        check(sd.hashCode() == sd2.hashCode(), "hashCode");
        check(sd.clone() != null, "clone non-null");        System.out.println("PASS (" + passed + " assertions)");
    }
}
