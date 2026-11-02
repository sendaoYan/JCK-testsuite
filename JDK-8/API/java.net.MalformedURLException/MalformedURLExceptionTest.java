/*
 * @test
 * @summary java.net.MalformedURLException conformance: message and hierarchy.
 * @run main MalformedURLExceptionTest
 */


public class MalformedURLExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        java.net.MalformedURLException e = new java.net.MalformedURLException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e instanceof java.io.IOException, "is IOException");
        java.net.MalformedURLException e0 = new java.net.MalformedURLException();
        check(e0.getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
