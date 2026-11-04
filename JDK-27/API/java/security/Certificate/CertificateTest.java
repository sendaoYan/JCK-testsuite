/*
 * @test
 * @summary java.security.Certificate conformance: white-box structure + black-box behavior.
 * @run main CertificateTest
 */

import java.lang.reflect.*;
import java.security.*;
import java.util.*;
import java.io.*;
import javax.security.auth.x500.X500Principal;

public class CertificateTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.Certificate");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        checkMethods(c, new String[]{"decode", "encode", "getFormat", "getGuarantor", "getPrincipal", "getPublicKey", "toString"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        // Deprecated interface (replaced by java.security.cert.Certificate); full custom implementation
        final Principal guarantor = new X500Principal("CN=jck-guarantor");
        final Principal subject = new X500Principal("CN=jck-subject");
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        final PublicKey pub = kpg.generateKeyPair().getPublic();
        class JckCert implements Certificate {
            public Principal getGuarantor() { return guarantor; }
            public Principal getPrincipal() { return subject; }
            public PublicKey getPublicKey() { return pub; }
            public void encode(OutputStream out) throws KeyException, IOException {
                out.write(getFormat().getBytes(java.nio.charset.StandardCharsets.US_ASCII));
            }
            public void decode(InputStream in) throws KeyException, IOException { }
            public String getFormat() { return "JCK-FORMAT"; }
            public String toString(boolean detailed) { return detailed ? "JCK cert detailed" : "JCK cert"; }
        }
        JckCert c = new JckCert();
        check(c.getGuarantor() == guarantor, "guarantor");
        check(c.getPrincipal() == subject, "principal");
        check(c.getPublicKey() == pub, "public key");
        check(c.getFormat().equals("JCK-FORMAT"), "format");
        check(c.toString(false).equals("JCK cert"), "toString brief");
        check(c.toString(true).equals("JCK cert detailed"), "toString detailed");
        // encode writes to a stream
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        c.encode(bos);
        check(new String(bos.toByteArray(), java.nio.charset.StandardCharsets.US_ASCII).equals("JCK-FORMAT"),
            "encode");
        // decode accepts a stream without error
        c.decode(new ByteArrayInputStream(new byte[0]));
        check(true, "decode callable");
        // interface relationship: distinct from java.security.cert.Certificate
        check(c instanceof java.security.Certificate, "is java.security.Certificate");
        check(!((Object) c instanceof java.security.cert.Certificate), "not cert.Certificate");
        // Identity.addCertificate consumed this interface historically; Identity carries none by default
        Identity id = new Identity("jck") {};
        check(id.certificates() == null || id.certificates().length == 0, "identity no certs");

        // ---- appended contract dimensions ----
        Class<?> c0 = java.security.Certificate.class;
        check(c0.getDeclaredMethods().length == 7, "exactly 7 declared methods");
        boolean anyDefault = false;
        for (Method m : c0.getDeclaredMethods()) if (m.isDefault()) anyDefault = true;
        check(!anyDefault, "no default methods");
        check(c0.getInterfaces().length == 0, "extends no interfaces");
        check(!Serializable.class.isAssignableFrom(c0), "not Serializable");
        check(c0.isAnnotationPresent(Deprecated.class), "@Deprecated");
        // checked-exception clauses
        Method enc = c0.getMethod("encode", OutputStream.class);
        Set<Class<?>> encEx = new HashSet<>(Arrays.asList(enc.getExceptionTypes()));
        check(encEx.contains(KeyException.class) && encEx.contains(IOException.class), "encode throws clauses");
        Method dec = c0.getMethod("decode", InputStream.class);
        Set<Class<?>> decEx = new HashSet<>(Arrays.asList(dec.getExceptionTypes()));
        check(decEx.contains(KeyException.class) && decEx.contains(IOException.class), "decode throws clauses");
        // encode/decode may propagate their checked exceptions verbatim
        java.security.Certificate failing = new java.security.Certificate() {
            public Principal getGuarantor() { return null; }
            public Principal getPrincipal() { return null; }
            public PublicKey getPublicKey() { return null; }
            public void encode(OutputStream out) throws IOException { throw new IOException("io"); }
            public void decode(InputStream in) throws KeyException { throw new KeyException("key"); }
            public String getFormat() { return null; }
            public String toString(boolean detailed) { return "f"; }
        };
        try { failing.encode(new ByteArrayOutputStream()); check(false, "encode IOException propagates"); }
        catch (IOException e) { check("io".equals(e.getMessage()), "encode IOException verbatim"); }
        try { failing.decode(new ByteArrayInputStream(new byte[0])); check(false, "decode KeyException propagates"); }
        catch (KeyException e) { check("key".equals(e.getMessage()), "decode KeyException verbatim"); }
        // interface defines no equals value contract: distinct same-content implementations stay unequal
        check(!c.equals(new JckCert()), "no value equals across implementations");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    static void checkInterfaces(Class<?> c, String[] names) {
        for (String n : names) check(hasInterface(c, n), "implements " + n);
    }
    static void checkMethods(Class<?> c, String[] names) {
        for (String n : names) check(hasMethod(c, n), "method " + n);
    }
    static boolean hasMethod(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    static void checkEnumConstants(Class<?> c, String[] names) {
        Object[] cs = c.getEnumConstants();
        check(cs != null, "enum constants");
        check(cs.length == names.length, "enum constant count");
        for (String n : names) check(hasConst(cs, n), "constant " + n);
    }
    static boolean hasConst(Object[] cs, String name) {
        for (Object o : cs) if (((Enum<?>) o).name().equals(name)) return true;
        return false;
    }
}
