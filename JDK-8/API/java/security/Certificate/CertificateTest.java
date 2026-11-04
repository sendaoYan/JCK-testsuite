/*
 * @test
 * @summary java.security.Certificate conformance: deprecated interface contract:
 *          exact 7-method surface (getGuarantor/getPrincipal/getPublicKey/
 *          encode(OutputStream)/decode(InputStream)/getFormat/toString(boolean)),
 *          checked-exception clauses, full behavioral implementation (encode
 *          stream round-trip, toString(boolean) branches, accessor identity),
 *          identity equals, Deprecated annotation status, disjointness from
 *          java.security.cert.Certificate, no Serializable, no default methods.
 * @run main CertificateTest
 */

public class CertificateTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.Certificate");
        check(c.isInterface(), "is interface");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("decode"), "declares decode");
        check(ms.contains("encode"), "declares encode");
        check(ms.contains("getFormat"), "declares getFormat");
        check(ms.contains("getGuarantor"), "declares getGuarantor");
        check(ms.contains("getPrincipal"), "declares getPrincipal");
        check(ms.contains("getPublicKey"), "declares getPublicKey");
        check(ms.contains("toString"), "declares toString");

        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- structural + behavioral contract (appended) ----
    static void testContract() throws Exception {
        Class<?> c = Class.forName("java.security.Certificate");

        // exact method surface: 7 methods, all abstract, no defaults
        check(c.getDeclaredMethods().length == 7, "exactly 7 declared methods");
        boolean anyDefault = false, anyStatic = false;
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (m.isDefault()) anyDefault = true;
            if (java.lang.reflect.Modifier.isStatic(m.getModifiers())) anyStatic = true;
        }
        check(!anyDefault, "no default methods");
        check(!anyStatic, "no static methods");

        // checked-exception clauses on encode/decode
        java.lang.reflect.Method enc = c.getMethod("encode", java.io.OutputStream.class);
        Class<?>[] encEx = enc.getExceptionTypes();
        boolean hasKeyEx = false, hasIOEx = false;
        for (Class<?> e : encEx) { if (e == java.security.KeyException.class) hasKeyEx = true; if (e == java.io.IOException.class) hasIOEx = true; }
        check(hasKeyEx && hasIOEx, "encode throws KeyException,IOException");
        java.lang.reflect.Method dec = c.getMethod("decode", java.io.InputStream.class);
        Class<?>[] decEx = dec.getExceptionTypes();
        hasKeyEx = false; hasIOEx = false;
        for (Class<?> e : decEx) { if (e == java.security.KeyException.class) hasKeyEx = true; if (e == java.io.IOException.class) hasIOEx = true; }
        check(hasKeyEx && hasIOEx, "decode throws KeyException,IOException");

        // toString(boolean) overrides Object.toString with one boolean parameter
        java.lang.reflect.Method ts = c.getMethod("toString", boolean.class);
        check(ts.getReturnType() == String.class, "toString(boolean) returns String");

        // interface traits
        check(c.isAnnotationPresent(Deprecated.class), "@Deprecated");
        check(!java.io.Serializable.class.isAssignableFrom(c), "not Serializable");
        check(c.getInterfaces().length == 0, "extends no interfaces");
        check(!java.security.cert.Certificate.class.isAssignableFrom(c)
                && !c.isAssignableFrom(java.security.cert.Certificate.class),
                "disjoint from java.security.cert.Certificate");

        // ---- behavioral implementation ----
        final java.security.Principal guarantor = new javax.security.auth.x500.X500Principal("CN=jck-guarantor");
        final java.security.Principal subject = new javax.security.auth.x500.X500Principal("CN=jck-subject");
        java.security.KeyPairGenerator kpg = java.security.KeyPairGenerator.getInstance("RSA");
        kpg.initialize(512);
        final java.security.PublicKey pub = kpg.generateKeyPair().getPublic();

        class RecCert implements java.security.Certificate {
            java.io.OutputStream lastStream;
            java.io.InputStream lastIn;
            int encodeCalls, decodeCalls;
            public java.security.Principal getGuarantor() { return guarantor; }
            public java.security.Principal getPrincipal() { return subject; }
            public java.security.PublicKey getPublicKey() { return pub; }
            public void encode(java.io.OutputStream out) throws java.security.KeyException, java.io.IOException {
                encodeCalls++;
                lastStream = out;
                out.write(getFormat().getBytes(java.nio.charset.StandardCharsets.US_ASCII));
            }
            public void decode(java.io.InputStream in) throws java.security.KeyException, java.io.IOException {
                decodeCalls++;
                lastIn = in;
            }
            public String getFormat() { return "JCK-FORMAT"; }
            public String toString(boolean detailed) { return detailed ? "cert[verbose]" : "cert[brief]"; }
        }
        RecCert cert = new RecCert();
        check(cert.getGuarantor() == guarantor, "getGuarantor identity");
        check(cert.getPrincipal() == subject, "getPrincipal identity");
        check(cert.getPublicKey() == pub, "getPublicKey identity");
        check("JCK-FORMAT".equals(cert.getFormat()), "getFormat");

        // encode: stream wiring + byte content + repeat-call state
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        cert.encode(bos);
        check(cert.encodeCalls == 1, "encode invoked once");
        check(cert.lastStream == bos, "encode receives caller stream");
        check("JCK-FORMAT".equals(new String(bos.toByteArray(), java.nio.charset.StandardCharsets.US_ASCII)),
                "encode writes format bytes");
        java.io.ByteArrayOutputStream bos2 = new java.io.ByteArrayOutputStream();
        cert.encode(bos2);
        check(cert.encodeCalls == 2 && bos2.size() == bos.size(), "encode repeatable and deterministic");

        // decode: stream identity pass-through
        java.io.ByteArrayInputStream bin = new java.io.ByteArrayInputStream(new byte[]{9, 8});
        cert.decode(bin);
        check(cert.decodeCalls == 1 && cert.lastIn == bin, "decode receives caller stream");

        // toString(boolean) both branches are implementation-defined but distinct per contract
        check("cert[brief]".equals(cert.toString(false)), "toString(false)");
        check("cert[verbose]".equals(cert.toString(true)), "toString(true)");
        check(!cert.toString(false).equals(cert.toString(true)), "toString branches differ");

        // implementation may propagate checked exceptions (IOException through encode)
        class FailingCert implements java.security.Certificate {
            public java.security.Principal getGuarantor() { return null; }
            public java.security.Principal getPrincipal() { return null; }
            public java.security.PublicKey getPublicKey() { return null; }
            public void encode(java.io.OutputStream out) throws java.io.IOException { throw new java.io.IOException("disk"); }
            public void decode(java.io.InputStream in) throws java.security.KeyException { throw new java.security.KeyException("bad"); }
            public String getFormat() { return null; }
            public String toString(boolean detailed) { return "f"; }
        }
        FailingCert fc = new FailingCert();
        try {
            fc.encode(new java.io.ByteArrayOutputStream());
            check(false, "encode propagates IOException");
        } catch (java.io.IOException e) {
            check("disk".equals(e.getMessage()), "encode propagates IOException verbatim");
        }
        try {
            fc.decode(new java.io.ByteArrayInputStream(new byte[0]));
            check(false, "decode propagates KeyException");
        } catch (java.security.KeyException e) {
            check("bad".equals(e.getMessage()), "decode propagates KeyException verbatim");
        }

        // interface defines no equals contract: distinct implementations with identical content stay unequal
        RecCert other = new RecCert();
        check(!cert.equals(other), "no value equals across implementations");
        check(cert.equals(cert), "reflexive equals");
    }
}
