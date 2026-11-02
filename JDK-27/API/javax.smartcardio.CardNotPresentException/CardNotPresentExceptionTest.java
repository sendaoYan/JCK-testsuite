/*
 * @test
 * @summary javax.smartcardio.CardNotPresentException conformance: card not present exception.
 * @run main CardNotPresentExceptionTest
 */

public class CardNotPresentExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        javax.smartcardio.CardNotPresentException e = new javax.smartcardio.CardNotPresentException("boom");
        check("boom".equals(e.getMessage()), "getMessage");
        check(e instanceof javax.smartcardio.CardException, "is CardException");
        javax.smartcardio.CardNotPresentException e2 = new javax.smartcardio.CardNotPresentException(new RuntimeException("c"));
        check(e2.getCause() != null, "cause");
        javax.smartcardio.CardNotPresentException e3 = new javax.smartcardio.CardNotPresentException("m", new RuntimeException("c"));
        check("m".equals(e3.getMessage()), "message");
        check(e3.getCause() != null, "cause 2");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
