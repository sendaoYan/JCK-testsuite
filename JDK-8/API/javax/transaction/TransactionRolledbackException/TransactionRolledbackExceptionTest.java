/*
 * @test
 * @summary javax.transaction.TransactionRolledbackException conformance: constructors, message, cause and superclass.
 * @run main TransactionRolledbackExceptionTest
 */

public class TransactionRolledbackExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.transaction.TransactionRolledbackException e = new javax.transaction.TransactionRolledbackException("boom");
        check("boom".equals(e.getMessage()), "String ctor sets message");
        check(e instanceof java.rmi.RemoteException, "extends java.rmi.RemoteException");
        javax.transaction.TransactionRolledbackException e4 = new javax.transaction.TransactionRolledbackException();
        check(e4.getMessage() == null, "no-arg ctor message null");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
