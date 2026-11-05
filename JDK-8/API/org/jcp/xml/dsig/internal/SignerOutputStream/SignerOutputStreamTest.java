/*
 * @test
 * @summary org.jcp.xml.dsig.internal.SignerOutputStream conformance: type shape via reflection (headless-safe).
 * @run main SignerOutputStreamTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class SignerOutputStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.SignerOutputStream", false, SignerOutputStreamTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.io.ByteArrayOutputStream"), "superclass java.io.ByteArrayOutputStream");
        check(hasMethod(c, "write"), "declares write");
        deepen();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ---- deepening: exact member face + behavior (contract lock) ----
    static void deepen() throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.SignerOutputStream");
        check("java.io.ByteArrayOutputStream".equals(c.getSuperclass().getName()), "superclass java.io.ByteArrayOutputStream");
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
        check(names.equals(new java.util.HashSet<String>(java.util.Arrays.asList("write"))),
                "exact public/protected method face (1 names): " + names);
        java.util.Set<String> ctorFace = new java.util.HashSet<String>();
        for (java.lang.reflect.Constructor<?> k : c.getDeclaredConstructors()) {
            if (java.lang.reflect.Modifier.isPrivate(k.getModifiers())) continue;
            StringBuilder sb = new StringBuilder();
            for (Class<?> t : k.getParameterTypes()) sb.append(t.getName()).append(",");
            ctorFace.add(sb.toString());
        }
        check(ctorFace.size() == 1, "exactly 1 declared constructors, got " + ctorFace.size());
        check(ctorFace.contains("java.security.Signature,"), "constructor(Signature) present");
        // black-box behavior
        java.security.KeyPairGenerator kpg = java.security.KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        java.security.KeyPair kp = kpg.generateKeyPair();
        java.security.Signature sig = java.security.Signature.getInstance("SHA256withRSA");
        sig.initSign(kp.getPrivate());
        org.jcp.xml.dsig.internal.SignerOutputStream sos = new org.jcp.xml.dsig.internal.SignerOutputStream(sig);
        check(sos instanceof java.io.ByteArrayOutputStream, "SignerOutputStream buffers via ByteArrayOutputStream");
        sos.write(new byte[]{7, 8, 9});
        byte[] produced = sig.sign();
        java.security.Signature sig2 = java.security.Signature.getInstance("SHA256withRSA");
        sig2.initSign(kp.getPrivate());
        sig2.update(new byte[]{7, 8, 9});
        check(java.util.Arrays.equals(produced, sig2.sign()), "sig.sign matches an independent signature over the same bytes");
        java.security.Signature v = java.security.Signature.getInstance("SHA256withRSA");
        v.initVerify(kp.getPublic());
        v.update(new byte[]{7, 8, 9});
        check(v.verify(produced), "the produced signature verifies against the written bytes");
    }
}
