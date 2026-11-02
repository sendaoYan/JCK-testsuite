/*
 * @test
 * @summary javax.transaction.xa.XAException conformance: error codes and constructors
 * @run main XAExceptionTest
 */

public class XAExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        javax.transaction.xa.XAException e = new javax.transaction.xa.XAException();
        check(e.errorCode == 0, "default errorCode 0");
        check(e.getMessage() == null, "no-arg message null");
        javax.transaction.xa.XAException e2 = new javax.transaction.xa.XAException(7);
        check(e2.errorCode == 7, "int ctor errorCode");
        javax.transaction.xa.XAException e3 = new javax.transaction.xa.XAException("boom");
        check(e3.errorCode == 0 && "boom".equals(e3.getMessage()), "String ctor");
        check(javax.transaction.xa.XAException.XA_RBBASE == 100, "XA_RBBASE");
        check(javax.transaction.xa.XAException.XA_RBROLLBACK == 100, "XA_RBROLLBACK");
        check(javax.transaction.xa.XAException.XA_RBEND == 107, "XA_RBEND");
        check(javax.transaction.xa.XAException.XA_NOMIGRATE == 9, "XA_NOMIGRATE");
        check(javax.transaction.xa.XAException.XA_HEURHAZ == 8, "XA_HEURHAZ");
        check(javax.transaction.xa.XAException.XA_HEURCOM == 7, "XA_HEURCOM");
        check(javax.transaction.xa.XAException.XA_HEURRB == 6, "XA_HEURRB");
        check(javax.transaction.xa.XAException.XA_HEURMIX == 5, "XA_HEURMIX");
        check(javax.transaction.xa.XAException.XA_RETRY == 4, "XA_RETRY");
        check(javax.transaction.xa.XAException.XA_RDONLY == 3, "XA_RDONLY");
        check(javax.transaction.xa.XAException.XAER_ASYNC == -2, "XAER_ASYNC");
        check(javax.transaction.xa.XAException.XAER_RMERR == -3, "XAER_RMERR");
        check(javax.transaction.xa.XAException.XAER_NOTA == -4, "XAER_NOTA");
        check(javax.transaction.xa.XAException.XAER_INVAL == -5, "XAER_INVAL");
        check(javax.transaction.xa.XAException.XAER_PROTO == -6, "XAER_PROTO");
        check(javax.transaction.xa.XAException.XAER_RMFAIL == -7, "XAER_RMFAIL");
        check(javax.transaction.xa.XAException.XAER_DUPID == -8, "XAER_DUPID");
        check(javax.transaction.xa.XAException.XAER_OUTSIDE == -9, "XAER_OUTSIDE");
        check(e instanceof Exception, "extends Exception");        System.out.println("PASS (" + passed + " assertions)");
    }
}
