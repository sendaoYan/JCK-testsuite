/*
 * @test
 * @summary java.net.HttpRetryException conformance: detail/responseCode/location.
 * @run main HttpRetryExceptionTest
 */


public class HttpRetryExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        java.net.HttpRetryException e = new java.net.HttpRetryException("retry", 503);
        check(e instanceof java.io.IOException, "is IOException");
        check("retry".equals(e.getMessage()), "getMessage");
        check(e.responseCode() == 503, "responseCode");
        check(e.getLocation() == null, "location null");
        java.net.HttpRetryException e2 = new java.net.HttpRetryException("retry", 503, "http://x/");
        check("http://x/".equals(e2.getLocation()), "getLocation");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
