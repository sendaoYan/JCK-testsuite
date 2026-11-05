/*
 * @test
 * @summary javax.print.attribute.UnmodifiableSetException conformance: exception class.
 * @run main UnmodifiableSetExceptionTest
 */

import java.lang.reflect.*;


public class UnmodifiableSetExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        javax.print.attribute.UnmodifiableSetException e1 = new javax.print.attribute.UnmodifiableSetException();
        check(e1.getMessage() == null, "default message");
        check(e1 instanceof RuntimeException, "extends RuntimeException");
        javax.print.attribute.UnmodifiableSetException e2 = new javax.print.attribute.UnmodifiableSetException("m");
        check("m".equals(e2.getMessage()), "message ctor");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: constructor chain / unchecked / serializable ----
    static void testContractDeep() throws Exception {
        javax.print.attribute.UnmodifiableSetException e0 = new javax.print.attribute.UnmodifiableSetException();
        check(e0.getMessage() == null, "no-arg null message");
        javax.print.attribute.UnmodifiableSetException e1 = new javax.print.attribute.UnmodifiableSetException("locked");
        check("locked".equals(e1.getMessage()), "message form");
        check(e1 instanceof RuntimeException, "unchecked");
        check(e1 instanceof java.io.Serializable, "serializable");
        Object rt = serRoundTrip(e1);
        check("locked".equals(((javax.print.attribute.UnmodifiableSetException) rt).getMessage()), "serialization round-trip");
    }

    static Object serRoundTrip(Object o) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        new java.io.ObjectOutputStream(bos).writeObject(o);
        return new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
    }
    static void expectIAE(String what, Runnable r, String msg) {
        try { r.run(); throw new AssertionError("expected IAE: " + what); }
        catch (IllegalArgumentException e) { check(msg == null || msg.equals(e.getMessage()), "IAE msg " + what + " [" + e.getMessage() + "]"); }
    }
    static void expectNPE(String what, Runnable r, String msg) {
        try { r.run(); throw new AssertionError("expected NPE: " + what); }
        catch (NullPointerException e) { check(msg == null || msg.equals(e.getMessage()), "NPE msg " + what + " [" + e.getMessage() + "]"); }
    }

}
