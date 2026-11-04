/*
 * @test
 * @summary java.security.KeyStoreException conformance: white-box structure + black-box behavior.
 * @run main KeyStoreExceptionTest
 */

import java.lang.reflect.*;
import java.security.*;
import java.util.*;

public class KeyStoreExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.KeyStoreException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass().getName().equals("java.security.GeneralSecurityException"), "superclass");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        KeyStoreException e0 = new KeyStoreException();
        check(e0.getMessage() == null, "null message");
        KeyStoreException e1 = new KeyStoreException("jck keystore failure");
        check("jck keystore failure".equals(e1.getMessage()), "message");
        Throwable t = new java.io.IOException("io root");
        KeyStoreException e2 = new KeyStoreException("wrapped", t);
        check(e2.getCause() == t, "cause");
        check(e2 instanceof GeneralSecurityException, "hierarchy");
        // real API: operations on an uninitialized KeyStore throw KeyStoreException
        KeyStore ks = KeyStore.getInstance("PKCS12");
        try {
            ks.size();
            check(false, "size before load");
        } catch (KeyStoreException kse) {
            check(kse.getMessage() != null, "uninitialized size throws");
        }
        try {
            ks.aliases();
            check(false, "aliases before load");
        } catch (KeyStoreException kse) {
            check(true, "uninitialized aliases throws");
        }
        try {
            ks.containsAlias("x");
            check(false, "containsAlias before load");
        } catch (KeyStoreException kse) {
            check(true, "uninitialized containsAlias throws");
        }
        // unknown store type
        try {
            KeyStore.getInstance("JCK-NOT-A-TYPE");
            check(false, "unknown type");
        } catch (KeyStoreException kse) {
            check(true, "unknown type KeyStoreException");
        }
        // deleteEntry of a missing alias after init is a no-op success
        KeyStore init = KeyStore.getInstance("PKCS12");
        init.load(null, null);
        init.deleteEntry("no-such-alias");
        check(init.size() == 0, "delete no-op");

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
        java.security.KeyStoreException e0 = new java.security.KeyStoreException();
        check(e0.getMessage() == null, "() ctor message null");
        check(e0.getCause() == null, "() ctor cause null");
        java.security.KeyStoreException e1 = new java.security.KeyStoreException("boom");
        check("boom".equals(e1.getMessage()), "(String) ctor message exact");
        check(e1.getCause() == null, "(String) ctor cause defaults null");
        java.lang.Exception root = new java.lang.Exception("root-cause");
        java.security.KeyStoreException e2 = new java.security.KeyStoreException("m", root);
        check("m".equals(e2.getMessage()), "(String,Throwable) ctor message exact");
        check(e2.getCause() == root, "(String,Throwable) ctor cause identity");
        java.security.KeyStoreException e3 = new java.security.KeyStoreException(root);
        check(e3.getCause() == root, "(Throwable) ctor cause identity");
        check(root.toString().equals(e3.getMessage()), "(Throwable) ctor message == cause.toString()");
        check(java.security.KeyStoreException.class.getConstructors().length == 4, "public constructor count == 4");

        // 异常链不变量（Throwable 契约）
        java.lang.Exception rootC = new java.lang.Exception("r");
        check(new java.security.KeyStoreException("z").initCause(rootC).getCause() == rootC, "initCause sets cause");
        java.security.KeyStoreException once = new java.security.KeyStoreException("z");
        once.initCause(rootC);
        ct(java.lang.IllegalStateException.class, () -> once.initCause(new java.lang.Exception("c2")), "second initCause");
        java.security.KeyStoreException selfT = new java.security.KeyStoreException("z");
        ct(java.lang.IllegalArgumentException.class, () -> selfT.initCause(selfT), "initCause(this) self-reference");
        java.security.KeyStoreException fill = new java.security.KeyStoreException("z");
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
        java.security.KeyStoreException sup = new java.security.KeyStoreException("s");
        check(sup.getSuppressed().length == 0, "suppressed empty by default");
        sup.addSuppressed(new java.lang.Exception("s1"));
        check(sup.getSuppressed().length == 1
                && "s1".equals(sup.getSuppressed()[0].getMessage()), "addSuppressed/getSuppressed round-trip");
        ct(java.lang.NullPointerException.class, () -> sup.addSuppressed(null), "addSuppressed(null)");

        // toString / printStackTrace 形态
        check(new java.security.KeyStoreException().toString().equals("java.security.KeyStoreException"), "toString with null message");
        check(new java.security.KeyStoreException("q").toString().equals("java.security.KeyStoreException: q"), "toString with message");
        java.io.ByteArrayOutputStream pba = new java.io.ByteArrayOutputStream();
        new java.security.KeyStoreException("pt").printStackTrace(new java.io.PrintStream(pba));
        check(pba.toString().contains("java.security.KeyStoreException"), "printStackTrace output contains class name");
        check(!new java.security.KeyStoreException("x").equals(new java.security.KeyStoreException("x")), "identity equals: equal messages still not equal");

        // Serializable 契约：serialVersionUID 精确 + round-trip 保持 message/cause/suppressed
        check(java.io.ObjectStreamClass.lookup(java.security.KeyStoreException.class).getSerialVersionUID()
                == -1119353179322377262L, "serialVersionUID stable == -1119353179322377262");
        java.security.KeyStoreException ser = new java.security.KeyStoreException("ser-msg", new java.lang.Exception("ser-cause"));
        ser.addSuppressed(new java.lang.Exception("sup-ser"));
        java.io.ByteArrayOutputStream ob = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(ob)) { oos.writeObject(ser); }
        java.security.KeyStoreException rt = (java.security.KeyStoreException) new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(ob.toByteArray())).readObject();
        check("ser-msg".equals(rt.getMessage()), "serialization preserves message");
        check(rt.getCause() != null && "ser-cause".equals(rt.getCause().getMessage()),
                "serialization preserves cause message");
        check(rt.getSuppressed().length == 1
                && "sup-ser".equals(rt.getSuppressed()[0].getMessage()),
                "serialization preserves suppressed exceptions");

        // 类型层级（继承链精确）
        check(java.security.GeneralSecurityException.class.isAssignableFrom(java.security.KeyStoreException.class), "assignable to java.security.GeneralSecurityException");
        check(java.lang.Exception.class.isAssignableFrom(java.security.KeyStoreException.class), "assignable to java.lang.Exception");
        check(java.lang.Throwable.class.isAssignableFrom(java.security.KeyStoreException.class), "assignable to Throwable");

        // 真实抛出路径 1：未初始化（未 load）的 KeyStore 上任意操作
        java.security.KeyStore unloaded = java.security.KeyStore.getInstance("JKS");
        ct(java.security.KeyStoreException.class, unloaded::size, "uninitialized KeyStore.size() throws");
        ct(java.security.KeyStoreException.class, () -> unloaded.getKey("a", null), "uninitialized KeyStore.getKey() throws");
        ct(java.security.KeyStoreException.class, unloaded::aliases, "uninitialized KeyStore.aliases() throws");
        ct(java.security.KeyStoreException.class, () -> unloaded.isCertificateEntry("x"), "uninitialized KeyStore.isCertificateEntry() throws");
        ct(java.security.KeyStoreException.class, () -> unloaded.store(new java.io.ByteArrayOutputStream(), "pw".toCharArray()),
                "uninitialized KeyStore.store() throws");
        // 真实抛出路径 2：工厂负例（不存在的 KeyStore 类型）
        ct(java.security.KeyStoreException.class, () -> java.security.KeyStore.getInstance("BogusKS"), "getInstance(bogus type) throws");
        // 真实抛出路径 3：JKS 不接受 SecretKey（必须 PrivateKey+证书链）
        boolean badKey = false;
        try {
            javax.crypto.SecretKey sk = javax.crypto.KeyGenerator.getInstance("AES").generateKey();
            java.security.KeyStore jks = java.security.KeyStore.getInstance("JKS");
            jks.load(null, null);
            jks.setKeyEntry("s", sk, "pw".toCharArray(), null);
        } catch (java.security.KeyStoreException kse) { badKey = kse.getMessage() != null && kse.getMessage().contains("non-PrivateKey"); }
        check(badKey, "JKS setKeyEntry(SecretKey) throws KeyStoreException mentioning non-PrivateKey");
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
