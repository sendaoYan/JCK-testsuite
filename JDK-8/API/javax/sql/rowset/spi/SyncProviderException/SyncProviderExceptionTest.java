/*
 * @test
 * @summary javax.sql.rowset.spi.SyncProviderException conformance: message and hierarchy.
 * @run main SyncProviderExceptionTest
 */


public class SyncProviderExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.sql.rowset.spi.SyncProviderException e = new javax.sql.rowset.spi.SyncProviderException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e instanceof java.sql.SQLException, "is SQLException");
        javax.sql.rowset.spi.SyncProviderException e0 = new javax.sql.rowset.spi.SyncProviderException();
        check(e0.getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
