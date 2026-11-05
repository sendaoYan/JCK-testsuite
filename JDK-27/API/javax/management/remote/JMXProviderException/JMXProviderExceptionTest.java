/*
 * @test
 * @summary javax.management.remote.JMXProviderException conformance: white-box structure + black-box behavior.
 * @run main JMXProviderExceptionTest
 */
import javax.management.*;
import javax.management.remote.*;


import java.io.IOException;
import java.lang.reflect.Constructor;

import javax.management.remote.JMXProviderException;

public class JMXProviderExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.remote.JMXProviderException");
        check(IOException.class.isAssignableFrom(c), "extends IOException");
        check(c.getConstructor() != null, "() ctor");
        check(c.getConstructor(String.class) != null, "(String) ctor");
        check(c.getConstructor(String.class, Throwable.class) != null, "(String,Throwable) ctor");
        check(c.getMethod("getCause") != null, "getCause");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Exception cause = new Exception("root");
        JMXProviderException e = new JMXProviderException("msg", cause);
        check(e.getMessage().equals("msg"), "message");
        check(e.getCause() == cause, "cause preserved");
        check(e instanceof IOException, "is IOException");

        JMXProviderException noCause = new JMXProviderException("only");
        check(noCause.getMessage().equals("only"), "string-only message");
        check(noCause.getCause() == null, "string-only cause null");

        JMXProviderException empty = new JMXProviderException();
        check(empty.getMessage() == null, "no-arg message null");
        check(empty.getCause() == null, "no-arg cause null");

        // Standard Throwable behavior: suppressed exception machinery is usable.
        JMXProviderException ex = new JMXProviderException("m");
        ex.addSuppressed(new RuntimeException("s"));
        check(ex.getSuppressed().length == 1, "suppressed exceptions work");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepened();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ==== deepened (2026-11, batch m): ctor matrix, cause identity, serialization ====
    static void testDeepened() throws Exception {
        JMXProviderException e0 = new JMXProviderException();
        check(e0.getMessage() == null, "d-no-arg message null");
        check(e0.getCause() == null, "d-no-arg cause null");
        JMXProviderException e1 = new JMXProviderException("m");
        check("m".equals(e1.getMessage()), "d-message ctor");
        check(e1.getCause() == null, "d-message ctor cause null");
        java.lang.Throwable cause = new java.lang.AssertionError("inner");
        JMXProviderException e2 = new JMXProviderException("m2", cause);
        check("m2".equals(e2.getMessage()), "d-full ctor message");
        check(e2.getCause() == cause, "d-full ctor cause identity");
        check(e2 instanceof java.io.IOException, "d-IOException hierarchy");
        check(e2 instanceof java.io.Serializable, "d-Serializable");
        check(e2.toString().startsWith("javax.management.remote.JMXProviderException"), "d-toString prefix");
        // Serialization round-trip preserves message and cause.
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = null;
        try {
            oos = new java.io.ObjectOutputStream(bos);
            oos.writeObject(e2);
        } finally {
            if (oos != null) oos.close();
        }
        java.io.ObjectInputStream ois = null;
        try {
            ois = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray()));
            JMXProviderException back = (JMXProviderException) ois.readObject();
            check("m2".equals(back.getMessage()), "d-ser message");
            check(back.getCause() instanceof java.lang.AssertionError, "d-ser cause type");
            check("inner".equals(back.getCause().getMessage()), "d-ser cause message");
        } finally {
            if (ois != null) ois.close();
        }
    }

}
