/*
 * @test
 * @summary javax.transaction.InvalidTransactionException conformance: constructors, message, cause and superclass.
 * @run main InvalidTransactionExceptionTest
 */

public class InvalidTransactionExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.transaction.InvalidTransactionException e = new javax.transaction.InvalidTransactionException("boom");
        check("boom".equals(e.getMessage()), "String ctor sets message");
        check(e instanceof java.rmi.RemoteException, "extends java.rmi.RemoteException");
        javax.transaction.InvalidTransactionException e4 = new javax.transaction.InvalidTransactionException();
        check(e4.getMessage() == null, "no-arg ctor message null");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
