/*
 * @test
 * @summary java.security.ProviderException conformance: constructors, message, cause and superclass.
 * @run main ProviderExceptionTest
 */

public class ProviderExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.ProviderException e = new java.security.ProviderException("boom");
        check("boom".equals(e.getMessage()), "String ctor sets message");
        check(e instanceof java.lang.RuntimeException, "extends java.lang.RuntimeException");
        Throwable cau = new Exception("cause");
        java.security.ProviderException e2 = new java.security.ProviderException("m", cau);
        check(e2.getCause() == cau, "(String,Throwable) ctor cause");
        Throwable cau2 = new Exception("cause2");
        java.security.ProviderException e3 = new java.security.ProviderException(cau2);
        check(e3.getCause() == cau2, "(Throwable) ctor cause");
        java.security.ProviderException e4 = new java.security.ProviderException();
        check(e4.getMessage() == null, "no-arg ctor message null");

        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ---- 契约深化：构造矩阵 / 异常链不变量 / 栈帧与抑制 / 序列化 / 层级 ----
    static void testContractDeep() throws Exception {
        // 构造矩阵与 getter 精确往返
        java.security.ProviderException e0 = new java.security.ProviderException();
        check(e0.getMessage() == null, "() ctor message null");
        check(e0.getCause() == null, "() ctor cause null");
        java.security.ProviderException e1 = new java.security.ProviderException("boom");
        check("boom".equals(e1.getMessage()), "(String) ctor message exact");
        check(e1.getCause() == null, "(String) ctor cause defaults null");
        java.lang.Exception root = new java.lang.Exception("root-cause");
        java.security.ProviderException e2 = new java.security.ProviderException("m", root);
        check("m".equals(e2.getMessage()), "(String,Throwable) ctor message exact");
        check(e2.getCause() == root, "(String,Throwable) ctor cause identity");
        java.security.ProviderException e3 = new java.security.ProviderException(root);
        check(e3.getCause() == root, "(Throwable) ctor cause identity");
        check(root.toString().equals(e3.getMessage()), "(Throwable) ctor message == cause.toString()");
        check(java.security.ProviderException.class.getConstructors().length == 4, "public constructor count == 4");

        // 异常链不变量（Throwable 契约）
        java.lang.Exception rootC = new java.lang.Exception("r");
        check(new java.security.ProviderException("z").initCause(rootC).getCause() == rootC, "initCause sets cause");
        java.security.ProviderException once = new java.security.ProviderException("z");
        once.initCause(rootC);
        ct(java.lang.IllegalStateException.class, () -> once.initCause(new java.lang.Exception("c2")), "second initCause");
        java.security.ProviderException selfT = new java.security.ProviderException("z");
        ct(java.lang.IllegalArgumentException.class, () -> selfT.initCause(selfT), "initCause(this) self-reference");
        java.security.ProviderException fill = new java.security.ProviderException("z");
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
        java.security.ProviderException sup = new java.security.ProviderException("s");
        check(sup.getSuppressed().length == 0, "suppressed empty by default");
        sup.addSuppressed(new java.lang.Exception("s1"));
        check(sup.getSuppressed().length == 1
                && "s1".equals(sup.getSuppressed()[0].getMessage()), "addSuppressed/getSuppressed round-trip");
        ct(java.lang.NullPointerException.class, () -> sup.addSuppressed(null), "addSuppressed(null)");

        // toString / printStackTrace 形态
        check(new java.security.ProviderException().toString().equals("java.security.ProviderException"), "toString with null message");
        check(new java.security.ProviderException("q").toString().equals("java.security.ProviderException: q"), "toString with message");
        java.io.ByteArrayOutputStream pba = new java.io.ByteArrayOutputStream();
        new java.security.ProviderException("pt").printStackTrace(new java.io.PrintStream(pba));
        check(pba.toString().contains("java.security.ProviderException"), "printStackTrace output contains class name");
        check(!new java.security.ProviderException("x").equals(new java.security.ProviderException("x")), "identity equals: equal messages still not equal");

        // Serializable 契约：serialVersionUID 精确 + round-trip 保持 message/cause/suppressed
        check(java.io.ObjectStreamClass.lookup(java.security.ProviderException.class).getSerialVersionUID()
                == 5256023526693665674L, "serialVersionUID stable == 5256023526693665674");
        java.security.ProviderException ser = new java.security.ProviderException("ser-msg", new java.lang.Exception("ser-cause"));
        ser.addSuppressed(new java.lang.Exception("sup-ser"));
        java.io.ByteArrayOutputStream ob = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(ob)) { oos.writeObject(ser); }
        java.security.ProviderException rt = (java.security.ProviderException) new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(ob.toByteArray())).readObject();
        check("ser-msg".equals(rt.getMessage()), "serialization preserves message");
        check(rt.getCause() != null && "ser-cause".equals(rt.getCause().getMessage()),
                "serialization preserves cause message");
        check(rt.getSuppressed().length == 1
                && "sup-ser".equals(rt.getSuppressed()[0].getMessage()),
                "serialization preserves suppressed exceptions");

        // 类型层级（继承链精确）
        check(java.lang.RuntimeException.class.isAssignableFrom(java.security.ProviderException.class), "assignable to java.lang.RuntimeException");
        check(java.lang.Throwable.class.isAssignableFrom(java.security.ProviderException.class), "assignable to Throwable");

        // ProviderException 属 RuntimeException（非受检、不在 GSE 族）：运行时配置错误信号
        check(java.lang.RuntimeException.class.isAssignableFrom(java.security.ProviderException.class),
                "ProviderException is a RuntimeException");
        check(!java.security.GeneralSecurityException.class.isAssignableFrom(java.security.ProviderException.class),
                "ProviderException is NOT a GeneralSecurityException");
        boolean poly = false;
        try { throw new java.security.ProviderException("pe", new java.lang.Exception("c")); }
        catch (java.lang.RuntimeException re) { poly = true; }
        check(poly, "ProviderException catchable as RuntimeException (unchecked)");
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
