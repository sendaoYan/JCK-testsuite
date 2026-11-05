/*
 * @test
 * @summary javax.xml.crypto.NoSuchMechanismException conformance.
 * @run main NoSuchMechanismExceptionTest
 */

import javax.xml.crypto.NoSuchMechanismException;

public class NoSuchMechanismExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(NoSuchMechanismException.class.getSuperclass().getName().equals("java.lang.RuntimeException"), "extends RuntimeException");
        NoSuchMechanismException e1 = new NoSuchMechanismException("m");
        check("m".equals(e1.getMessage()) && e1.getCause() == null, "message ctor");
        Throwable cause = new IllegalStateException("root");
        NoSuchMechanismException e2 = new NoSuchMechanismException("m2", cause);
        check("m2".equals(e2.getMessage()) && e2.getCause() == cause, "message+cause ctor");
        NoSuchMechanismException e3 = new NoSuchMechanismException(cause);
        check(e3.getCause() == cause && cause.toString().equals(e3.getMessage()), "cause-only ctor");
        // unchecked: no throws clause needed
        boolean caught = false;
        try { throw new NoSuchMechanismException("unchecked"); }
        catch (RuntimeException re) { caught = re instanceof NoSuchMechanismException; }
        check(caught, "catchable as RuntimeException");
        // serialization round-trip
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) { oos.writeObject(e2); }
        NoSuchMechanismException back = (NoSuchMechanismException) new java.io.ObjectInputStream(
            new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check("m2".equals(back.getMessage()), "message survives serialization");
        check(back.getCause() != null && "root".equals(back.getCause().getMessage()), "cause survives serialization");
        // real triggers from both factory families name the missing mechanism
        try {
            javax.xml.crypto.dsig.XMLSignatureFactory.getInstance("BogusMech");
            check(false, "signature factory bogus mechanism should throw");
        } catch (NoSuchMechanismException nsme) {
            check(nsme.getMessage().contains("BogusMech"), "signature factory message names mechanism");
        }
        try {
            javax.xml.crypto.dsig.keyinfo.KeyInfoFactory.getInstance("BogusMech");
            check(false, "keyinfo factory bogus mechanism should throw");
        } catch (NoSuchMechanismException nsme) {
            check(nsme.getMessage().contains("BogusMech"), "keyinfo factory message names mechanism");
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
