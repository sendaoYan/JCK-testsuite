/*
 * @test
 * @summary java.net.UnknownHostException conformance: white-box structure + black-box behavior.
 * @run main UnknownHostExceptionTest
 */

import java.lang.reflect.*;
import java.net.*;

public class UnknownHostExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.UnknownHostException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(java.io.IOException.class.isAssignableFrom(c), "extends IOException");
        check(c.getConstructor(String.class) != null, "ctor(String)");
        check(c.getConstructor() != null, "ctor()");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable");
    }

    static void testBlackBox() throws Exception {
        UnknownHostException e = new UnknownHostException("no such host");
        check(e.getMessage().equals("no such host"), "message");
        check(e instanceof java.io.IOException, "instanceof IOException");
        try { throw e; } catch (java.io.IOException ioe) { passed++; }
        UnknownHostException e2 = new UnknownHostException();
        check(e2.getMessage() == null, "no-arg message null");
    }

    // ------------------------------------------------------------------
    // Deepening: exact messages, null semantics, serialization, API origin
    // ------------------------------------------------------------------

    static void testDeep() throws Exception {
        UnknownHostException e = new UnknownHostException("boom");
        check(e.getCause() == null, "no cause by default");
        check("java.net.UnknownHostException: boom".equals(e.toString()), "toString exact");
        UnknownHostException en = new UnknownHostException(null);
        check(en.getMessage() == null, "null message stays null");
        check(en.getCause() == null, "null-message no cause");
        try {
            throw new UnknownHostException();
        } catch (Exception ex) {
            check(ex instanceof UnknownHostException, "catchable as Exception");
            check(((UnknownHostException) ex).getMessage() == null, "no-arg catch message null");
        }
        // serialization round trip preserves class and message
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(e);
        oos.writeObject(new UnknownHostException());
        oos.close();
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()));
        UnknownHostException back = (UnknownHostException) ois.readObject();
        UnknownHostException back0 = (UnknownHostException) ois.readObject();
        check("boom".equals(back.getMessage()), "serialized message preserved");
        check(back.getClass() == UnknownHostException.class, "serialized exact class");
        check(back instanceof java.io.IOException, "serialized still IOException");
        check(back0.getMessage() == null, "serialized no-arg message null");
        check(!back.equals(back0), "distinct instances");
        // produced by the InetAddress API with a malformed literal (no DNS)
        try {
            InetAddress.getByName("999.888.777.666");
            check(false, "malformed literal must throw");
        } catch (UnknownHostException uhe) {
            check(uhe.getMessage() != null, "UHE from getByName has message");
            check(uhe.getMessage().contains("999.888.777.666"), "message contains the literal");
        }
        // strict literal API throws IAE instead of UHE (JDK-18+ ofLiteral)
        try {
            InetAddress.ofLiteral("999.888.777.666");
            check(false, "ofLiteral malformed must throw");
        } catch (IllegalArgumentException iae) {
            check("Invalid IP address literal: 999.888.777.666".equals(iae.getMessage()),
                    "ofLiteral IAE exact message");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
