/*
 * @test
 * @summary java.security.SignatureException conformance: constructors, message, cause and superclass.
 * @run main SignatureExceptionTest
 */

public class SignatureExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.SignatureException e = new java.security.SignatureException("boom");
        check("boom".equals(e.getMessage()), "String ctor sets message");
        check(e instanceof java.security.GeneralSecurityException, "extends java.security.GeneralSecurityException");
        Throwable cau = new Exception("cause");
        java.security.SignatureException e2 = new java.security.SignatureException("m", cau);
        check(e2.getCause() == cau, "(String,Throwable) ctor cause");
        Throwable cau2 = new Exception("cause2");
        java.security.SignatureException e3 = new java.security.SignatureException(cau2);
        check(e3.getCause() == cau2, "(Throwable) ctor cause");
        java.security.SignatureException e4 = new java.security.SignatureException();
        check(e4.getMessage() == null, "no-arg ctor message null");

        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ---- 契约深化：构造矩阵 / 异常链不变量 / 栈帧与抑制 / 序列化 / 层级 ----
    static void testContractDeep() throws Exception {
        // 构造矩阵与 getter 精确往返
        java.security.SignatureException e0 = new java.security.SignatureException();
        check(e0.getMessage() == null, "() ctor message null");
        check(e0.getCause() == null, "() ctor cause null");
        java.security.SignatureException e1 = new java.security.SignatureException("boom");
        check("boom".equals(e1.getMessage()), "(String) ctor message exact");
        check(e1.getCause() == null, "(String) ctor cause defaults null");
        java.lang.Exception root = new java.lang.Exception("root-cause");
        java.security.SignatureException e2 = new java.security.SignatureException("m", root);
        check("m".equals(e2.getMessage()), "(String,Throwable) ctor message exact");
        check(e2.getCause() == root, "(String,Throwable) ctor cause identity");
        java.security.SignatureException e3 = new java.security.SignatureException(root);
        check(e3.getCause() == root, "(Throwable) ctor cause identity");
        check(root.toString().equals(e3.getMessage()), "(Throwable) ctor message == cause.toString()");
        check(java.security.SignatureException.class.getConstructors().length == 4, "public constructor count == 4");

        // 异常链不变量（Throwable 契约）
        java.lang.Exception rootC = new java.lang.Exception("r");
        check(new java.security.SignatureException("z").initCause(rootC).getCause() == rootC, "initCause sets cause");
        java.security.SignatureException once = new java.security.SignatureException("z");
        once.initCause(rootC);
        ct(java.lang.IllegalStateException.class, () -> once.initCause(new java.lang.Exception("c2")), "second initCause");
        java.security.SignatureException selfT = new java.security.SignatureException("z");
        ct(java.lang.IllegalArgumentException.class, () -> selfT.initCause(selfT), "initCause(this) self-reference");
        java.security.SignatureException fill = new java.security.SignatureException("z");
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
        java.security.SignatureException sup = new java.security.SignatureException("s");
        check(sup.getSuppressed().length == 0, "suppressed empty by default");
        sup.addSuppressed(new java.lang.Exception("s1"));
        check(sup.getSuppressed().length == 1
                && "s1".equals(sup.getSuppressed()[0].getMessage()), "addSuppressed/getSuppressed round-trip");
        ct(java.lang.NullPointerException.class, () -> sup.addSuppressed(null), "addSuppressed(null)");

        // toString / printStackTrace 形态
        check(new java.security.SignatureException().toString().equals("java.security.SignatureException"), "toString with null message");
        check(new java.security.SignatureException("q").toString().equals("java.security.SignatureException: q"), "toString with message");
        java.io.ByteArrayOutputStream pba = new java.io.ByteArrayOutputStream();
        new java.security.SignatureException("pt").printStackTrace(new java.io.PrintStream(pba));
        check(pba.toString().contains("java.security.SignatureException"), "printStackTrace output contains class name");
        check(!new java.security.SignatureException("x").equals(new java.security.SignatureException("x")), "identity equals: equal messages still not equal");

        // Serializable 契约：serialVersionUID 精确 + round-trip 保持 message/cause/suppressed
        check(java.io.ObjectStreamClass.lookup(java.security.SignatureException.class).getSerialVersionUID()
                == 7509989324975124438L, "serialVersionUID stable == 7509989324975124438");
        java.security.SignatureException ser = new java.security.SignatureException("ser-msg", new java.lang.Exception("ser-cause"));
        ser.addSuppressed(new java.lang.Exception("sup-ser"));
        java.io.ByteArrayOutputStream ob = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(ob)) { oos.writeObject(ser); }
        java.security.SignatureException rt = (java.security.SignatureException) new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(ob.toByteArray())).readObject();
        check("ser-msg".equals(rt.getMessage()), "serialization preserves message");
        check(rt.getCause() != null && "ser-cause".equals(rt.getCause().getMessage()),
                "serialization preserves cause message");
        check(rt.getSuppressed().length == 1
                && "sup-ser".equals(rt.getSuppressed()[0].getMessage()),
                "serialization preserves suppressed exceptions");

        // 类型层级（继承链精确）
        check(java.security.GeneralSecurityException.class.isAssignableFrom(java.security.SignatureException.class), "assignable to java.security.GeneralSecurityException");
        check(java.lang.Exception.class.isAssignableFrom(java.security.SignatureException.class), "assignable to java.lang.Exception");
        check(java.lang.Throwable.class.isAssignableFrom(java.security.SignatureException.class), "assignable to Throwable");

        // 真实抛出路径 1：未初始化的 Signature 上 update/sign
        ct(java.security.SignatureException.class, () -> java.security.Signature.getInstance("SHA256withDSA").update((byte) 1),
                "update before init throws SignatureException");
        ct(java.security.SignatureException.class, () -> java.security.Signature.getInstance("SHA256withDSA").sign(),
                "sign before init throws SignatureException");
        // 真实抛出路径 2：initVerify 后 verify 长度非法的签名（RSA-512 期望 64 字节）
        boolean badLen = false;
        try {
            java.security.KeyPair rk = genRsa512();
            java.security.Signature v = java.security.Signature.getInstance("SHA256withRSA");
            v.initVerify(rk.getPublic());
            v.update("abc".getBytes("UTF-8"));
            v.verify(new byte[10]);
        } catch (java.security.SignatureException se) { badLen = se.getMessage() != null && se.getMessage().contains("expecting 64"); }
        check(badLen, "verify with 10-byte sig (expects 64) throws SignatureException mentioning length");
        boolean poly = false;
        try { java.security.Signature.getInstance("SHA256withDSA").sign(); }
        catch (java.security.GeneralSecurityException gse) { poly = true; }
        check(poly, "SignatureException catchable polymorphically as GeneralSecurityException");
    }
    static java.security.KeyPair genRsa512() throws Exception {
        java.security.KeyPairGenerator kpg = java.security.KeyPairGenerator.getInstance("RSA");
        kpg.initialize(512);
        return kpg.generateKeyPair();
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
