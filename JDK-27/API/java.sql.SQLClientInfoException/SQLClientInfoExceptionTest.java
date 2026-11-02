/*
 * @test
 * @summary java.sql.SQLClientInfoException conformance: constructors and failed-properties map
 * @run main SQLClientInfoExceptionTest
 */

public class SQLClientInfoExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        java.sql.SQLClientInfoException e = new java.sql.SQLClientInfoException();
        check(e.getFailedProperties() == null, "no-arg failedProperties null");
        java.util.Map<String, java.sql.ClientInfoStatus> m = new java.util.HashMap<String, java.sql.ClientInfoStatus>();
        m.put("k", java.sql.ClientInfoStatus.REASON_UNKNOWN);
        java.sql.SQLClientInfoException e2 = new java.sql.SQLClientInfoException(m);
        check(e2.getFailedProperties() == m, "getFailedProperties returns same map");
        check(e2.getFailedProperties().get("k") == java.sql.ClientInfoStatus.REASON_UNKNOWN, "map content");
        RuntimeException cause = new RuntimeException("c");
        java.sql.SQLClientInfoException e3 = new java.sql.SQLClientInfoException("msg", "st", 1, m, cause);
        check("st".equals(e3.getSQLState()) && e3.getErrorCode() == 1, "state/code");
        check(e3.getCause() == cause && e3.getFailedProperties() == m, "cause and map");
        check(e3 instanceof java.sql.SQLException, "extends SQLException");        System.out.println("PASS (" + passed + " assertions)");
    }
}
