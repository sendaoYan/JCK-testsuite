/*
 * @test
 * @summary javax.sql.rowset.serial.SerialBlob conformance: byte array storage and clone
 * @run main SerialBlobTest
 */

public class SerialBlobTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        javax.sql.rowset.serial.SerialBlob b = new javax.sql.rowset.serial.SerialBlob(new byte[]{1, 2, 3, 4, 5});
        check(b.length() == 5, "length");
        byte[] got = b.getBytes(1, 2);
        check(got.length == 2 && got[0] == 1 && got[1] == 2, "getBytes");
        check(b.getBinaryStream() != null, "getBinaryStream non-null");
        check(b.position(new byte[]{3, 4}, 1) == 3, "position pattern");
        int n = b.setBytes(6, new byte[]{7, 8});
        check(n == 2, "setBytes returns 2");
        check(b.length() == 7, "length after setBytes");
        javax.sql.rowset.serial.SerialBlob c = (javax.sql.rowset.serial.SerialBlob) b.clone();
        check(c != null && c.length() == 7, "clone");
        check(b.equals(c), "equals clone");
        b.free();        System.out.println("PASS (" + passed + " assertions)");
    }
}
