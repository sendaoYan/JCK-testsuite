/*
 * @test
 * @summary javax.sql.rowset.serial.SerialDatalink conformance: getDatalink.
 * @run main SerialDatalinkTest
 */


public class SerialDatalinkTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.sql.rowset.serial.SerialDatalink d = new javax.sql.rowset.serial.SerialDatalink(new java.net.URL("http://example.com/"));
        check(d.getDatalink().toString().equals("http://example.com/"), "getDatalink");        System.out.println("PASS (" + passed + " assertions)");
    }
}
