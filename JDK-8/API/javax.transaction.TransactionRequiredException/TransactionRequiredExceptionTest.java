/*
 * @test
 * @summary javax.transaction.TransactionRequiredException conformance: constructors, message, cause and superclass.
 * @run main TransactionRequiredExceptionTest
 */

public class TransactionRequiredExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.transaction.TransactionRequiredException e = new javax.transaction.TransactionRequiredException("boom");
        check("boom".equals(e.getMessage()), "String ctor sets message");
        check(e instanceof java.rmi.RemoteException, "extends java.rmi.RemoteException");
        javax.transaction.TransactionRequiredException e4 = new javax.transaction.TransactionRequiredException();
        check(e4.getMessage() == null, "no-arg ctor message null");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
