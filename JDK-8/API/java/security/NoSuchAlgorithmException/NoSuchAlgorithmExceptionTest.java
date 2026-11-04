/*
 * @test
 * @summary java.security.NoSuchAlgorithmException conformance: constructors, message, cause and superclass.
 * @run main NoSuchAlgorithmExceptionTest
 */

public class NoSuchAlgorithmExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.NoSuchAlgorithmException e = new java.security.NoSuchAlgorithmException("boom");
        check("boom".equals(e.getMessage()), "String ctor sets message");
        check(e instanceof java.security.GeneralSecurityException, "extends java.security.GeneralSecurityException");
        Throwable cau = new Exception("cause");
        java.security.NoSuchAlgorithmException e2 = new java.security.NoSuchAlgorithmException("m", cau);
        check(e2.getCause() == cau, "(String,Throwable) ctor cause");
        Throwable cau2 = new Exception("cause2");
        java.security.NoSuchAlgorithmException e3 = new java.security.NoSuchAlgorithmException(cau2);
        check(e3.getCause() == cau2, "(Throwable) ctor cause");
        java.security.NoSuchAlgorithmException e4 = new java.security.NoSuchAlgorithmException();
        check(e4.getMessage() == null, "no-arg ctor message null");

        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ---- 契约深化：构造矩阵 / 异常链不变量 / 栈帧与抑制 / 序列化 / 层级 ----
    static void testContractDeep() throws Exception {
        // 构造矩阵与 getter 精确往返
        java.security.NoSuchAlgorithmException e0 = new java.security.NoSuchAlgorithmException();
        check(e0.getMessage() == null, "() ctor message null");
        check(e0.getCause() == null, "() ctor cause null");
        java.security.NoSuchAlgorithmException e1 = new java.security.NoSuchAlgorithmException("boom");
        check("boom".equals(e1.getMessage()), "(String) ctor message exact");
        check(e1.getCause() == null, "(String) ctor cause defaults null");
        java.lang.Exception root = new java.lang.Exception("root-cause");
        java.security.NoSuchAlgorithmException e2 = new java.security.NoSuchAlgorithmException("m", root);
        check("m".equals(e2.getMessage()), "(String,Throwable) ctor message exact");
        check(e2.getCause() == root, "(String,Throwable) ctor cause identity");
        java.security.NoSuchAlgorithmException e3 = new java.security.NoSuchAlgorithmException(root);
        check(e3.getCause() == root, "(Throwable) ctor cause identity");
        check(root.toString().equals(e3.getMessage()), "(Throwable) ctor message == cause.toString()");
        check(java.security.NoSuchAlgorithmException.class.getConstructors().length == 4, "public constructor count == 4");

        // 异常链不变量（Throwable 契约）
        java.lang.Exception rootC = new java.lang.Exception("r");
        check(new java.security.NoSuchAlgorithmException("z").initCause(rootC).getCause() == rootC, "initCause sets cause");
        java.security.NoSuchAlgorithmException once = new java.security.NoSuchAlgorithmException("z");
        once.initCause(rootC);
        ct(java.lang.IllegalStateException.class, () -> once.initCause(new java.lang.Exception("c2")), "second initCause");
        java.security.NoSuchAlgorithmException selfT = new java.security.NoSuchAlgorithmException("z");
        ct(java.lang.IllegalArgumentException.class, () -> selfT.initCause(selfT), "initCause(this) self-reference");
        java.security.NoSuchAlgorithmException fill = new java.security.NoSuchAlgorithmException("z");
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
        java.security.NoSuchAlgorithmException sup = new java.security.NoSuchAlgorithmException("s");
        check(sup.getSuppressed().length == 0, "suppressed empty by default");
        sup.addSuppressed(new java.lang.Exception("s1"));
        check(sup.getSuppressed().length == 1
                && "s1".equals(sup.getSuppressed()[0].getMessage()), "addSuppressed/getSuppressed round-trip");
        ct(java.lang.NullPointerException.class, () -> sup.addSuppressed(null), "addSuppressed(null)");

        // toString / printStackTrace 形态
        check(new java.security.NoSuchAlgorithmException().toString().equals("java.security.NoSuchAlgorithmException"), "toString with null message");
        check(new java.security.NoSuchAlgorithmException("q").toString().equals("java.security.NoSuchAlgorithmException: q"), "toString with message");
        java.io.ByteArrayOutputStream pba = new java.io.ByteArrayOutputStream();
        new java.security.NoSuchAlgorithmException("pt").printStackTrace(new java.io.PrintStream(pba));
        check(pba.toString().contains("java.security.NoSuchAlgorithmException"), "printStackTrace output contains class name");
        check(!new java.security.NoSuchAlgorithmException("x").equals(new java.security.NoSuchAlgorithmException("x")), "identity equals: equal messages still not equal");

        // Serializable 契约：serialVersionUID 精确 + round-trip 保持 message/cause/suppressed
        check(java.io.ObjectStreamClass.lookup(java.security.NoSuchAlgorithmException.class).getSerialVersionUID()
                == -7443947487218346562L, "serialVersionUID stable == -7443947487218346562");
        java.security.NoSuchAlgorithmException ser = new java.security.NoSuchAlgorithmException("ser-msg", new java.lang.Exception("ser-cause"));
        ser.addSuppressed(new java.lang.Exception("sup-ser"));
        java.io.ByteArrayOutputStream ob = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(ob)) { oos.writeObject(ser); }
        java.security.NoSuchAlgorithmException rt = (java.security.NoSuchAlgorithmException) new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(ob.toByteArray())).readObject();
        check("ser-msg".equals(rt.getMessage()), "serialization preserves message");
        check(rt.getCause() != null && "ser-cause".equals(rt.getCause().getMessage()),
                "serialization preserves cause message");
        check(rt.getSuppressed().length == 1
                && "sup-ser".equals(rt.getSuppressed()[0].getMessage()),
                "serialization preserves suppressed exceptions");

        // 类型层级（继承链精确）
        check(java.security.GeneralSecurityException.class.isAssignableFrom(java.security.NoSuchAlgorithmException.class), "assignable to java.security.GeneralSecurityException");
        check(java.lang.Exception.class.isAssignableFrom(java.security.NoSuchAlgorithmException.class), "assignable to java.lang.Exception");
        check(java.lang.Throwable.class.isAssignableFrom(java.security.NoSuchAlgorithmException.class), "assignable to Throwable");

        // 真实抛出路径：全部引擎工厂的 bogus 算法负例，消息包含请求的算法名
        boolean md = false;
        try { java.security.MessageDigest.getInstance("BogusDigest"); }
        catch (java.security.NoSuchAlgorithmException e) { md = e.getMessage() != null && e.getMessage().contains("BogusDigest"); }
        check(md, "MessageDigest.getInstance(bogus): message contains algorithm name");
        boolean sig = false;
        try { java.security.Signature.getInstance("BogusSig"); }
        catch (java.security.NoSuchAlgorithmException e) { sig = e.getMessage() != null && e.getMessage().contains("BogusSig"); }
        check(sig, "Signature.getInstance(bogus): message contains algorithm name");
        boolean kpg = false;
        try { java.security.KeyPairGenerator.getInstance("BogusKPG"); }
        catch (java.security.NoSuchAlgorithmException e) { kpg = e.getMessage() != null && e.getMessage().contains("BogusKPG"); }
        check(kpg, "KeyPairGenerator.getInstance(bogus): message contains algorithm name");
        boolean kf = false;
        try { java.security.KeyFactory.getInstance("BogusKF"); }
        catch (java.security.NoSuchAlgorithmException e) { kf = true; }
        check(kf, "KeyFactory.getInstance(bogus) throws NSAE");
        boolean sr = false;
        try { java.security.SecureRandom.getInstance("BogusPRNG"); }
        catch (java.security.NoSuchAlgorithmException e) { sr = true; }
        check(sr, "SecureRandom.getInstance(bogus) throws NSAE");
        // 多态 catch：作为 GeneralSecurityException
        boolean poly = false;
        try { java.security.MessageDigest.getInstance("BogusDigest"); }
        catch (java.security.GeneralSecurityException gse) { poly = gse instanceof java.security.NoSuchAlgorithmException; }
        check(poly, "NSAE catchable polymorphically as GeneralSecurityException");
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
