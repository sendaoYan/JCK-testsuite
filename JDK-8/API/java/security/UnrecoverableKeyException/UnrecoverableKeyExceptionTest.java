/*
 * @test
 * @summary java.security.UnrecoverableKeyException conformance: constructors, message, cause and superclass.
 * @run main UnrecoverableKeyExceptionTest
 */

public class UnrecoverableKeyExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.UnrecoverableKeyException e = new java.security.UnrecoverableKeyException("boom");
        check("boom".equals(e.getMessage()), "String ctor sets message");
        check(e instanceof java.security.UnrecoverableEntryException, "extends java.security.UnrecoverableEntryException");
        java.security.UnrecoverableKeyException e4 = new java.security.UnrecoverableKeyException();
        check(e4.getMessage() == null, "no-arg ctor message null");

        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ---- 契约深化：构造矩阵 / 异常链不变量 / 栈帧与抑制 / 序列化 / 层级 ----
    static void testContractDeep() throws Exception {
        // 构造矩阵与 getter 精确往返
        java.security.UnrecoverableKeyException e0 = new java.security.UnrecoverableKeyException();
        check(e0.getMessage() == null, "() ctor message null");
        check(e0.getCause() == null, "() ctor cause null");
        java.security.UnrecoverableKeyException e1 = new java.security.UnrecoverableKeyException("boom");
        check("boom".equals(e1.getMessage()), "(String) ctor message exact");
        check(e1.getCause() == null, "(String) ctor cause defaults null");
        check(java.security.UnrecoverableKeyException.class.getConstructors().length == 2, "public constructor count == 2");

        // 异常链不变量（Throwable 契约）
        java.lang.Exception rootC = new java.lang.Exception("r");
        check(new java.security.UnrecoverableKeyException("z").initCause(rootC).getCause() == rootC, "initCause sets cause");
        java.security.UnrecoverableKeyException once = new java.security.UnrecoverableKeyException("z");
        once.initCause(rootC);
        ct(java.lang.IllegalStateException.class, () -> once.initCause(new java.lang.Exception("c2")), "second initCause");
        java.security.UnrecoverableKeyException selfT = new java.security.UnrecoverableKeyException("z");
        ct(java.lang.IllegalArgumentException.class, () -> selfT.initCause(selfT), "initCause(this) self-reference");
        java.security.UnrecoverableKeyException fill = new java.security.UnrecoverableKeyException("z");
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
        java.security.UnrecoverableKeyException sup = new java.security.UnrecoverableKeyException("s");
        check(sup.getSuppressed().length == 0, "suppressed empty by default");
        sup.addSuppressed(new java.lang.Exception("s1"));
        check(sup.getSuppressed().length == 1
                && "s1".equals(sup.getSuppressed()[0].getMessage()), "addSuppressed/getSuppressed round-trip");
        ct(java.lang.NullPointerException.class, () -> sup.addSuppressed(null), "addSuppressed(null)");

        // toString / printStackTrace 形态
        check(new java.security.UnrecoverableKeyException().toString().equals("java.security.UnrecoverableKeyException"), "toString with null message");
        check(new java.security.UnrecoverableKeyException("q").toString().equals("java.security.UnrecoverableKeyException: q"), "toString with message");
        java.io.ByteArrayOutputStream pba = new java.io.ByteArrayOutputStream();
        new java.security.UnrecoverableKeyException("pt").printStackTrace(new java.io.PrintStream(pba));
        check(pba.toString().contains("java.security.UnrecoverableKeyException"), "printStackTrace output contains class name");
        check(!new java.security.UnrecoverableKeyException("x").equals(new java.security.UnrecoverableKeyException("x")), "identity equals: equal messages still not equal");

        // Serializable 契约：serialVersionUID 精确 + round-trip 保持 message/cause/suppressed
        check(java.io.ObjectStreamClass.lookup(java.security.UnrecoverableKeyException.class).getSerialVersionUID()
                == 7275063078190151277L, "serialVersionUID stable == 7275063078190151277");
        java.security.UnrecoverableKeyException ser = new java.security.UnrecoverableKeyException("ser-msg");
        ser.addSuppressed(new java.lang.Exception("sup-ser"));
        java.io.ByteArrayOutputStream ob = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(ob)) { oos.writeObject(ser); }
        java.security.UnrecoverableKeyException rt = (java.security.UnrecoverableKeyException) new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(ob.toByteArray())).readObject();
        check("ser-msg".equals(rt.getMessage()), "serialization preserves message");
        check(rt.getSuppressed().length == 1
                && "sup-ser".equals(rt.getSuppressed()[0].getMessage()),
                "serialization preserves suppressed exceptions");

        // 类型层级（继承链精确）
        check(java.security.UnrecoverableEntryException.class.isAssignableFrom(java.security.UnrecoverableKeyException.class), "assignable to java.security.UnrecoverableEntryException");
        check(java.security.GeneralSecurityException.class.isAssignableFrom(java.security.UnrecoverableKeyException.class), "assignable to java.security.GeneralSecurityException");
        check(java.lang.Exception.class.isAssignableFrom(java.security.UnrecoverableKeyException.class), "assignable to java.lang.Exception");
        check(java.lang.Throwable.class.isAssignableFrom(java.security.UnrecoverableKeyException.class), "assignable to Throwable");

        // 真实抛出路径：JCEKS 密钥库错误口令取密钥 / 取条目 / 缺口令保护
        boolean wrongGetKey = false;
        try {
            javax.crypto.SecretKey sk = javax.crypto.KeyGenerator.getInstance("AES").generateKey();
            java.security.KeyStore jceks = java.security.KeyStore.getInstance("JCEKS");
            jceks.load(null, null);
            jceks.setKeyEntry("s", sk, "right".toCharArray(), null);
            check(jceks.getKey("s", "right".toCharArray()) != null, "correct password recovers key");
            jceks.getKey("s", "wrong".toCharArray());
        } catch (java.security.UnrecoverableKeyException e) { wrongGetKey = true; }
        check(wrongGetKey, "getKey with wrong password throws UnrecoverableKeyException");
        boolean wrongEntry = false;
        try {
            javax.crypto.SecretKey sk = javax.crypto.KeyGenerator.getInstance("AES").generateKey();
            java.security.KeyStore jceks = java.security.KeyStore.getInstance("JCEKS");
            jceks.load(null, null);
            jceks.setEntry("e", new java.security.KeyStore.SecretKeyEntry(sk),
                    new java.security.KeyStore.PasswordProtection("right".toCharArray()));
            jceks.getEntry("e", new java.security.KeyStore.PasswordProtection("wrong".toCharArray()));
        } catch (java.security.UnrecoverableKeyException e) { wrongEntry = true; }
        check(wrongEntry, "getEntry with wrong password throws UnrecoverableKeyException");
        boolean nullProt = false;
        try {
            javax.crypto.SecretKey sk = javax.crypto.KeyGenerator.getInstance("AES").generateKey();
            java.security.KeyStore jceks = java.security.KeyStore.getInstance("JCEKS");
            jceks.load(null, null);
            jceks.setEntry("e", new java.security.KeyStore.SecretKeyEntry(sk),
                    new java.security.KeyStore.PasswordProtection("right".toCharArray()));
            jceks.getEntry("e", null);
        } catch (java.security.UnrecoverableKeyException e) { nullProt = e.getMessage() != null && e.getMessage().contains("password"); }
        check(nullProt, "getEntry with null protection throws UKE mentioning password");
        boolean asUEE = false;
        try {
            javax.crypto.SecretKey sk = javax.crypto.KeyGenerator.getInstance("AES").generateKey();
            java.security.KeyStore jceks = java.security.KeyStore.getInstance("JCEKS");
            jceks.load(null, null);
            jceks.setKeyEntry("s", sk, "right".toCharArray(), null);
            jceks.getKey("s", "wrong".toCharArray());
        } catch (java.security.UnrecoverableEntryException ue) { asUEE = true; }
        check(asUEE, "UKE catchable polymorphically as UnrecoverableEntryException");
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
