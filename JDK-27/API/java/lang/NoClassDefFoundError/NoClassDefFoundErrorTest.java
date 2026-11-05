/*
 * @test
 * @summary java.lang.NoClassDefFoundError conformance: white-box structure + black-box behavior.
 * @run main NoClassDefFoundErrorTest
 */

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class NoClassDefFoundErrorTest {
    static int passed;

    // lcP2: side discriminator — true on modern JDKs (java.version no longer starts with "1.")
    static final boolean MODERN = !System.getProperty("java.version").startsWith("1.");

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expect(Class<? extends Throwable> type, Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected " + type.getSimpleName());
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + ": got " + t.getClass().getName());
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.NoClassDefFoundError");
        check(!c.isInterface(), "not an interface");
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(c.getSuperclass() == LinkageError.class, "superclass is LinkageError");
        check(c.getDeclaredConstructors().length == 2, "2 public constructors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Class<?> c = Class.forName("java.lang.NoClassDefFoundError");

        NoClassDefFoundError e0 = new NoClassDefFoundError();
        check(e0.getMessage() == null, "no-arg ctor has null message");
        { NoClassDefFoundError e = new NoClassDefFoundError("boom"); check("boom".equals(e.getMessage()), "(String) ctor message"); }

        // common Throwable contract
        NoClassDefFoundError t = new NoClassDefFoundError();
        check(t.getCause() == null, "fresh cause null");
        check(t.initCause(new Exception("cause")) == t, "initCause returns this");
        check(t.getCause() != null && "cause".equals(t.getCause().getMessage()), "cause set");
        expect(java.lang.IllegalStateException.class, () -> t.initCause(new Exception("x")), "initCause twice");
        Throwable sup = new Exception("suppressed");
        t.addSuppressed(sup);
        check(t.getSuppressed().length == 1 && t.getSuppressed()[0] == sup, "addSuppressed/getSuppressed");
        check(t.getStackTrace().length >= 1, "getStackTrace non-empty");
        check(t.toString().startsWith("java.lang.NoClassDefFoundError"), "toString prefix");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        t.printStackTrace(new PrintStream(bos));
        check(bos.size() > 0, "printStackTrace(PrintStream) writes");
        check(t.getLocalizedMessage() == t.getMessage(), "getLocalizedMessage == getMessage");

    }

    // ---- lcP2 契约深化：LinkageError 家族归属矩阵（14 类型一张表，双侧 javap 实证） ----
    static void testFamilyMatrixLcP2() {
        Throwable[] fam = {
            new LinkageError(), new ClassCircularityError(), new ClassFormatError(),
            new VerifyError(), new UnsupportedClassVersionError(),
            new IncompatibleClassChangeError(), new AbstractMethodError(),
            new IllegalAccessError(), new InstantiationError(), new NoSuchFieldError(),
            new NoSuchMethodError(), new NoClassDefFoundError(),
            new BootstrapMethodError(), new UnsatisfiedLinkError()
        };
        check(fam.length == 14, "family matrix covers all 14 family types");
        for (int i = 0; i < fam.length; i++) {
            Class<?> k = fam[i].getClass();
            check(fam[i] instanceof LinkageError, k.getSimpleName() + " instanceof LinkageError");
            check(fam[i] instanceof Error, k.getSimpleName() + " instanceof Error");
            check(fam[i] instanceof Throwable, k.getSimpleName() + " instanceof Throwable");
            check(fam[i] instanceof java.io.Serializable, k.getSimpleName() + " instanceof Serializable");
            check(!(fam[i] instanceof RuntimeException), k.getSimpleName() + " is not a RuntimeException");
            check(!(fam[i] instanceof Exception), k.getSimpleName() + " is not an Exception");
        }
        // 直接父类格：14 条边（JDK-8 与 JDK-27 javap 双侧一致）
        check(LinkageError.class.getSuperclass() == Error.class, "LinkageError direct superclass is Error");
        check(ClassCircularityError.class.getSuperclass() == LinkageError.class, "ClassCircularityError <: LinkageError");
        check(ClassFormatError.class.getSuperclass() == LinkageError.class, "ClassFormatError <: LinkageError");
        check(UnsupportedClassVersionError.class.getSuperclass() == ClassFormatError.class, "UnsupportedClassVersionError <: ClassFormatError");
        check(VerifyError.class.getSuperclass() == LinkageError.class, "VerifyError <: LinkageError");
        check(IncompatibleClassChangeError.class.getSuperclass() == LinkageError.class, "IncompatibleClassChangeError <: LinkageError");
        check(AbstractMethodError.class.getSuperclass() == IncompatibleClassChangeError.class, "AbstractMethodError <: IncompatibleClassChangeError");
        check(IllegalAccessError.class.getSuperclass() == IncompatibleClassChangeError.class, "IllegalAccessError <: IncompatibleClassChangeError");
        check(InstantiationError.class.getSuperclass() == IncompatibleClassChangeError.class, "InstantiationError <: IncompatibleClassChangeError");
        check(NoSuchFieldError.class.getSuperclass() == IncompatibleClassChangeError.class, "NoSuchFieldError <: IncompatibleClassChangeError");
        check(NoSuchMethodError.class.getSuperclass() == IncompatibleClassChangeError.class, "NoSuchMethodError <: IncompatibleClassChangeError");
        check(NoClassDefFoundError.class.getSuperclass() == LinkageError.class, "NoClassDefFoundError <: LinkageError");
        check(BootstrapMethodError.class.getSuperclass() == LinkageError.class, "BootstrapMethodError <: LinkageError");
        check(UnsatisfiedLinkError.class.getSuperclass() == LinkageError.class, "UnsatisfiedLinkError <: LinkageError");
        // 兄弟分支互斥负例（先上转 Error 再 instanceof，避免 javac 对不相关具体类型的转换检查）
        Error cfe = new ClassFormatError();
        Error vfe = new VerifyError();
        Error ncde = new NoClassDefFoundError();
        Error icce = new IncompatibleClassChangeError();
        Error le = new LinkageError();
        check(!(cfe instanceof VerifyError), "ClassFormatError is not a VerifyError");
        check(!(vfe instanceof ClassFormatError), "VerifyError is not a ClassFormatError");
        check(!(ncde instanceof IncompatibleClassChangeError), "NoClassDefFoundError is not an IncompatibleClassChangeError");
        check(!(icce instanceof NoClassDefFoundError), "IncompatibleClassChangeError is not a NoClassDefFoundError");
        check(!(le instanceof VirtualMachineError), "LinkageError is not a VirtualMachineError");
        check(LinkageError.class.isAssignableFrom(LinkageError.class), "LinkageError assignable to itself");
    }


    // ---- lcP2 契约深化：构造器全链 + 反射签名锁定 ----
    static void testCtorChainLcP2() {
        try {
            check(NoClassDefFoundError.class.getConstructor() != null, "() ctor retrievable via reflection");
            check(NoClassDefFoundError.class.getConstructor(String.class) != null, "(String) ctor retrievable via reflection");


        } catch (ReflectiveOperationException e) {
            throw new AssertionError("ctor reflection failed", e);
        }
        NoClassDefFoundError e0 = new NoClassDefFoundError();
        check(e0.getMessage() == null, "no-arg ctor: null message");
        check("java.lang.NoClassDefFoundError".equals(e0.toString()), "no-arg ctor: toString exact");
        check(e0.getCause() == null, "no-arg ctor: null cause");
        NoClassDefFoundError e1 = new NoClassDefFoundError("m");
        check("m".equals(e1.getMessage()), "(String) ctor: message exact");
        check("java.lang.NoClassDefFoundError: m".equals(e1.toString()), "(String) ctor: toString exact");
        check(NoClassDefFoundError.class.getDeclaredConstructors().length == 2, "declared ctor count == 2 (javap-verified: ()/(String))");



    }


    // ---- lcP2 契约深化：Throwable 通用契约（精确消息/抑制/栈轨迹/内存打印） ----
    static void testThrowableContractLcP2() {
        NoClassDefFoundError t = new NoClassDefFoundError("m");
        check("java.lang.NoClassDefFoundError: m".equals(t.toString()), "toString exact: java.lang.NoClassDefFoundError: m");
        check("m".equals(t.getMessage()), "getMessage exact");
        check(t.getLocalizedMessage() == t.getMessage(), "getLocalizedMessage defaults to same reference as getMessage");
        check(t.getCause() == null, "fresh instance: cause null");
        check(t.getSuppressed().length == 0, "fresh instance: suppressed empty");
        check(t.getStackTrace().length >= 1, "fresh instance: captured stack trace non-empty");
        check("NoClassDefFoundErrorTest".equals(t.getStackTrace()[0].getClassName()), "top frame class is this test class");
        check(t.fillInStackTrace() == t, "fillInStackTrace returns this");
        // initCause：一次成功、二次 ISE（消息含新 cause 的 toString）、自禁
        Exception c1 = new Exception("c1");
        check(t.initCause(c1) == t, "initCause returns this");
        check(t.getCause() == c1, "getCause is the same instance after initCause");
        try {
            t.initCause(new Exception("x"));
            check(false, "second initCause must throw IllegalStateException");
        } catch (IllegalStateException ise) {
            passed++;
            check("Can't overwrite cause with java.lang.Exception: x".equals(ise.getMessage()), "initCause ISE message exact");
        }
        NoClassDefFoundError t2 = new NoClassDefFoundError();
        try {
            t2.initCause(t2);
            check(false, "initCause(self) must throw IllegalArgumentException");
        } catch (IllegalArgumentException iae) {
            passed++;
            check("Self-causation not permitted".equals(iae.getMessage()), "self-causation IAE message exact");
        }
        // addSuppressed：自禁、保序、防御拷贝
        try {
            t2.addSuppressed(t2);
            check(false, "addSuppressed(self) must throw IllegalArgumentException");
        } catch (IllegalArgumentException iae) {
            passed++;
            check("Self-suppression not permitted".equals(iae.getMessage()), "self-suppression IAE message exact");
        }
        Exception s1 = new Exception("s1");
        Exception s2 = new Exception("s2");
        t2.addSuppressed(s1);
        t2.addSuppressed(s2);
        check(t2.getSuppressed().length == 2, "two suppressed exceptions preserved");
        check(t2.getSuppressed()[0] == s1 && t2.getSuppressed()[1] == s2, "suppressed order preserved");
        Throwable[] got = t2.getSuppressed();
        got[0] = null;
        check(t2.getSuppressed()[0] == s1, "getSuppressed returns a defensive copy");
        // setStackTrace：回读、清空、null NPE
        NoClassDefFoundError t3 = new NoClassDefFoundError("m");
        StackTraceElement f1 = new StackTraceElement("Foo", "bar", "Foo.java", 42);
        StackTraceElement f2 = new StackTraceElement("Baz", "qux", "Baz.java", -2);
        check(!f1.isNativeMethod() && f2.isNativeMethod(), "StackTraceElement.isNativeMethod: -2 native, 42 not");
        t3.setStackTrace(new StackTraceElement[] { f1, f2 });
        check(t3.getStackTrace().length == 2, "setStackTrace length honored");
        check(t3.getStackTrace()[0].equals(f1) && t3.getStackTrace()[1].equals(f2), "setStackTrace round-trip value-equal");
        check("Foo".equals(t3.getStackTrace()[0].getClassName()) && "bar".equals(t3.getStackTrace()[0].getMethodName())
              && "Foo.java".equals(t3.getStackTrace()[0].getFileName()) && t3.getStackTrace()[0].getLineNumber() == 42,
              "stack frame fields exact");
        t3.setStackTrace(new StackTraceElement[0]);
        check(t3.getStackTrace().length == 0, "setStackTrace(empty) clears the trace");
        try {
            t3.setStackTrace(null);
            check(false, "setStackTrace(null) must throw NPE");
        } catch (NullPointerException npe) {
            passed++;
            if (MODERN) {
                check(npe.getMessage() != null && npe.getMessage().contains("stackTrace"), "MODERN: helpful NPE names the stackTrace field");
            } else {
                check(npe.getMessage() == null, "JDK-8: NPE carries no message");
            }
        }
        // printStackTrace 到内存流：首行逐字、自定义帧、Suppressed/Caused by 标记
        String sep = System.lineSeparator();
        NoClassDefFoundError t4 = new NoClassDefFoundError("m");
        t4.setStackTrace(new StackTraceElement[] { f1, f2 });
        t4.initCause(new Exception("root"));
        t4.addSuppressed(new Exception("sup"));
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        t4.printStackTrace(new java.io.PrintStream(bos));
        String printed = bos.toString();
        check(("java.lang.NoClassDefFoundError: m" + sep).equals(printed.substring(0, "java.lang.NoClassDefFoundError: m".length() + sep.length())), "printStackTrace first line exact");
        check(printed.contains("at Foo.bar(Foo.java:42)"), "custom frame printed verbatim");
        check(printed.contains("at Baz.qux(Native Method)"), "native marker printed for line -2");
        check(printed.contains("Suppressed: java.lang.Exception: sup"), "Suppressed: marker line present");
        check(printed.contains("Caused by: java.lang.Exception: root"), "Caused by: marker line present");
        NoClassDefFoundError t5 = new NoClassDefFoundError();
        java.io.ByteArrayOutputStream bos2 = new java.io.ByteArrayOutputStream();
        t5.printStackTrace(new java.io.PrintStream(bos2));
        check(("java.lang.NoClassDefFoundError" + sep).equals(bos2.toString().substring(0, "java.lang.NoClassDefFoundError".length() + sep.length())), "no-message printStackTrace first line is bare class name");
    }


    // ---- lcP2 契约深化：serialVersionUID 精确锁定（双侧实测一致，直锁） ----
    static void testSvuidLcP2() {
        check(java.io.Serializable.class.isAssignableFrom(NoClassDefFoundError.class), "NoClassDefFoundError implements Serializable");
        java.io.ObjectStreamClass osc = java.io.ObjectStreamClass.lookup(NoClassDefFoundError.class);
        check(osc != null, "ObjectStreamClass.lookup(NoClassDefFoundError.class) non-null");
        check(osc.getSerialVersionUID() == 9095859863287012458L, "NoClassDefFoundError.serialVersionUID == 9095859863287012458L (probe-locked, JDK-8 == JDK-27)");
    }


    // ---- lcP2 契约深化：序列化往返全保持（message/cause/suppressed/stackTrace） ----
    static void testSerializationRoundTripLcP2() {
        try {
            NoClassDefFoundError orig = new NoClassDefFoundError("ser");
            orig.initCause(new Exception("root"));
            orig.addSuppressed(new Exception("s1"));
            orig.addSuppressed(new Exception("s2"));
            orig.setStackTrace(new StackTraceElement[] {
                new StackTraceElement("Foo", "bar", "Foo.java", 42),
                new StackTraceElement("Baz", "qux", "Baz.java", -2)
            });
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
            oos.writeObject(orig);
            oos.close();
            java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()));
            NoClassDefFoundError copy = (NoClassDefFoundError) ois.readObject();
            ois.close();
            check(copy != orig, "round-trip yields a distinct object");
            check("ser".equals(copy.getMessage()), "round-trip message preserved");
            check(copy.getCause() != null && "root".equals(copy.getCause().getMessage()), "round-trip cause preserved (by value)");
            check(copy.getSuppressed().length == 2, "round-trip suppressed count preserved");
            check("s1".equals(copy.getSuppressed()[0].getMessage()) && "s2".equals(copy.getSuppressed()[1].getMessage()),
                  "round-trip suppressed order preserved");
            check(copy.getStackTrace().length == 2, "round-trip stack trace length preserved");
            check("Foo".equals(copy.getStackTrace()[0].getClassName()) && "bar".equals(copy.getStackTrace()[0].getMethodName())
                  && copy.getStackTrace()[0].getLineNumber() == 42, "round-trip stack frame exact");
            check(copy.toString().equals(orig.toString()), "round-trip toString equal");
            check(copy.getClass() == NoClassDefFoundError.class, "round-trip class preserved");
        } catch (Exception e) {
            throw new AssertionError("serialization round-trip failed", e);
        }
    }


    // ---- lcP2 契约深化：真触发——<clinit> 抛出后一切后续访问 => NoClassDefFoundError ----
    static RuntimeException lcP2ClinitBoom;
    static boolean lcP2ClinitRan;

    static class BadInit {
        static int v;
        static {
            RuntimeException ex = new RuntimeException("lcP2boom");
            lcP2ClinitBoom = ex; // 存入外层测试类：BadInit 自身从此不可再访问
            lcP2ClinitRan = true;
            if (true) throw ex; // JLS 8.7: static initializer must be able to complete normally
        }
    }

    static class GoodSibling {
        static final int OK = 7;
    }

    static void testRealTriggerFailedInitLcP2() {
        check(!lcP2ClinitRan, "BadInit not initialized yet");
        // (1) 首次访问：静态块异常被包装为 ExceptionInInitializerError，cause 是抛出的同一实例
        try {
            new BadInit();
            check(false, "first access must throw ExceptionInInitializerError");
        } catch (java.lang.ExceptionInInitializerError eiie) {
            passed++;
            check(eiie.getCause() == lcP2ClinitBoom, "EIIE cause is the exact RuntimeException instance thrown from <clinit>");
            check("lcP2boom".equals(eiie.getCause().getMessage()), "EIIE cause message preserved");
            check(eiie instanceof LinkageError, "EIIE is itself a LinkageError");
            check(eiie.getMessage() == null, "JVM-created EIIE has null message (both sides)");
        }
        check(lcP2ClinitRan, "<clinit> ran exactly once");
        String expected = "Could not initialize class NoClassDefFoundErrorTest$BadInit";
        // (2) 二次访问（new）：NoClassDefFoundError，消息逐字
        try {
            new BadInit();
            check(false, "second access must throw NoClassDefFoundError");
        } catch (NoClassDefFoundError e) {
            passed++;
            check(expected.equals(e.getMessage()), "NCDFE message exact: " + e.getMessage());
            check(("java.lang.NoClassDefFoundError: " + expected).equals(e.toString()), "NCDFE toString exact");
            check(e instanceof LinkageError && e instanceof Error, "NCDFE instanceof LinkageError and Error");
            if (MODERN) {
                check(e.getCause() instanceof java.lang.ExceptionInInitializerError, "JDK-27: NCDFE carries EIIE as cause (JDK-8154604)");
            } else {
                check(e.getCause() == null, "JDK-8: NCDFE cause is null (pre JDK-8154604)");
            }
            String sep = System.lineSeparator();
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            e.printStackTrace(new java.io.PrintStream(bos));
            String printed = bos.toString();
            check(printed.startsWith("java.lang.NoClassDefFoundError: " + expected + sep), "printStackTrace first line exact");
            check(printed.contains("\tat "), "printStackTrace prints frames");
        }
        // (3) 读静态字段：同一消息
        try {
            int x = BadInit.v;
            check(false, "static field read must throw NCDFE, got v=" + x);
        } catch (NoClassDefFoundError e) {
            passed++;
            check(expected.equals(e.getMessage()), "field-read vector: same exact message");
        }
        // (4) Class.forName(initialize=true)
        try {
            Class.forName("NoClassDefFoundErrorTest$BadInit", true, NoClassDefFoundErrorTest.class.getClassLoader());
            check(false, "forName(init=true) must throw NCDFE");
        } catch (ClassNotFoundException cnfe) {
            check(false, "unexpected CNFE: " + cnfe);
        } catch (NoClassDefFoundError e) {
            passed++;
            check(expected.equals(e.getMessage()), "forName vector: same exact message");
        }
        // (5)(6) initialize=false 返回 Class 不抛；反射实例化再触发 => NCDFE 直接抛出（不包 ITE）
        try {
            Class<?> k = Class.forName("NoClassDefFoundErrorTest$BadInit", false, NoClassDefFoundErrorTest.class.getClassLoader());
            check(k != null && "NoClassDefFoundErrorTest$BadInit".equals(k.getName()), "forName(init=false) returns the Class without initializing");
            try {
                java.lang.reflect.Constructor<?> ctor = k.getDeclaredConstructor();
                try {
                    ctor.newInstance();
                    check(false, "reflective instantiation must throw NCDFE");
                } catch (java.lang.reflect.InvocationTargetException ite) {
                    check(false, "NCDFE must not be wrapped in InvocationTargetException, got " + ite.getCause());
                } catch (NoClassDefFoundError e) {
                    passed++;
                    check(expected.equals(e.getMessage()), "reflection vector: same exact message");
                }
            } catch (ReflectiveOperationException roe) {
                throw new AssertionError("reflective lookup failed", roe);
            }
        } catch (ClassNotFoundException cnfe) {
            check(false, "unexpected CNFE: " + cnfe);
        }
        // (7) 兄弟类不受影响
        check(GoodSibling.OK == 7, "sibling class initializes fine");
        // (8) 手工构造的 NCDFE 与 JVM 抛出的消息语义并存
        NoClassDefFoundError manual = new NoClassDefFoundError(expected);
        check(expected.equals(manual.getMessage()), "manually constructed NCDFE keeps message verbatim");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testFamilyMatrixLcP2();
        testCtorChainLcP2();
        testThrowableContractLcP2();
        testSvuidLcP2();
        testSerializationRoundTripLcP2();
        testRealTriggerFailedInitLcP2();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
