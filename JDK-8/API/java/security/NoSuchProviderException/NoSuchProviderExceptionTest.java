/*
 * @test
 * @summary java.security.NoSuchProviderException conformance: constructors, message, cause and superclass.
 * @run main NoSuchProviderExceptionTest
 */

public class NoSuchProviderExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.NoSuchProviderException e = new java.security.NoSuchProviderException("boom");
        check("boom".equals(e.getMessage()), "String ctor sets message");
        check(e instanceof java.security.GeneralSecurityException, "extends java.security.GeneralSecurityException");
        java.security.NoSuchProviderException e4 = new java.security.NoSuchProviderException();
        check(e4.getMessage() == null, "no-arg ctor message null");

        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ---- 契约深化：构造矩阵 / 异常链不变量 / 栈帧与抑制 / 序列化 / 层级 ----
    static void testContractDeep() throws Exception {
        // 构造矩阵与 getter 精确往返
        java.security.NoSuchProviderException e0 = new java.security.NoSuchProviderException();
        check(e0.getMessage() == null, "() ctor message null");
        check(e0.getCause() == null, "() ctor cause null");
        java.security.NoSuchProviderException e1 = new java.security.NoSuchProviderException("boom");
        check("boom".equals(e1.getMessage()), "(String) ctor message exact");
        check(e1.getCause() == null, "(String) ctor cause defaults null");
        check(java.security.NoSuchProviderException.class.getConstructors().length == 2, "public constructor count == 2");

        // 异常链不变量（Throwable 契约）
        java.lang.Exception rootC = new java.lang.Exception("r");
        check(new java.security.NoSuchProviderException("z").initCause(rootC).getCause() == rootC, "initCause sets cause");
        java.security.NoSuchProviderException once = new java.security.NoSuchProviderException("z");
        once.initCause(rootC);
        ct(java.lang.IllegalStateException.class, () -> once.initCause(new java.lang.Exception("c2")), "second initCause");
        java.security.NoSuchProviderException selfT = new java.security.NoSuchProviderException("z");
        ct(java.lang.IllegalArgumentException.class, () -> selfT.initCause(selfT), "initCause(this) self-reference");
        java.security.NoSuchProviderException fill = new java.security.NoSuchProviderException("z");
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
        java.security.NoSuchProviderException sup = new java.security.NoSuchProviderException("s");
        check(sup.getSuppressed().length == 0, "suppressed empty by default");
        sup.addSuppressed(new java.lang.Exception("s1"));
        check(sup.getSuppressed().length == 1
                && "s1".equals(sup.getSuppressed()[0].getMessage()), "addSuppressed/getSuppressed round-trip");
        ct(java.lang.NullPointerException.class, () -> sup.addSuppressed(null), "addSuppressed(null)");

        // toString / printStackTrace 形态
        check(new java.security.NoSuchProviderException().toString().equals("java.security.NoSuchProviderException"), "toString with null message");
        check(new java.security.NoSuchProviderException("q").toString().equals("java.security.NoSuchProviderException: q"), "toString with message");
        java.io.ByteArrayOutputStream pba = new java.io.ByteArrayOutputStream();
        new java.security.NoSuchProviderException("pt").printStackTrace(new java.io.PrintStream(pba));
        check(pba.toString().contains("java.security.NoSuchProviderException"), "printStackTrace output contains class name");
        check(!new java.security.NoSuchProviderException("x").equals(new java.security.NoSuchProviderException("x")), "identity equals: equal messages still not equal");

        // Serializable 契约：serialVersionUID 精确 + round-trip 保持 message/cause/suppressed
        check(java.io.ObjectStreamClass.lookup(java.security.NoSuchProviderException.class).getSerialVersionUID()
                == 8488111756688534474L, "serialVersionUID stable == 8488111756688534474");
        java.security.NoSuchProviderException ser = new java.security.NoSuchProviderException("ser-msg");
        ser.addSuppressed(new java.lang.Exception("sup-ser"));
        java.io.ByteArrayOutputStream ob = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(ob)) { oos.writeObject(ser); }
        java.security.NoSuchProviderException rt = (java.security.NoSuchProviderException) new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(ob.toByteArray())).readObject();
        check("ser-msg".equals(rt.getMessage()), "serialization preserves message");
        check(rt.getSuppressed().length == 1
                && "sup-ser".equals(rt.getSuppressed()[0].getMessage()),
                "serialization preserves suppressed exceptions");

        // 类型层级（继承链精确）
        check(java.security.GeneralSecurityException.class.isAssignableFrom(java.security.NoSuchProviderException.class), "assignable to java.security.GeneralSecurityException");
        check(java.lang.Exception.class.isAssignableFrom(java.security.NoSuchProviderException.class), "assignable to java.lang.Exception");
        check(java.lang.Throwable.class.isAssignableFrom(java.security.NoSuchProviderException.class), "assignable to Throwable");

        // 真实抛出路径：按名指定不存在 provider 的引擎工厂
        boolean np = false;
        try { java.security.MessageDigest.getInstance("MD5", "NoProv"); }
        catch (java.security.NoSuchProviderException e) { np = e.getMessage() != null && e.getMessage().contains("NoProv"); }
        check(np, "getInstance(alg, bogusProvider): throws with provider name in message");
        ct(java.security.NoSuchProviderException.class, () -> java.security.Signature.getInstance("SHA256withDSA", "NoProv"),
                "Signature.getInstance(alg, bogusProvider) throws NSPE");
        boolean poly = false;
        try { java.security.MessageDigest.getInstance("MD5", "NoProv"); }
        catch (java.security.GeneralSecurityException gse) { poly = gse instanceof java.security.NoSuchProviderException; }
        check(poly, "NSPE catchable polymorphically as GeneralSecurityException");
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
