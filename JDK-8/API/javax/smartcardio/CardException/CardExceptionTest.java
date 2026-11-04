/*
 * @test
 * @summary javax.smartcardio.CardException conformance: card exception.
 * @run main CardExceptionTest
 */

public class CardExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        javax.smartcardio.CardException e = new javax.smartcardio.CardException("boom");
        check("boom".equals(e.getMessage()), "getMessage");
        check(e instanceof Exception, "is Exception");
        javax.smartcardio.CardException e2 = new javax.smartcardio.CardException(new RuntimeException("c"));
        check(e2.getCause() != null, "cause");
        javax.smartcardio.CardException e3 = new javax.smartcardio.CardException("m", new RuntimeException("c"));
        check("m".equals(e3.getMessage()), "message");
        check(e3.getCause() != null, "cause 2");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
