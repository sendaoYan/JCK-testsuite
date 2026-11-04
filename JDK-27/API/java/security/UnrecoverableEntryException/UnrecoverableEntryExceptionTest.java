/*
 * @test
 * @summary java.security.UnrecoverableEntryException conformance: white-box structure + black-box behavior.
 * @run main UnrecoverableEntryExceptionTest
 */

import java.lang.reflect.*;
import java.security.*;

public class UnrecoverableEntryExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.UnrecoverableEntryException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass().getName().equals("java.security.GeneralSecurityException"), "superclass");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        UnrecoverableEntryException e0 = new UnrecoverableEntryException();
        check(e0.getMessage() == null, "null message");
        UnrecoverableEntryException e1 = new UnrecoverableEntryException("jck entry failure");
        check("jck entry failure".equals(e1.getMessage()), "message");
        check(e1 instanceof GeneralSecurityException, "hierarchy");
        // subclasses
        check(new UnrecoverableKeyException() instanceof UnrecoverableEntryException, "key subtype");
        // no cause constructor in the API: cause stays null
        check(e1.getCause() == null, "null cause");
        // thrown indirectly by KeyStore.getEntry with a wrong password
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(null, null);
        javax.crypto.SecretKey sk = javax.crypto.KeyGenerator.getInstance("AES").generateKey();
        ks.setEntry("s", new KeyStore.SecretKeyEntry(sk), new KeyStore.PasswordProtection("right".toCharArray()));
        try {
            ks.getEntry("s", new KeyStore.PasswordProtection("wrong".toCharArray()));
            check(false, "wrong entry password");
        } catch (UnrecoverableEntryException uee) {
            check(true, "getEntry wrong password");
        }
        check(ks.getEntry("s", new KeyStore.PasswordProtection("right".toCharArray())) != null, "right password");

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
        java.security.UnrecoverableEntryException e0 = new java.security.UnrecoverableEntryException();
        check(e0.getMessage() == null, "() ctor message null");
        check(e0.getCause() == null, "() ctor cause null");
        java.security.UnrecoverableEntryException e1 = new java.security.UnrecoverableEntryException("boom");
        check("boom".equals(e1.getMessage()), "(String) ctor message exact");
        check(e1.getCause() == null, "(String) ctor cause defaults null");
        check(java.security.UnrecoverableEntryException.class.getConstructors().length == 2, "public constructor count == 2");

        // 异常链不变量（Throwable 契约）
        java.lang.Exception rootC = new java.lang.Exception("r");
        check(new java.security.UnrecoverableEntryException("z").initCause(rootC).getCause() == rootC, "initCause sets cause");
        java.security.UnrecoverableEntryException once = new java.security.UnrecoverableEntryException("z");
        once.initCause(rootC);
        ct(java.lang.IllegalStateException.class, () -> once.initCause(new java.lang.Exception("c2")), "second initCause");
        java.security.UnrecoverableEntryException selfT = new java.security.UnrecoverableEntryException("z");
        ct(java.lang.IllegalArgumentException.class, () -> selfT.initCause(selfT), "initCause(this) self-reference");
        java.security.UnrecoverableEntryException fill = new java.security.UnrecoverableEntryException("z");
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
        java.security.UnrecoverableEntryException sup = new java.security.UnrecoverableEntryException("s");
        check(sup.getSuppressed().length == 0, "suppressed empty by default");
        sup.addSuppressed(new java.lang.Exception("s1"));
        check(sup.getSuppressed().length == 1
                && "s1".equals(sup.getSuppressed()[0].getMessage()), "addSuppressed/getSuppressed round-trip");
        ct(java.lang.NullPointerException.class, () -> sup.addSuppressed(null), "addSuppressed(null)");

        // toString / printStackTrace 形态
        check(new java.security.UnrecoverableEntryException().toString().equals("java.security.UnrecoverableEntryException"), "toString with null message");
        check(new java.security.UnrecoverableEntryException("q").toString().equals("java.security.UnrecoverableEntryException: q"), "toString with message");
        java.io.ByteArrayOutputStream pba = new java.io.ByteArrayOutputStream();
        new java.security.UnrecoverableEntryException("pt").printStackTrace(new java.io.PrintStream(pba));
        check(pba.toString().contains("java.security.UnrecoverableEntryException"), "printStackTrace output contains class name");
        check(!new java.security.UnrecoverableEntryException("x").equals(new java.security.UnrecoverableEntryException("x")), "identity equals: equal messages still not equal");

        // Serializable 契约：serialVersionUID 精确 + round-trip 保持 message/cause/suppressed
        check(java.io.ObjectStreamClass.lookup(java.security.UnrecoverableEntryException.class).getSerialVersionUID()
                == -4527142945246286535L, "serialVersionUID stable == -4527142945246286535");
        java.security.UnrecoverableEntryException ser = new java.security.UnrecoverableEntryException("ser-msg");
        ser.addSuppressed(new java.lang.Exception("sup-ser"));
        java.io.ByteArrayOutputStream ob = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(ob)) { oos.writeObject(ser); }
        java.security.UnrecoverableEntryException rt = (java.security.UnrecoverableEntryException) new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(ob.toByteArray())).readObject();
        check("ser-msg".equals(rt.getMessage()), "serialization preserves message");
        check(rt.getSuppressed().length == 1
                && "sup-ser".equals(rt.getSuppressed()[0].getMessage()),
                "serialization preserves suppressed exceptions");

        // 类型层级（继承链精确）
        check(java.security.GeneralSecurityException.class.isAssignableFrom(java.security.UnrecoverableEntryException.class), "assignable to java.security.GeneralSecurityException");
        check(java.lang.Exception.class.isAssignableFrom(java.security.UnrecoverableEntryException.class), "assignable to java.lang.Exception");
        check(java.lang.Throwable.class.isAssignableFrom(java.security.UnrecoverableEntryException.class), "assignable to Throwable");

        // 层级：UnrecoverableKeyException 的直接父类；同族多态捕获
        check(java.security.UnrecoverableEntryException.class.isAssignableFrom(java.security.UnrecoverableKeyException.class),
                "UnrecoverableKeyException is an UnrecoverableEntryException");
        boolean asUEE = false, asGSE = false;
        try {
            javax.crypto.SecretKey sk = javax.crypto.KeyGenerator.getInstance("AES").generateKey();
            java.security.KeyStore jceks = java.security.KeyStore.getInstance("JCEKS");
            jceks.load(null, null);
            jceks.setKeyEntry("s", sk, "right".toCharArray(), null);
            jceks.getKey("s", "wrong".toCharArray());
        } catch (java.security.UnrecoverableEntryException ue) { asUEE = true; }
        check(asUEE, "wrong-password UnrecoverableKeyException catchable as UnrecoverableEntryException");
        try {
            javax.crypto.SecretKey sk = javax.crypto.KeyGenerator.getInstance("AES").generateKey();
            java.security.KeyStore jceks = java.security.KeyStore.getInstance("JCEKS");
            jceks.load(null, null);
            jceks.setKeyEntry("s", sk, "right".toCharArray(), null);
            jceks.getKey("s", "wrong".toCharArray());
        } catch (java.security.GeneralSecurityException gse) { asGSE = true; }
        check(asGSE, "same path catchable as GeneralSecurityException");
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
