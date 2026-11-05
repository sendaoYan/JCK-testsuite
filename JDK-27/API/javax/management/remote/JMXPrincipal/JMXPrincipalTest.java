/*
 * @test
 * @summary javax.management.remote.JMXPrincipal conformance: white-box structure + black-box behavior.
 * @run main JMXPrincipalTest
 */
import javax.management.*;
import javax.management.remote.*;


import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.security.Principal;

import javax.management.remote.JMXPrincipal;

public class JMXPrincipalTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.remote.JMXPrincipal");
        check(!c.isInterface(), "concrete class");
        check(Principal.class.isAssignableFrom(c), "implements Principal");
        check(Serializable.class.isAssignableFrom(c), "implements Serializable");
        Constructor<?> k = c.getConstructor(String.class);
        check(k != null, "(String) ctor");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        JMXPrincipal p = new JMXPrincipal("user");
        check(p.getName().equals("user"), "name");
        check(p.toString().equals("JMXPrincipal:  user"), "toString: " + p.toString());

        check(p.equals(new JMXPrincipal("user")), "equals same name");
        check(p.hashCode() == new JMXPrincipal("user").hashCode(), "hashCode consistent");
        check(!p.equals(new JMXPrincipal("other")), "different name not equal");
        check(!p.equals(new JMXPrincipal("User")), "equals case-sensitive");
        check(!p.equals("user"), "not equal to String");
        check(!p.equals(null), "not equal to null");
        check(p.equals(p), "reflexive");

        JMXPrincipal empty = new JMXPrincipal("");
        check(empty.getName().equals(""), "empty name allowed");
        check(empty.hashCode() == "".hashCode(), "empty name hashCode");

        try {
            new JMXPrincipal(null);
            check(false, "expected NPE for null name");
        } catch (NullPointerException e) {
            check(e.getMessage().equals("illegal null input"), "null name message: " + e.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepened();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ==== deepened (2026-11, batch m): null semantics, equals/hashCode, toString, Subject, serialization ====
    static void testDeepened() throws Exception {
        JMXPrincipal p = new JMXPrincipal("alice");
        check("alice".equals(p.getName()), "d-name");
        check(p.getName() == p.getName(), "d-getName stable reference");
        check("JMXPrincipal:  alice".equals(p.toString()), "d-toString format (two spaces)");
        check(p.hashCode() == new JMXPrincipal("alice").hashCode(), "d-hashCode equal for same name");
        check(p.equals(new JMXPrincipal("alice")), "d-equals same name");
        check(!p.equals(new JMXPrincipal("bob")), "d-not equals different name");
        check(!new JMXPrincipal("").equals(p), "d-empty name distinct");
        check(!p.equals(null), "d-not equals null");
        check(!p.equals("alice"), "d-not equals String");
        try {
            new JMXPrincipal(null);
            check(false, "d-null name should throw");
        } catch (NullPointerException e) {
            check("illegal null input".equals(e.getMessage()), "d-null name message: " + e.getMessage());
        }
        // Principal view inside a Subject: by-value containment.
        javax.security.auth.Subject s = new javax.security.auth.Subject();
        check(s.getPrincipals().add(p), "d-add to subject principals");
        check(s.getPrincipals().contains(new JMXPrincipal("alice")), "d-subject contains by value");
        check(s.getPrincipals().remove(new JMXPrincipal("alice")), "d-subject removes by value");
        // Serialization round-trip.
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = null;
        try {
            oos = new java.io.ObjectOutputStream(bos);
            oos.writeObject(p);
        } finally {
            if (oos != null) oos.close();
        }
        java.io.ObjectInputStream ois = null;
        try {
            ois = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray()));
            Object q = ois.readObject();
            check(q instanceof JMXPrincipal && q.equals(p), "d-serialization round-trip equals");
        } finally {
            if (ois != null) ois.close();
        }
    }

}
