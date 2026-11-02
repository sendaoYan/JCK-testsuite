/*
 * @test
 * @summary java.net.URISyntaxException conformance: input/reason/index.
 * @run main URISyntaxExceptionTest
 */


public class URISyntaxExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        java.net.URISyntaxException e = new java.net.URISyntaxException("bad input", "missing :");
        check(e instanceof java.lang.Exception, "is Exception");
        check("bad input".equals(e.getInput()), "getInput");
        check("missing :".equals(e.getReason()), "getReason");
        check(e.getIndex() == -1, "index -1 when not set");
        check(e.getMessage() != null && e.getMessage().contains("bad input"), "getMessage includes input");
        java.net.URISyntaxException e2 = new java.net.URISyntaxException("bad input", "missing :", 3);
        check(e2.getIndex() == 3, "getIndex");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
