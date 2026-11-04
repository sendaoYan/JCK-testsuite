/*
 * @test
 * @summary javax.sql.rowset.serial.SerialException conformance: message and hierarchy.
 * @run main SerialExceptionTest
 */


public class SerialExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.sql.rowset.serial.SerialException e = new javax.sql.rowset.serial.SerialException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e instanceof java.sql.SQLException, "is SQLException");
        javax.sql.rowset.serial.SerialException e0 = new javax.sql.rowset.serial.SerialException();
        check(e0.getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
