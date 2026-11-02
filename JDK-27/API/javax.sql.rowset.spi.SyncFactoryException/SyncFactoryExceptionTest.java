/*
 * @test
 * @summary javax.sql.rowset.spi.SyncFactoryException conformance: constructors
 * @run main SyncFactoryExceptionTest
 */

public class SyncFactoryExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        javax.sql.rowset.spi.SyncFactoryException e = new javax.sql.rowset.spi.SyncFactoryException();
        check(e.getMessage() == null, "no-arg");
        javax.sql.rowset.spi.SyncFactoryException e2 = new javax.sql.rowset.spi.SyncFactoryException("m");
        check("m".equals(e2.getMessage()), "String ctor");
        check(e2 instanceof java.sql.SQLException, "extends SQLException");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
