/*
 * @test
 * @summary java.security.InvalidParameterException conformance: white-box structure + black-box behavior.
 * @run main InvalidParameterExceptionTest
 */

import java.lang.reflect.*;
import java.security.InvalidParameterException;

public class InvalidParameterExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.InvalidParameterException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass().getName().equals("java.lang.IllegalArgumentException"), "superclass");
        check(c.getInterfaces().length == 0, "no interfaces");
        check(c.getDeclaredConstructors().length == 4, "constructor count");
        check(c.getDeclaredMethods().length == 0, "no declared methods");
    }
    static void testBlackBox() {
        InvalidParameterException e = new InvalidParameterException();
        check(e != null, "no-arg non-null");
        check(e.getMessage() == null, "no-arg getMessage null");
        check(e.getCause() == null, "no-arg getCause null");

        InvalidParameterException m = new InvalidParameterException("boom");
        check("boom".equals(m.getMessage()), "String ctor message");
        check(m.getCause() == null, "String ctor getCause null");

        Throwable t = new RuntimeException("cause");
        InvalidParameterException st = new InvalidParameterException("msg", t);
        check("msg".equals(st.getMessage()), "String+Throwable ctor message");
        check(st.getCause() == t, "String+Throwable ctor cause");

        InvalidParameterException tt = new InvalidParameterException(t);
        check(tt.getCause() == t, "Throwable ctor cause");
        check(tt.getMessage() != null, "Throwable ctor message non-null");

        check(e instanceof IllegalArgumentException, "instanceof IllegalArgumentException");
        check(e instanceof RuntimeException, "instanceof RuntimeException");
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
        java.security.InvalidParameterException e0 = new java.security.InvalidParameterException();
        check(e0.getMessage() == null, "() ctor message null");
        check(e0.getCause() == null, "() ctor cause null");
        java.security.InvalidParameterException e1 = new java.security.InvalidParameterException("boom");
        check("boom".equals(e1.getMessage()), "(String) ctor message exact");
        check(e1.getCause() == null, "(String) ctor cause defaults null");
        check(java.security.InvalidParameterException.class.getConstructors().length == 4, "public constructor count == 4");

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

        // 版本差异（JDK-9+）：JDK-8 仅有 ()/(String) 两构造器，本版含 (String,Throwable)/(Throwable)
        check(java.security.InvalidParameterException.class.getConstructors().length == 4, "JDK-27 has 4 public constructors (JDK-8: 2)");
        java.lang.Throwable root = new java.lang.Exception("ipe-cause");
        java.security.InvalidParameterException st = new java.security.InvalidParameterException("m", root);
        check("m".equals(st.getMessage()) && st.getCause() == root, "(String,Throwable) ctor (JDK-9+)");
        java.security.InvalidParameterException tt = new java.security.InvalidParameterException(root);
        check(tt.getCause() == root && root.toString().equals(tt.getMessage()), "(Throwable) ctor (JDK-9+)");
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
