/*
 * @test
 * @summary java.security.InvalidParameterException conformance: constructors, message, cause and superclass.
 * @run main InvalidParameterExceptionTest
 */

public class InvalidParameterExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.InvalidParameterException e = new java.security.InvalidParameterException("boom");
        check("boom".equals(e.getMessage()), "String ctor sets message");
        check(e instanceof java.lang.IllegalArgumentException, "extends java.lang.IllegalArgumentException");
        java.security.InvalidParameterException e4 = new java.security.InvalidParameterException();
        check(e4.getMessage() == null, "no-arg ctor message null");

        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ---- 契约深化：构造矩阵 / 异常链不变量 / 栈帧与抑制 / 序列化 / 层级 ----
    static void testContractDeep() throws Exception {
        // 构造矩阵与 getter 精确往返
        java.security.InvalidParameterException e0 = new java.security.InvalidParameterException();
        check(e0.getMessage() == null, "() ctor message null");
        check(e0.getCause() == null, "() ctor cause null");
        java.security.InvalidParameterException e1 = new java.security.InvalidParameterException("boom");
        check("boom".equals(e1.getMessage()), "(String) ctor message exact");
        check(e1.getCause() == null, "(String) ctor cause defaults null");
        check(java.security.InvalidParameterException.class.getConstructors().length == 2, "public constructor count == 2");

        // 异常链不变量（Throwable 契约）
        java.lang.Exception rootC = new java.lang.Exception("r");
        check(new java.security.InvalidParameterException("z").initCause(rootC).getCause() == rootC, "initCause sets cause");
        java.security.InvalidParameterException once = new java.security.InvalidParameterException("z");
        once.initCause(rootC);
        ct(java.lang.IllegalStateException.class, () -> once.initCause(new java.lang.Exception("c2")), "second initCause");
        java.security.InvalidParameterException selfT = new java.security.InvalidParameterException("z");
        ct(java.lang.IllegalArgumentException.class, () -> selfT.initCause(selfT), "initCause(this) self-reference");
        java.security.InvalidParameterException fill = new java.security.InvalidParameterException("z");
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
        java.security.InvalidParameterException sup = new java.security.InvalidParameterException("s");
        check(sup.getSuppressed().length == 0, "suppressed empty by default");
        sup.addSuppressed(new java.lang.Exception("s1"));
        check(sup.getSuppressed().length == 1
                && "s1".equals(sup.getSuppressed()[0].getMessage()), "addSuppressed/getSuppressed round-trip");
        ct(java.lang.NullPointerException.class, () -> sup.addSuppressed(null), "addSuppressed(null)");

        // toString / printStackTrace 形态
        check(new java.security.InvalidParameterException().toString().equals("java.security.InvalidParameterException"), "toString with null message");
        check(new java.security.InvalidParameterException("q").toString().equals("java.security.InvalidParameterException: q"), "toString with message");
        java.io.ByteArrayOutputStream pba = new java.io.ByteArrayOutputStream();
        new java.security.InvalidParameterException("pt").printStackTrace(new java.io.PrintStream(pba));
        check(pba.toString().contains("java.security.InvalidParameterException"), "printStackTrace output contains class name");
        check(!new java.security.InvalidParameterException("x").equals(new java.security.InvalidParameterException("x")), "identity equals: equal messages still not equal");

        // Serializable 契约：serialVersionUID 精确 + round-trip 保持 message/cause/suppressed
        check(java.io.ObjectStreamClass.lookup(java.security.InvalidParameterException.class).getSerialVersionUID()
                == -857968536935667808L, "serialVersionUID stable == -857968536935667808");
        java.security.InvalidParameterException ser = new java.security.InvalidParameterException("ser-msg");
        ser.addSuppressed(new java.lang.Exception("sup-ser"));
        java.io.ByteArrayOutputStream ob = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(ob)) { oos.writeObject(ser); }
        java.security.InvalidParameterException rt = (java.security.InvalidParameterException) new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(ob.toByteArray())).readObject();
        check("ser-msg".equals(rt.getMessage()), "serialization preserves message");
        check(rt.getSuppressed().length == 1
                && "sup-ser".equals(rt.getSuppressed()[0].getMessage()),
                "serialization preserves suppressed exceptions");

        // 类型层级（继承链精确）
        check(java.lang.IllegalArgumentException.class.isAssignableFrom(java.security.InvalidParameterException.class), "assignable to java.lang.IllegalArgumentException");
        check(java.lang.RuntimeException.class.isAssignableFrom(java.security.InvalidParameterException.class), "assignable to java.lang.RuntimeException");
        check(java.lang.Throwable.class.isAssignableFrom(java.security.InvalidParameterException.class), "assignable to Throwable");

        // 真实抛出路径：KeyPairGenerator 非法 keysize（DSA 拒绝 -1 与 64）
        ct(java.security.InvalidParameterException.class, () -> java.security.KeyPairGenerator.getInstance("DSA").initialize(-1),
                "KPG initialize(-1) throws InvalidParameterException");
        ct(java.security.InvalidParameterException.class, () -> java.security.KeyPairGenerator.getInstance("DSA").initialize(64),
                "KPG initialize(64) throws InvalidParameterException");
        // 层级：RuntimeException 族而非 GeneralSecurityException
        check(!java.security.GeneralSecurityException.class.isAssignableFrom(java.security.InvalidParameterException.class),
                "InvalidParameterException is NOT a GeneralSecurityException");
        boolean poly = false;
        try { java.security.KeyPairGenerator.getInstance("DSA").initialize(-1); }
        catch (java.lang.IllegalArgumentException iae) { poly = true; }
        check(poly, "InvalidParameterException catchable as IllegalArgumentException");

        // 版本差异记录：JDK-8 仅有 ()/(String) 两构造器（JDK-9+ 增加 cause 构造器，见 JDK-27 侧断言）
        check(java.security.InvalidParameterException.class.getConstructors().length == 2, "JDK-8 has only 2 public constructors");
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
