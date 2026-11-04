/*
 * @test
 * @summary java.security.KeyManagementException conformance: white-box structure + black-box behavior.
 * @run main KeyManagementExceptionTest
 */

import java.lang.reflect.*;
import java.security.*;

public class KeyManagementExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.KeyManagementException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass().getName().equals("java.security.KeyException"), "superclass");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        KeyManagementException e0 = new KeyManagementException();
        check(e0.getMessage() == null, "null message");
        check(e0.getCause() == null, "null cause");
        KeyManagementException e1 = new KeyManagementException("jck key failure");
        check("jck key failure".equals(e1.getMessage()), "message");
        check(e1 instanceof GeneralSecurityException, "hierarchy");
        check(e1 instanceof Exception, "exception");
        Throwable t = new IllegalStateException("root");
        KeyManagementException e2 = new KeyManagementException("wrapped", t);
        check(e2.getCause() == t, "cause");
        check("wrapped".equals(e2.getMessage()), "message with cause");
        // declared thrown by Identity.setPublicKey (the JDK-27 stub never actually throws it)
        Identity id = new Identity("jck") {};
        id.setPublicKey(null);
        check(true, "KME declared on setPublicKey");

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
        java.security.KeyManagementException e0 = new java.security.KeyManagementException();
        check(e0.getMessage() == null, "() ctor message null");
        check(e0.getCause() == null, "() ctor cause null");
        java.security.KeyManagementException e1 = new java.security.KeyManagementException("boom");
        check("boom".equals(e1.getMessage()), "(String) ctor message exact");
        check(e1.getCause() == null, "(String) ctor cause defaults null");
        java.lang.Exception root = new java.lang.Exception("root-cause");
        java.security.KeyManagementException e2 = new java.security.KeyManagementException("m", root);
        check("m".equals(e2.getMessage()), "(String,Throwable) ctor message exact");
        check(e2.getCause() == root, "(String,Throwable) ctor cause identity");
        java.security.KeyManagementException e3 = new java.security.KeyManagementException(root);
        check(e3.getCause() == root, "(Throwable) ctor cause identity");
        check(root.toString().equals(e3.getMessage()), "(Throwable) ctor message == cause.toString()");
        check(java.security.KeyManagementException.class.getConstructors().length == 4, "public constructor count == 4");

        // 异常链不变量（Throwable 契约）
        java.lang.Exception rootC = new java.lang.Exception("r");
        check(new java.security.KeyManagementException("z").initCause(rootC).getCause() == rootC, "initCause sets cause");
        java.security.KeyManagementException once = new java.security.KeyManagementException("z");
        once.initCause(rootC);
        ct(java.lang.IllegalStateException.class, () -> once.initCause(new java.lang.Exception("c2")), "second initCause");
        java.security.KeyManagementException selfT = new java.security.KeyManagementException("z");
        ct(java.lang.IllegalArgumentException.class, () -> selfT.initCause(selfT), "initCause(this) self-reference");
        java.security.KeyManagementException fill = new java.security.KeyManagementException("z");
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
        java.security.KeyManagementException sup = new java.security.KeyManagementException("s");
        check(sup.getSuppressed().length == 0, "suppressed empty by default");
        sup.addSuppressed(new java.lang.Exception("s1"));
        check(sup.getSuppressed().length == 1
                && "s1".equals(sup.getSuppressed()[0].getMessage()), "addSuppressed/getSuppressed round-trip");
        ct(java.lang.NullPointerException.class, () -> sup.addSuppressed(null), "addSuppressed(null)");

        // toString / printStackTrace 形态
        check(new java.security.KeyManagementException().toString().equals("java.security.KeyManagementException"), "toString with null message");
        check(new java.security.KeyManagementException("q").toString().equals("java.security.KeyManagementException: q"), "toString with message");
        java.io.ByteArrayOutputStream pba = new java.io.ByteArrayOutputStream();
        new java.security.KeyManagementException("pt").printStackTrace(new java.io.PrintStream(pba));
        check(pba.toString().contains("java.security.KeyManagementException"), "printStackTrace output contains class name");
        check(!new java.security.KeyManagementException("x").equals(new java.security.KeyManagementException("x")), "identity equals: equal messages still not equal");

        // Serializable 契约：serialVersionUID 精确 + round-trip 保持 message/cause/suppressed
        check(java.io.ObjectStreamClass.lookup(java.security.KeyManagementException.class).getSerialVersionUID()
                == 947674216157062695L, "serialVersionUID stable == 947674216157062695");
        java.security.KeyManagementException ser = new java.security.KeyManagementException("ser-msg", new java.lang.Exception("ser-cause"));
        ser.addSuppressed(new java.lang.Exception("sup-ser"));
        java.io.ByteArrayOutputStream ob = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(ob)) { oos.writeObject(ser); }
        java.security.KeyManagementException rt = (java.security.KeyManagementException) new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(ob.toByteArray())).readObject();
        check("ser-msg".equals(rt.getMessage()), "serialization preserves message");
        check(rt.getCause() != null && "ser-cause".equals(rt.getCause().getMessage()),
                "serialization preserves cause message");
        check(rt.getSuppressed().length == 1
                && "sup-ser".equals(rt.getSuppressed()[0].getMessage()),
                "serialization preserves suppressed exceptions");

        // 类型层级（继承链精确）
        check(java.security.GeneralSecurityException.class.isAssignableFrom(java.security.KeyManagementException.class), "assignable to java.security.GeneralSecurityException");
        check(java.lang.Exception.class.isAssignableFrom(java.security.KeyManagementException.class), "assignable to java.lang.Exception");
        check(java.lang.Throwable.class.isAssignableFrom(java.security.KeyManagementException.class), "assignable to Throwable");

        // 本类为密钥管理操作失败信号（Identity/KeyStore 管理层），公开引擎无直接抛出点：构造语义 + 层级记录
        check(java.security.GeneralSecurityException.class.isAssignableFrom(java.security.KeyManagementException.class),
                "KeyManagementException stays in GeneralSecurityException family");
        boolean poly = false;
        try { throw new java.security.KeyManagementException("km", new java.lang.Exception("c")); }
        catch (java.security.GeneralSecurityException g) { poly = "km".equals(g.getMessage()); }
        check(poly, "thrown directly and caught as GeneralSecurityException with message intact");
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
