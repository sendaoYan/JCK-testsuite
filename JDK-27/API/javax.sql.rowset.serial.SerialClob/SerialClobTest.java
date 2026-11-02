/*
 * @test
 * @summary javax.sql.rowset.serial.SerialClob conformance: character storage and clone
 * @run main SerialClobTest
 */

public class SerialClobTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        javax.sql.rowset.serial.SerialClob cl = new javax.sql.rowset.serial.SerialClob("hello".toCharArray());
        check(cl.length() == 5, "length");
        check("hello".equals(cl.getSubString(1, 5)), "getSubString");
        check(cl.getCharacterStream() != null, "getCharacterStream non-null");
        check(cl.position("ell", 1) == 2, "position string");
        int n = cl.setString(6, "XYZ");
        check(n == 3, "setString returns 3");
        check(cl.length() == 8, "length after setString");
        check("XYZ".equals(cl.getSubString(6, 3)), "getSubString after setString");
        javax.sql.rowset.serial.SerialClob cl2 = (javax.sql.rowset.serial.SerialClob) cl.clone();
        check(cl2 != null && cl2.length() == 8, "clone");
        check(cl.equals(cl2), "equals clone");
        cl.free();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
