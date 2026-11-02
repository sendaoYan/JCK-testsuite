/*
 * @test
 * @summary javax.sql.rowset.spi.SyncProviderException conformance: constructors
 * @run main SyncProviderExceptionTest
 */

public class SyncProviderExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        javax.sql.rowset.spi.SyncProviderException e = new javax.sql.rowset.spi.SyncProviderException();
        check(e.getMessage() == null, "no-arg");
        check(e.getSyncResolver() != null, "no-arg default resolver non-null");
        check(e.getSyncResolver() instanceof javax.sql.rowset.spi.SyncResolver, "default resolver is SyncResolver");
        javax.sql.rowset.spi.SyncProviderException e2 = new javax.sql.rowset.spi.SyncProviderException("m");
        check("m".equals(e2.getMessage()), "String ctor");
        check(e2 instanceof java.sql.SQLException, "extends SQLException");        System.out.println("PASS (" + passed + " assertions)");
    }
}
