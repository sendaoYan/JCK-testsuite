/*
 * @test
 * @summary javax.sql.rowset.serial.SerialClob conformance: length/getSubString.
 * @run main SerialClobTest
 */


public class SerialClobTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.sql.rowset.serial.SerialClob c = new javax.sql.rowset.serial.SerialClob(new char[]{'a', 'b', 'c'});
        check(c.length() == 3, "length");
        check("ab".equals(c.getSubString(1, 2)), "getSubString");        System.out.println("PASS (" + passed + " assertions)");
    }
}
