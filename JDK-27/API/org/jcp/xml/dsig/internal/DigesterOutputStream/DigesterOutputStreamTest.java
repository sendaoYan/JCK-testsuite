/*
 * @test
 * @summary org.jcp.xml.dsig.internal.DigesterOutputStream conformance: internal package reflection.
 * @modules java.xml.crypto/org.jcp.xml.dsig.internal:+open
 * @run main DigesterOutputStreamTest
 */
import java.lang.reflect.*;
import java.util.*;
public class DigesterOutputStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.DigesterOutputStream", false, DigesterOutputStreamTest.class.getClassLoader());
        check(c.getName().equals("org.jcp.xml.dsig.internal.DigesterOutputStream"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("java.io.OutputStream"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("close"), "method:close");
        check(methods.contains("getDigestValue"), "method:getDigestValue");
        check(methods.contains("getInputStream"), "method:getInputStream");
        check(methods.contains("write"), "method:write");
        deepen();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ---- deepening: exact member face + behavior (contract lock) ----
    static void deepen() throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.DigesterOutputStream");
        check("java.io.OutputStream".equals(c.getSuperclass().getName()), "superclass java.io.OutputStream");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.isEmpty(), "no directly declared interfaces");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()) && !java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !java.lang.reflect.Modifier.isFinal(c.getModifiers()), "modifier set [public]");
        java.util.Set<String> names = new java.util.HashSet<String>();
        java.util.Set<String> staticNames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (java.lang.reflect.Modifier.isPublic(mod) || java.lang.reflect.Modifier.isProtected(mod)) {
                names.add(m.getName());
                if (java.lang.reflect.Modifier.isStatic(mod)) staticNames.add(m.getName());
            }
        }
        check(names.equals(new java.util.HashSet<String>(java.util.Arrays.asList("close", "getDigestValue", "getInputStream", "write"))),
                "exact public/protected method face (4 names): " + names);
        java.util.Set<String> ctorFace = new java.util.HashSet<String>();
        for (java.lang.reflect.Constructor<?> k : c.getDeclaredConstructors()) {
            if (java.lang.reflect.Modifier.isPrivate(k.getModifiers())) continue;
            StringBuilder sb = new StringBuilder();
            for (Class<?> t : k.getParameterTypes()) sb.append(t.getName()).append(",");
            ctorFace.add(sb.toString());
        }
        check(ctorFace.size() == 2, "exactly 2 declared constructors, got " + ctorFace.size());
        check(ctorFace.contains("java.security.MessageDigest,"), "constructor(MessageDigest) present");
        check(ctorFace.contains("java.security.MessageDigest,boolean,"), "constructor(MessageDigest, boolean) present");
        // black-box behavior
        java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
        org.jcp.xml.dsig.internal.DigesterOutputStream out = new org.jcp.xml.dsig.internal.DigesterOutputStream(md);
        check(out instanceof java.io.OutputStream, "DigesterOutputStream is an OutputStream");
        out.write(new byte[]{1, 2, 3}, 0, 3);
        byte[] dv = out.getDigestValue();
        check(dv != null && dv.length == 32, "getDigestValue yields a 32-byte SHA-256 digest");
        java.security.MessageDigest md2 = java.security.MessageDigest.getInstance("SHA-256");
        md2.update(new byte[]{1, 2, 3});
        check(java.util.Arrays.equals(dv, md2.digest()), "digest equals the digest of exactly the written bytes");
        check(out.getInputStream() == null, "getInputStream is null without buffering");
        org.jcp.xml.dsig.internal.DigesterOutputStream out2 =
                new org.jcp.xml.dsig.internal.DigesterOutputStream(java.security.MessageDigest.getInstance("SHA-256"), true);
        out2.write(9);
        check(out2.getDigestValue().length == 32, "two-arg (buffered) constructor digests too");
        org.jcp.xml.dsig.internal.DigesterOutputStream out3 =
                new org.jcp.xml.dsig.internal.DigesterOutputStream(java.security.MessageDigest.getInstance("SHA-256"));
        check(out3.getDigestValue().length == 32, "digest of zero written bytes is the empty digest");
    }
}
