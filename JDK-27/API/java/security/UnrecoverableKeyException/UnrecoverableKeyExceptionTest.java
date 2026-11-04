/*
 * @test
 * @summary java.security.UnrecoverableKeyException conformance: white-box structure + black-box behavior.
 * @run main UnrecoverableKeyExceptionTest
 */

import java.lang.reflect.*;
import java.security.*;
import java.security.cert.CertificateFactory;
import java.io.ByteArrayInputStream;
import java.util.*;

public class UnrecoverableKeyExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.UnrecoverableKeyException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass().getName().equals("java.security.UnrecoverableEntryException"), "superclass");
    }

    // Fixed fixtures, generated once with JDK-27 keytool (RSA-2048 self-signed certs CN=jck-root / CN=jck-leaf,
    // validity 2026-11-03T04:09:52Z..2036-10-31T04:09:52Z, sig SHA384withRSA, root serial 0x70bdab2c498601da,
    // leaf serial 0xbc0944b4fd45f023; CRL v1 issued by jck-root, thisUpdate 2026-11-03, nextUpdate 2027-02-01,
    // revoked serial 0; PKCS#8 DER of the root private key). Tests decode at runtime; no external files.

    static final String ROOT_CERT_B64 =
        "MIIDLjCCAhagAwIBAgIIcL2rLEmGAdowDQYJKoZIhvcNAQEMBQAwPTELMAkGA1UEBhMC" + "\n" +
        "VVMxDTALBgNVBAoTBFRlc3QxDDAKBgNVBAsTA0pDSzERMA8GA1UEAxMIamNrLXJvb3Qw" + "\n" +
        "HhcNMjYxMTAzMDQwOTUyWhcNMzYxMDMxMDQwOTUyWjA9MQswCQYDVQQGEwJVUzENMAsG" + "\n" +
        "A1UEChMEVGVzdDEMMAoGA1UECxMDSkNLMREwDwYDVQQDEwhqY2stcm9vdDCCASIwDQYJ" + "\n" +
        "KoZIhvcNAQEBBQADggEPADCCAQoCggEBALrM4ukZtyCiH9zsjkRMgOt6EYkhMv8IXY7t" + "\n" +
        "wYzWwLJnR9ED7BnleYFAaeSH17SWUAPdUBTH+ig57+2DI2Te3IJo974rgHjq94UXk5ez" + "\n" +
        "y1aUlrpe4SV+1NCnA/zg2E3lIOIaObZrTbBTT16jveIuX3Rqjbz9/z6Xic3Wvn2CqBNp" + "\n" +
        "1rnF1/GqhN6/6O0I+8el3ulMXzqmc21YPQuvpJDYuR1026ht3vqZhuY3elLEdvTQfOQa" + "\n" +
        "5w4xNGg6BlXycQsOXXP7Uqk8s5fTxBEKhR9gfABehJTuHwqEkeaQgT44xtgeaEOnbqH2" + "\n" +
        "thm0cucWQnSsGOJdR2IZ6RYVP+ix4hOZ3vsCAwEAAaMyMDAwHQYDVR0OBBYEFI92/CSa" + "\n" +
        "lBAhQnaSMXtuaF3G4f9TMA8GA1UdEwEB/wQFMAMBAf8wDQYJKoZIhvcNAQEMBQADggEB" + "\n" +
        "AJy35loMlTYxvzsaqqlXMBOuaMULP/qZXyiZhX0H3sKtYxZVBdHLS/CG2k4mXtOQHNqq" + "\n" +
        "Gz+JQo0LS4VoxNCPrAky9tvubH4eSoF3xGJIdThVDBhcn0T2rYjb9z52xA1p+GIoc9Gi" + "\n" +
        "Fv6vh1l4OkhLq9ymIwnM+CjBUNOW6fwojo7c7o/qxk0+qPijanzhxh9ffIrRqQmQom9W" + "\n" +
        "pelY03y0XZKuAGxb0cuRBOCkH4Jg0RJhbJoR08wn3jEJdQ7t5I65VcOjJ0KzyRzV/g3V" + "\n" +
        "C0Wwnv9j7RSSuxdcwrNYVm3vgDQiZWlXDcYJob2fn2H+q0hohK0mdyw43fyDkxSsWiFq" + "\n" +
        "UF8=";
    static byte[] fromB64(String s) { return Base64.getMimeDecoder().decode(s); }
    static java.security.cert.X509Certificate fixtureCert(String b64) throws Exception {
        return (java.security.cert.X509Certificate) java.security.cert.CertificateFactory.getInstance("X.509")
            .generateCertificate(new java.io.ByteArrayInputStream(fromB64(b64)));
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        UnrecoverableKeyException e0 = new UnrecoverableKeyException();
        check(e0.getMessage() == null, "null message");
        UnrecoverableKeyException e1 = new UnrecoverableKeyException("jck unrecoverable");
        check("jck unrecoverable".equals(e1.getMessage()), "message");
        check(e1 instanceof GeneralSecurityException, "hierarchy");
        // UnrecoverableKeyException extends UnrecoverableEntryException
        check(e1 instanceof UnrecoverableEntryException, "entry subtype");
        // real API: wrong key password on a PKCS12 key entry
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(null, null);
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        java.security.cert.Certificate cert = fixtureCert(ROOT_CERT_B64);
        ks.setKeyEntry("k", kp.getPrivate(), "right".toCharArray(), new java.security.cert.Certificate[]{cert});
        try {
            ks.getKey("k", "wrong".toCharArray());
            check(false, "wrong password");
        } catch (UnrecoverableKeyException uke) {
            check(true, "wrong key password UnrecoverableKeyException");
        }
        check(ks.getKey("k", "right".toCharArray()).equals(kp.getPrivate()), "right password recovers");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
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
    // ---- 契约深化：构造矩阵 / 异常链不变量 / 栈帧与抑制 / 序列化 / 层级 ----
    static void testContractDeep() throws Exception {
        // 构造矩阵与 getter 精确往返
        java.security.UnrecoverableKeyException e0 = new java.security.UnrecoverableKeyException();
        check(e0.getMessage() == null, "() ctor message null");
        check(e0.getCause() == null, "() ctor cause null");
        java.security.UnrecoverableKeyException e1 = new java.security.UnrecoverableKeyException("boom");
        check("boom".equals(e1.getMessage()), "(String) ctor message exact");
        check(e1.getCause() == null, "(String) ctor cause defaults null");
        check(java.security.UnrecoverableKeyException.class.getConstructors().length == 2, "public constructor count == 2");

        // 异常链不变量（Throwable 契约）
        java.lang.Exception rootC = new java.lang.Exception("r");
        check(new java.security.UnrecoverableKeyException("z").initCause(rootC).getCause() == rootC, "initCause sets cause");
        java.security.UnrecoverableKeyException once = new java.security.UnrecoverableKeyException("z");
        once.initCause(rootC);
        ct(java.lang.IllegalStateException.class, () -> once.initCause(new java.lang.Exception("c2")), "second initCause");
        java.security.UnrecoverableKeyException selfT = new java.security.UnrecoverableKeyException("z");
        ct(java.lang.IllegalArgumentException.class, () -> selfT.initCause(selfT), "initCause(this) self-reference");
        java.security.UnrecoverableKeyException fill = new java.security.UnrecoverableKeyException("z");
        check(fill.fillInStackTrace() == fill, "fillInStackTrace returns this");

        // 栈帧与抑制异常
        fill.setStackTrace(new java.lang.StackTraceElement[]
                {new java.lang.StackTraceElement("Cls", "mth", "Cls.java", 7)});
        check(fill.getStackTrace().length == 1
                && fill.getStackTrace()[0].getLineNumber() == 7, "setStackTrace/getStackTrace round-trip");
        java.lang.StackTraceElement[] got = fill.getStackTrace();
        int gotLen = got.length;
        got = new java.lang.StackTraceElement[0];
        check(fill.getStackTrace().length == gotLen, "getStackTrace returns defensive copy");
        java.security.UnrecoverableKeyException sup = new java.security.UnrecoverableKeyException("s");
        check(sup.getSuppressed().length == 0, "suppressed empty by default");
        sup.addSuppressed(new java.lang.Exception("s1"));
        check(sup.getSuppressed().length == 1
                && "s1".equals(sup.getSuppressed()[0].getMessage()), "addSuppressed/getSuppressed round-trip");
        ct(java.lang.NullPointerException.class, () -> sup.addSuppressed(null), "addSuppressed(null)");

        // toString / printStackTrace 形态
        check(new java.security.UnrecoverableKeyException().toString().equals("java.security.UnrecoverableKeyException"), "toString with null message");
        check(new java.security.UnrecoverableKeyException("q").toString().equals("java.security.UnrecoverableKeyException: q"), "toString with message");
        java.io.ByteArrayOutputStream pba = new java.io.ByteArrayOutputStream();
        new java.security.UnrecoverableKeyException("pt").printStackTrace(new java.io.PrintStream(pba));
        check(pba.toString().contains("java.security.UnrecoverableKeyException"), "printStackTrace output contains class name");
        check(!new java.security.UnrecoverableKeyException("x").equals(new java.security.UnrecoverableKeyException("x")), "identity equals: equal messages still not equal");

        // Serializable 契约：serialVersionUID 精确 + round-trip 保持 message/cause/suppressed
        check(java.io.ObjectStreamClass.lookup(java.security.UnrecoverableKeyException.class).getSerialVersionUID()
                == 7275063078190151277L, "serialVersionUID stable == 7275063078190151277");
        java.security.UnrecoverableKeyException ser = new java.security.UnrecoverableKeyException("ser-msg");
        ser.addSuppressed(new java.lang.Exception("sup-ser"));
        java.io.ByteArrayOutputStream ob = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(ob)) { oos.writeObject(ser); }
        java.security.UnrecoverableKeyException rt = (java.security.UnrecoverableKeyException) new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(ob.toByteArray())).readObject();
        check("ser-msg".equals(rt.getMessage()), "serialization preserves message");
        check(rt.getSuppressed().length == 1
                && "sup-ser".equals(rt.getSuppressed()[0].getMessage()),
                "serialization preserves suppressed exceptions");

        // 类型层级（继承链精确）
        check(java.security.UnrecoverableEntryException.class.isAssignableFrom(java.security.UnrecoverableKeyException.class), "assignable to java.security.UnrecoverableEntryException");
        check(java.security.GeneralSecurityException.class.isAssignableFrom(java.security.UnrecoverableKeyException.class), "assignable to java.security.GeneralSecurityException");
        check(java.lang.Exception.class.isAssignableFrom(java.security.UnrecoverableKeyException.class), "assignable to java.lang.Exception");
        check(java.lang.Throwable.class.isAssignableFrom(java.security.UnrecoverableKeyException.class), "assignable to Throwable");

        // 真实抛出路径：JCEKS 密钥库错误口令取密钥 / 取条目 / 缺口令保护
        boolean wrongGetKey = false;
        try {
            javax.crypto.SecretKey sk = javax.crypto.KeyGenerator.getInstance("AES").generateKey();
            java.security.KeyStore jceks = java.security.KeyStore.getInstance("JCEKS");
            jceks.load(null, null);
            jceks.setKeyEntry("s", sk, "right".toCharArray(), null);
            check(jceks.getKey("s", "right".toCharArray()) != null, "correct password recovers key");
            jceks.getKey("s", "wrong".toCharArray());
        } catch (java.security.UnrecoverableKeyException e) { wrongGetKey = true; }
        check(wrongGetKey, "getKey with wrong password throws UnrecoverableKeyException");
        boolean wrongEntry = false;
        try {
            javax.crypto.SecretKey sk = javax.crypto.KeyGenerator.getInstance("AES").generateKey();
            java.security.KeyStore jceks = java.security.KeyStore.getInstance("JCEKS");
            jceks.load(null, null);
            jceks.setEntry("e", new java.security.KeyStore.SecretKeyEntry(sk),
                    new java.security.KeyStore.PasswordProtection("right".toCharArray()));
            jceks.getEntry("e", new java.security.KeyStore.PasswordProtection("wrong".toCharArray()));
        } catch (java.security.UnrecoverableKeyException e) { wrongEntry = true; }
        check(wrongEntry, "getEntry with wrong password throws UnrecoverableKeyException");
        boolean nullProt = false;
        try {
            javax.crypto.SecretKey sk = javax.crypto.KeyGenerator.getInstance("AES").generateKey();
            java.security.KeyStore jceks = java.security.KeyStore.getInstance("JCEKS");
            jceks.load(null, null);
            jceks.setEntry("e", new java.security.KeyStore.SecretKeyEntry(sk),
                    new java.security.KeyStore.PasswordProtection("right".toCharArray()));
            jceks.getEntry("e", null);
        } catch (java.security.UnrecoverableKeyException e) { nullProt = e.getMessage() != null && e.getMessage().contains("password"); }
        check(nullProt, "getEntry with null protection throws UKE mentioning password");
        boolean asUEE = false;
        try {
            javax.crypto.SecretKey sk = javax.crypto.KeyGenerator.getInstance("AES").generateKey();
            java.security.KeyStore jceks = java.security.KeyStore.getInstance("JCEKS");
            jceks.load(null, null);
            jceks.setKeyEntry("s", sk, "right".toCharArray(), null);
            jceks.getKey("s", "wrong".toCharArray());
        } catch (java.security.UnrecoverableEntryException ue) { asUEE = true; }
        check(asUEE, "UKE catchable polymorphically as UnrecoverableEntryException");
    }

    // ---- 契约深化公共 helper ----
    interface TR { void run() throws Throwable; }
    static void ct(Class<? extends Throwable> expected, TR r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName() + " but nothing was thrown");
    }

}
