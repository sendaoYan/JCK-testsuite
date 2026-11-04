/*
 * @test
 * @summary javax.activation.URLDataSource conformance: url data source behavior.
 * @run main URLDataSourceTest
 */


public class URLDataSourceTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        java.io.File tmp = java.io.File.createTempFile("uds", ".txt");
        try {
            javax.activation.URLDataSource uds = new javax.activation.URLDataSource(tmp.toURI().toURL());
            check("text/plain".equals(uds.getContentType()), "content type");
            check(uds.getInputStream() != null, "input stream");
            uds.getInputStream().close();
            check(uds instanceof javax.activation.DataSource, "is DataSource");
        } finally { tmp.delete(); }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
