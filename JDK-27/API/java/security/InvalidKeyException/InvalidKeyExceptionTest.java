/*
 * @test
 * @summary java.security.InvalidKeyException conformance: white-box structure + black-box behavior.
 * @run main InvalidKeyExceptionTest
 */

import java.lang.reflect.*;
import java.security.InvalidKeyException;
import java.security.KeyException;
import java.security.GeneralSecurityException;

public class InvalidKeyExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.InvalidKeyException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass().getName().equals("java.security.KeyException"), "superclass");
        check(c.getInterfaces().length == 0, "no interfaces");
        check(c.getDeclaredConstructors().length == 4, "constructor count");
        check(c.getDeclaredMethods().length == 0, "no declared methods");
    }
    static void testBlackBox() {
        InvalidKeyException e = new InvalidKeyException();
        check(e != null, "no-arg non-null");
        check(e.getMessage() == null, "no-arg getMessage null");
        check(e.getCause() == null, "no-arg getCause null");

        InvalidKeyException m = new InvalidKeyException("boom");
        check("boom".equals(m.getMessage()), "String ctor message");
        check(m.getCause() == null, "String ctor getCause null");

        Throwable t = new RuntimeException("cause");
        InvalidKeyException st = new InvalidKeyException("msg", t);
        check("msg".equals(st.getMessage()), "String+Throwable ctor message");
        check(st.getCause() == t, "String+Throwable ctor cause");

        InvalidKeyException tt = new InvalidKeyException(t);
        check(tt.getCause() == t, "Throwable ctor cause");
        check(tt.getMessage() != null, "Throwable ctor message non-null");

        check(e instanceof KeyException, "instanceof KeyException");
        check(e instanceof GeneralSecurityException, "instanceof GeneralSecurityException");
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
        java.security.InvalidKeyException e0 = new java.security.InvalidKeyException();
        check(e0.getMessage() == null, "() ctor message null");
        check(e0.getCause() == null, "() ctor cause null");
        java.security.InvalidKeyException e1 = new java.security.InvalidKeyException("boom");
        check("boom".equals(e1.getMessage()), "(String) ctor message exact");
        check(e1.getCause() == null, "(String) ctor cause defaults null");
        java.lang.Exception root = new java.lang.Exception("root-cause");
        java.security.InvalidKeyException e2 = new java.security.InvalidKeyException("m", root);
        check("m".equals(e2.getMessage()), "(String,Throwable) ctor message exact");
        check(e2.getCause() == root, "(String,Throwable) ctor cause identity");
        java.security.InvalidKeyException e3 = new java.security.InvalidKeyException(root);
        check(e3.getCause() == root, "(Throwable) ctor cause identity");
        check(root.toString().equals(e3.getMessage()), "(Throwable) ctor message == cause.toString()");
        check(java.security.InvalidKeyException.class.getConstructors().length == 4, "public constructor count == 4");

        // 异常链不变量（Throwable 契约）
        java.lang.Exception rootC = new java.lang.Exception("r");
        check(new java.security.InvalidKeyException("z").initCause(rootC).getCause() == rootC, "initCause sets cause");
        java.security.InvalidKeyException once = new java.security.InvalidKeyException("z");
        once.initCause(rootC);
        ct(java.lang.IllegalStateException.class, () -> once.initCause(new java.lang.Exception("c2")), "second initCause");
        java.security.InvalidKeyException selfT = new java.security.InvalidKeyException("z");
        ct(java.lang.IllegalArgumentException.class, () -> selfT.initCause(selfT), "initCause(this) self-reference");
        java.security.InvalidKeyException fill = new java.security.InvalidKeyException("z");
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
        java.security.InvalidKeyException sup = new java.security.InvalidKeyException("s");
        check(sup.getSuppressed().length == 0, "suppressed empty by default");
        sup.addSuppressed(new java.lang.Exception("s1"));
        check(sup.getSuppressed().length == 1
                && "s1".equals(sup.getSuppressed()[0].getMessage()), "addSuppressed/getSuppressed round-trip");
        ct(java.lang.NullPointerException.class, () -> sup.addSuppressed(null), "addSuppressed(null)");

        // toString / printStackTrace 形态
        check(new java.security.InvalidKeyException().toString().equals("java.security.InvalidKeyException"), "toString with null message");
        check(new java.security.InvalidKeyException("q").toString().equals("java.security.InvalidKeyException: q"), "toString with message");
        java.io.ByteArrayOutputStream pba = new java.io.ByteArrayOutputStream();
        new java.security.InvalidKeyException("pt").printStackTrace(new java.io.PrintStream(pba));
        check(pba.toString().contains("java.security.InvalidKeyException"), "printStackTrace output contains class name");
        check(!new java.security.InvalidKeyException("x").equals(new java.security.InvalidKeyException("x")), "identity equals: equal messages still not equal");

        // Serializable 契约：serialVersionUID 精确 + round-trip 保持 message/cause/suppressed
        check(java.io.ObjectStreamClass.lookup(java.security.InvalidKeyException.class).getSerialVersionUID()
                == 5698479920593359816L, "serialVersionUID stable == 5698479920593359816");
        java.security.InvalidKeyException ser = new java.security.InvalidKeyException("ser-msg", new java.lang.Exception("ser-cause"));
        ser.addSuppressed(new java.lang.Exception("sup-ser"));
        java.io.ByteArrayOutputStream ob = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(ob)) { oos.writeObject(ser); }
        java.security.InvalidKeyException rt = (java.security.InvalidKeyException) new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(ob.toByteArray())).readObject();
        check("ser-msg".equals(rt.getMessage()), "serialization preserves message");
        check(rt.getCause() != null && "ser-cause".equals(rt.getCause().getMessage()),
                "serialization preserves cause message");
        check(rt.getSuppressed().length == 1
                && "sup-ser".equals(rt.getSuppressed()[0].getMessage()),
                "serialization preserves suppressed exceptions");

        // 类型层级（继承链精确）
        check(java.security.KeyException.class.isAssignableFrom(java.security.InvalidKeyException.class), "assignable to java.security.KeyException");
        check(java.security.GeneralSecurityException.class.isAssignableFrom(java.security.InvalidKeyException.class), "assignable to java.security.GeneralSecurityException");
        check(java.lang.Exception.class.isAssignableFrom(java.security.InvalidKeyException.class), "assignable to java.lang.Exception");
        check(java.lang.Throwable.class.isAssignableFrom(java.security.InvalidKeyException.class), "assignable to Throwable");

        // 真实抛出路径：Signature 装入不匹配/伪造密钥
        ct(java.security.InvalidKeyException.class, () -> java.security.Signature.getInstance("SHA256withRSA").initSign(new FakePrivKey()),
                "initSign with unsupported key type throws InvalidKeyException");
        ct(java.security.InvalidKeyException.class, () -> java.security.Signature.getInstance("SHA256withRSA").initVerify((java.security.PublicKey) null),
                "initVerify(null PublicKey) throws InvalidKeyException");
        // 多态 catch：作为 KeyException / GeneralSecurityException
        boolean asKey = false, asGse = false;
        try { java.security.Signature.getInstance("SHA256withRSA").initVerify((java.security.PublicKey) null); }
        catch (java.security.KeyException ke) { asKey = true; }
        check(asKey, "InvalidKeyException catchable as KeyException");
        try { java.security.Signature.getInstance("SHA256withRSA").initVerify((java.security.PublicKey) null); }
        catch (java.security.GeneralSecurityException gse) { asGse = true; }
        check(asGse, "InvalidKeyException catchable as GeneralSecurityException");
    }
    // 伪造密钥：算法名不被任何已安装 provider 支持
    static class FakePrivKey implements java.security.PrivateKey {
        public String getAlgorithm() { return "FAKE"; }
        public String getFormat() { return "RAW"; }
        public byte[] getEncoded() { return new byte[]{1, 2, 3}; }
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
