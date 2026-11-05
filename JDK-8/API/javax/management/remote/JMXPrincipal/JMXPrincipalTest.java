/*
 * @test
 * @summary javax.management.remote.JMXPrincipal conformance: name/equals.
 * @run main JMXPrincipalTest
 */
import javax.management.*;


import javax.management.remote.*;
public class JMXPrincipalTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        JMXPrincipal p = new JMXPrincipal("alice");
        check("alice".equals(p.getName()), "name");
        check(p.equals(new JMXPrincipal("alice")), "equals");
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
