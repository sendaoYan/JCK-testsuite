/*
 * @test
 * @summary javax.sql.rowset.serial.SerialException conformance: constructors
 * @run main SerialExceptionTest
 */

public class SerialExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        javax.sql.rowset.serial.SerialException e = new javax.sql.rowset.serial.SerialException();
        check(e.getMessage() == null, "no-arg");
        javax.sql.rowset.serial.SerialException e2 = new javax.sql.rowset.serial.SerialException("m");
        check("m".equals(e2.getMessage()), "String ctor");
        check(e2 instanceof java.sql.SQLException, "extends SQLException");        System.out.println("PASS (" + passed + " assertions)");
    }
}
