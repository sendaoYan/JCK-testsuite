/*
 * @test
 * @summary java.security.DigestException conformance: constructors, message, cause and superclass.
 * @run main DigestExceptionTest
 */

public class DigestExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.DigestException e = new java.security.DigestException("boom");
        check("boom".equals(e.getMessage()), "String ctor sets message");
        check(e instanceof java.security.GeneralSecurityException, "extends java.security.GeneralSecurityException");
        Throwable cau = new Exception("cause");
        java.security.DigestException e2 = new java.security.DigestException("m", cau);
        check(e2.getCause() == cau, "(String,Throwable) ctor cause");
        Throwable cau2 = new Exception("cause2");
        java.security.DigestException e3 = new java.security.DigestException(cau2);
        check(e3.getCause() == cau2, "(Throwable) ctor cause");
        java.security.DigestException e4 = new java.security.DigestException();
        check(e4.getMessage() == null, "no-arg ctor message null");

        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ---- 契约深化：构造矩阵 / 异常链不变量 / 栈帧与抑制 / 序列化 / 层级 ----
    static void testContractDeep() throws Exception {
        // 构造矩阵与 getter 精确往返
        java.security.DigestException e0 = new java.security.DigestException();
        check(e0.getMessage() == null, "() ctor message null");
        check(e0.getCause() == null, "() ctor cause null");
        java.security.DigestException e1 = new java.security.DigestException("boom");
        check("boom".equals(e1.getMessage()), "(String) ctor message exact");
        check(e1.getCause() == null, "(String) ctor cause defaults null");
        java.lang.Exception root = new java.lang.Exception("root-cause");
        java.security.DigestException e2 = new java.security.DigestException("m", root);
        check("m".equals(e2.getMessage()), "(String,Throwable) ctor message exact");
        check(e2.getCause() == root, "(String,Throwable) ctor cause identity");
        java.security.DigestException e3 = new java.security.DigestException(root);
        check(e3.getCause() == root, "(Throwable) ctor cause identity");
        check(root.toString().equals(e3.getMessage()), "(Throwable) ctor message == cause.toString()");
        check(java.security.DigestException.class.getConstructors().length == 4, "public constructor count == 4");

        // 异常链不变量（Throwable 契约）
        java.lang.Exception rootC = new java.lang.Exception("r");
        check(new java.security.DigestException("z").initCause(rootC).getCause() == rootC, "initCause sets cause");
        java.security.DigestException once = new java.security.DigestException("z");
        once.initCause(rootC);
        ct(java.lang.IllegalStateException.class, () -> once.initCause(new java.lang.Exception("c2")), "second initCause");
        java.security.DigestException selfT = new java.security.DigestException("z");
        ct(java.lang.IllegalArgumentException.class, () -> selfT.initCause(selfT), "initCause(this) self-reference");
        java.security.DigestException fill = new java.security.DigestException("z");
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
        java.security.DigestException sup = new java.security.DigestException("s");
        check(sup.getSuppressed().length == 0, "suppressed empty by default");
        sup.addSuppressed(new java.lang.Exception("s1"));
        check(sup.getSuppressed().length == 1
                && "s1".equals(sup.getSuppressed()[0].getMessage()), "addSuppressed/getSuppressed round-trip");
        ct(java.lang.NullPointerException.class, () -> sup.addSuppressed(null), "addSuppressed(null)");

        // toString / printStackTrace 形态
        check(new java.security.DigestException().toString().equals("java.security.DigestException"), "toString with null message");
        check(new java.security.DigestException("q").toString().equals("java.security.DigestException: q"), "toString with message");
        java.io.ByteArrayOutputStream pba = new java.io.ByteArrayOutputStream();
        new java.security.DigestException("pt").printStackTrace(new java.io.PrintStream(pba));
        check(pba.toString().contains("java.security.DigestException"), "printStackTrace output contains class name");
        check(!new java.security.DigestException("x").equals(new java.security.DigestException("x")), "identity equals: equal messages still not equal");

        // Serializable 契约：serialVersionUID 精确 + round-trip 保持 message/cause/suppressed
        check(java.io.ObjectStreamClass.lookup(java.security.DigestException.class).getSerialVersionUID()
                == 5821450303093652515L, "serialVersionUID stable == 5821450303093652515");
        java.security.DigestException ser = new java.security.DigestException("ser-msg", new java.lang.Exception("ser-cause"));
        ser.addSuppressed(new java.lang.Exception("sup-ser"));
        java.io.ByteArrayOutputStream ob = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(ob)) { oos.writeObject(ser); }
        java.security.DigestException rt = (java.security.DigestException) new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(ob.toByteArray())).readObject();
        check("ser-msg".equals(rt.getMessage()), "serialization preserves message");
        check(rt.getCause() != null && "ser-cause".equals(rt.getCause().getMessage()),
                "serialization preserves cause message");
        check(rt.getSuppressed().length == 1
                && "sup-ser".equals(rt.getSuppressed()[0].getMessage()),
                "serialization preserves suppressed exceptions");

        // 类型层级（继承链精确）
        check(java.security.GeneralSecurityException.class.isAssignableFrom(java.security.DigestException.class), "assignable to java.security.GeneralSecurityException");
        check(java.lang.Exception.class.isAssignableFrom(java.security.DigestException.class), "assignable to java.lang.Exception");
        check(java.lang.Throwable.class.isAssignableFrom(java.security.DigestException.class), "assignable to Throwable");

        // 真实抛出路径：MessageDigest.digest(buf,off,len) 输出缓冲小于摘要长度
        ct(java.security.DigestException.class, () -> java.security.MessageDigest.getInstance("MD5").digest(new byte[8], 0, 8),
                "MD5 digest into 8-byte buffer with len=8 (<16) throws DigestException");
        boolean shortOk = false;
        try { java.security.MessageDigest.getInstance("MD5").digest(new byte[8], 0, 8); }
        catch (java.security.DigestException de) { shortOk = de.getMessage() != null && de.getMessage().contains("16"); }
        check(shortOk, "DigestException message mentions required length 16");
        // 区分对照：offset+len 越界是 IllegalArgumentException 而非 DigestException
        ct(java.lang.IllegalArgumentException.class,
                () -> java.security.MessageDigest.getInstance("MD5").digest(new byte[64], 60, 8),
                "off+len beyond buffer length is IAE, not DigestException");
        // 多态 catch：作为 GeneralSecurityException 捕获
        boolean poly = false;
        try { java.security.MessageDigest.getInstance("MD5").digest(new byte[8], 0, 8); }
        catch (java.security.GeneralSecurityException gse) { poly = true; }
        check(poly, "DigestException catchable polymorphically as GeneralSecurityException");
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
