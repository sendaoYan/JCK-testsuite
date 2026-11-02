/*
 * @test
 * @summary javax.sql.rowset.serial.SerialBlob conformance: length/getBytes.
 * @run main SerialBlobTest
 */


public class SerialBlobTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.sql.rowset.serial.SerialBlob b = new javax.sql.rowset.serial.SerialBlob(new byte[]{1, 2, 3});
        check(b.length() == 3, "length");
        byte[] g = b.getBytes(1, 2);
        check(g.length == 2 && g[0] == 1, "getBytes");        System.out.println("PASS (" + passed + " assertions)");
    }
}
