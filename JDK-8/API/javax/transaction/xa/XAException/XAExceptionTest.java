/*
 * @test
 * @summary javax.transaction.xa.XAException conformance: constructors and errorCode.
 * @run main XAExceptionTest
 */

public class XAExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.transaction.xa.XAException e = new javax.transaction.xa.XAException(7);
        check(e.errorCode == 7, "errorCode int ctor");
        javax.transaction.xa.XAException e2 = new javax.transaction.xa.XAException();
        check(e2.errorCode == 0, "no-arg ctor errorCode 0");
        javax.transaction.xa.XAException e3 = new javax.transaction.xa.XAException("msg");
        check("msg".equals(e3.getMessage()), "String ctor message");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
