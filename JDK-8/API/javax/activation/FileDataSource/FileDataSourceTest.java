/*
 * @test
 * @summary javax.activation.FileDataSource conformance: file data source behavior.
 * @run main FileDataSourceTest
 */


public class FileDataSourceTest {
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
        java.io.File tmp = java.io.File.createTempFile("fds", ".txt");
        try {
            javax.activation.FileDataSource fds = new javax.activation.FileDataSource(tmp);
            check("text/plain".equals(fds.getContentType()), "content type");
            check(fds.getInputStream() != null, "input stream");
            fds.getInputStream().close();
            check(fds instanceof javax.activation.DataSource, "is DataSource");
        } finally { tmp.delete(); }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
