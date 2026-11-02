/*
 * @test
 * @summary javax.annotation.processing.FilerException conformance: filer exception.
 * @run main FilerExceptionTest
 */

public class FilerExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        javax.annotation.processing.FilerException e = new javax.annotation.processing.FilerException("boom");
        check(e instanceof java.io.IOException, "is IOException");
        check("boom".equals(e.getMessage()), "getMessage");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
