/*
 * @test
 * @summary javax.transaction.xa.XAException conformance: white-box structure + black-box behavior.
 * @run main XAExceptionTest
 */

import java.lang.reflect.Modifier;
import javax.transaction.xa.XAException;

public class XAExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() {
        Class<?> c = XAException.class;
        check(c != null, "loads");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        check(c.getSuperclass() == Exception.class, "extends Exception");
    }

    static void testBlackBox() {
        // no-arg ctor
        XAException e0 = new XAException();
        check(e0.errorCode == 0, "default errorCode 0");
        check(e0.getMessage() == null, "no-arg message null");

        // int ctor sets errorCode, no message
        XAException e7 = new XAException(7);
        check(e7.errorCode == 7, "int ctor errorCode");
        check(e7.getMessage() == null, "int ctor message null");

        // String ctor sets message, errorCode 0
        XAException es = new XAException("boom");
        check(es.errorCode == 0, "String ctor errorCode 0");
        check("boom".equals(es.getMessage()), "String ctor message");

        // errorCode is a public mutable field
        es.errorCode = 99;
        check(es.errorCode == 99, "errorCode mutable field");

        // constants
        check(XAException.XA_RBBASE == 100, "XA_RBBASE");
        check(XAException.XA_RBROLLBACK == 100, "XA_RBROLLBACK");
        check(XAException.XA_RBCOMMFAIL == 101, "XA_RBCOMMFAIL");
        check(XAException.XA_RBDEADLOCK == 102, "XA_RBDEADLOCK");
        check(XAException.XA_RBINTEGRITY == 103, "XA_RBINTEGRITY");
        check(XAException.XA_RBOTHER == 104, "XA_RBOTHER");
        check(XAException.XA_RBPROTO == 105, "XA_RBPROTO");
        check(XAException.XA_RBTIMEOUT == 106, "XA_RBTIMEOUT");
        check(XAException.XA_RBTRANSIENT == 107, "XA_RBTRANSIENT");
        check(XAException.XA_RBEND == 107, "XA_RBEND");
        check(XAException.XA_NOMIGRATE == 9, "XA_NOMIGRATE");
        check(XAException.XA_HEURHAZ == 8, "XA_HEURHAZ");
        check(XAException.XA_HEURCOM == 7, "XA_HEURCOM");
        check(XAException.XA_HEURRB == 6, "XA_HEURRB");
        check(XAException.XA_HEURMIX == 5, "XA_HEURMIX");
        check(XAException.XA_RETRY == 4, "XA_RETRY");
        check(XAException.XA_RDONLY == 3, "XA_RDONLY");
        check(XAException.XAER_ASYNC == -2, "XAER_ASYNC");
        check(XAException.XAER_RMERR == -3, "XAER_RMERR");
        check(XAException.XAER_NOTA == -4, "XAER_NOTA");
        check(XAException.XAER_INVAL == -5, "XAER_INVAL");
        check(XAException.XAER_PROTO == -6, "XAER_PROTO");
        check(XAException.XAER_RMFAIL == -7, "XAER_RMFAIL");
        check(XAException.XAER_DUPID == -8, "XAER_DUPID");
        check(XAException.XAER_OUTSIDE == -9, "XAER_OUTSIDE");
    }

    public static void main(String[] args) {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
