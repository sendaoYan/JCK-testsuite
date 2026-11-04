/*
 * @test
 * @summary java.security.GeneralSecurityException conformance: constructors, message, cause and superclass.
 * @run main GeneralSecurityExceptionTest
 */

public class GeneralSecurityExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.GeneralSecurityException e = new java.security.GeneralSecurityException("boom");
        check("boom".equals(e.getMessage()), "String ctor sets message");
        check(e instanceof java.lang.Exception, "extends java.lang.Exception");
        Throwable cau = new Exception("cause");
        java.security.GeneralSecurityException e2 = new java.security.GeneralSecurityException("m", cau);
        check(e2.getCause() == cau, "(String,Throwable) ctor cause");
        Throwable cau2 = new Exception("cause2");
        java.security.GeneralSecurityException e3 = new java.security.GeneralSecurityException(cau2);
        check(e3.getCause() == cau2, "(Throwable) ctor cause");
        java.security.GeneralSecurityException e4 = new java.security.GeneralSecurityException();
        check(e4.getMessage() == null, "no-arg ctor message null");

        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ---- 契约深化：构造矩阵 / 异常链不变量 / 栈帧与抑制 / 序列化 / 层级 ----
    static void testContractDeep() throws Exception {
        // 构造矩阵与 getter 精确往返
        java.security.GeneralSecurityException e0 = new java.security.GeneralSecurityException();
        check(e0.getMessage() == null, "() ctor message null");
        check(e0.getCause() == null, "() ctor cause null");
        java.security.GeneralSecurityException e1 = new java.security.GeneralSecurityException("boom");
        check("boom".equals(e1.getMessage()), "(String) ctor message exact");
        check(e1.getCause() == null, "(String) ctor cause defaults null");
        java.lang.Exception root = new java.lang.Exception("root-cause");
        java.security.GeneralSecurityException e2 = new java.security.GeneralSecurityException("m", root);
        check("m".equals(e2.getMessage()), "(String,Throwable) ctor message exact");
        check(e2.getCause() == root, "(String,Throwable) ctor cause identity");
        java.security.GeneralSecurityException e3 = new java.security.GeneralSecurityException(root);
        check(e3.getCause() == root, "(Throwable) ctor cause identity");
        check(root.toString().equals(e3.getMessage()), "(Throwable) ctor message == cause.toString()");
        check(java.security.GeneralSecurityException.class.getConstructors().length == 4, "public constructor count == 4");

        // 异常链不变量（Throwable 契约）
        java.lang.Exception rootC = new java.lang.Exception("r");
        check(new java.security.GeneralSecurityException("z").initCause(rootC).getCause() == rootC, "initCause sets cause");
        java.security.GeneralSecurityException once = new java.security.GeneralSecurityException("z");
        once.initCause(rootC);
        ct(java.lang.IllegalStateException.class, () -> once.initCause(new java.lang.Exception("c2")), "second initCause");
        java.security.GeneralSecurityException selfT = new java.security.GeneralSecurityException("z");
        ct(java.lang.IllegalArgumentException.class, () -> selfT.initCause(selfT), "initCause(this) self-reference");
        java.security.GeneralSecurityException fill = new java.security.GeneralSecurityException("z");
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
        java.security.GeneralSecurityException sup = new java.security.GeneralSecurityException("s");
        check(sup.getSuppressed().length == 0, "suppressed empty by default");
        sup.addSuppressed(new java.lang.Exception("s1"));
        check(sup.getSuppressed().length == 1
                && "s1".equals(sup.getSuppressed()[0].getMessage()), "addSuppressed/getSuppressed round-trip");
        ct(java.lang.NullPointerException.class, () -> sup.addSuppressed(null), "addSuppressed(null)");

        // toString / printStackTrace 形态
        check(new java.security.GeneralSecurityException().toString().equals("java.security.GeneralSecurityException"), "toString with null message");
        check(new java.security.GeneralSecurityException("q").toString().equals("java.security.GeneralSecurityException: q"), "toString with message");
        java.io.ByteArrayOutputStream pba = new java.io.ByteArrayOutputStream();
        new java.security.GeneralSecurityException("pt").printStackTrace(new java.io.PrintStream(pba));
        check(pba.toString().contains("java.security.GeneralSecurityException"), "printStackTrace output contains class name");
        check(!new java.security.GeneralSecurityException("x").equals(new java.security.GeneralSecurityException("x")), "identity equals: equal messages still not equal");

        // Serializable 契约：serialVersionUID 精确 + round-trip 保持 message/cause/suppressed
        check(java.io.ObjectStreamClass.lookup(java.security.GeneralSecurityException.class).getSerialVersionUID()
                == 894798122053539237L, "serialVersionUID stable == 894798122053539237");
        java.security.GeneralSecurityException ser = new java.security.GeneralSecurityException("ser-msg", new java.lang.Exception("ser-cause"));
        ser.addSuppressed(new java.lang.Exception("sup-ser"));
        java.io.ByteArrayOutputStream ob = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(ob)) { oos.writeObject(ser); }
        java.security.GeneralSecurityException rt = (java.security.GeneralSecurityException) new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(ob.toByteArray())).readObject();
        check("ser-msg".equals(rt.getMessage()), "serialization preserves message");
        check(rt.getCause() != null && "ser-cause".equals(rt.getCause().getMessage()),
                "serialization preserves cause message");
        check(rt.getSuppressed().length == 1
                && "sup-ser".equals(rt.getSuppressed()[0].getMessage()),
                "serialization preserves suppressed exceptions");

        // 类型层级（继承链精确）
        check(java.lang.Exception.class.isAssignableFrom(java.security.GeneralSecurityException.class), "assignable to java.lang.Exception");
        check(java.lang.Throwable.class.isAssignableFrom(java.security.GeneralSecurityException.class), "assignable to Throwable");

        // 家族成员关系：security 受检异常族全部派生自本类；三个 outsider 不派生
        check(java.security.GeneralSecurityException.class.isAssignableFrom(java.security.DigestException.class),
                "family: DigestException is a GeneralSecurityException");
        check(java.security.GeneralSecurityException.class.isAssignableFrom(java.security.InvalidAlgorithmParameterException.class),
                "family: InvalidAlgorithmParameterException is a GeneralSecurityException");
        check(java.security.GeneralSecurityException.class.isAssignableFrom(java.security.InvalidKeyException.class),
                "family: InvalidKeyException is a GeneralSecurityException");
        check(java.security.GeneralSecurityException.class.isAssignableFrom(java.security.KeyException.class),
                "family: KeyException is a GeneralSecurityException");
        check(java.security.GeneralSecurityException.class.isAssignableFrom(java.security.KeyManagementException.class),
                "family: KeyManagementException is a GeneralSecurityException");
        check(java.security.GeneralSecurityException.class.isAssignableFrom(java.security.KeyStoreException.class),
                "family: KeyStoreException is a GeneralSecurityException");
        check(java.security.GeneralSecurityException.class.isAssignableFrom(java.security.NoSuchAlgorithmException.class),
                "family: NoSuchAlgorithmException is a GeneralSecurityException");
        check(java.security.GeneralSecurityException.class.isAssignableFrom(java.security.NoSuchProviderException.class),
                "family: NoSuchProviderException is a GeneralSecurityException");
        check(java.security.GeneralSecurityException.class.isAssignableFrom(java.security.SignatureException.class),
                "family: SignatureException is a GeneralSecurityException");
        check(java.security.GeneralSecurityException.class.isAssignableFrom(java.security.UnrecoverableEntryException.class),
                "family: UnrecoverableEntryException is a GeneralSecurityException");
        check(java.security.GeneralSecurityException.class.isAssignableFrom(java.security.UnrecoverableKeyException.class),
                "family: UnrecoverableKeyException is a GeneralSecurityException");
        check(!java.security.GeneralSecurityException.class.isAssignableFrom(java.security.InvalidParameterException.class),
                "outsider: InvalidParameterException NOT a GeneralSecurityException");
        check(!java.security.GeneralSecurityException.class.isAssignableFrom(java.security.ProviderException.class),
                "outsider: ProviderException NOT a GeneralSecurityException");
        check(!java.security.GeneralSecurityException.class.isAssignableFrom(java.security.PrivilegedActionException.class),
                "outsider: PrivilegedActionException NOT a GeneralSecurityException");
        // 真实抛出路径：NoSuchAlgorithmException 可被本类多态捕获（工厂负例）
        boolean poly = false;
        try { java.security.MessageDigest.getInstance("BogusDigest"); }
        catch (java.security.GeneralSecurityException gse) { poly = gse instanceof java.security.NoSuchAlgorithmException; }
        check(poly, "MessageDigest.getInstance(bogus) caught as GeneralSecurityException(NoSuchAlgorithmException)");
        // 直接抛出 + (String,Throwable) 全参
        boolean direct = false;
        try { throw new java.security.GeneralSecurityException("direct", new java.lang.Exception("c")); }
        catch (java.security.GeneralSecurityException g) { direct = "direct".equals(g.getMessage()) && g.getCause() instanceof java.lang.Exception; }
        check(direct, "throwable directly and catchable with message+cause intact");
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
