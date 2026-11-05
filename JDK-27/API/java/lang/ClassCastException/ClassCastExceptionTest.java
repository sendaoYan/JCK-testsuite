/*
 * @test
 * @summary java.lang.ClassCastException conformance: white-box structure + black-box behavior.
 * @run main ClassCastExceptionTest
 */

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class ClassCastExceptionTest {
    static int passed;

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
        Class<?> c = Class.forName("java.lang.ClassCastException");
        check(!c.isInterface(), "not an interface");
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(c.getSuperclass() == RuntimeException.class, "superclass is RuntimeException");
        check(c.getDeclaredConstructors().length == 2, "2 public constructors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Class<?> c = Class.forName("java.lang.ClassCastException");

        ClassCastException e0 = new ClassCastException();
        check(e0.getMessage() == null, "no-arg ctor has null message");
        { ClassCastException e = new ClassCastException("boom"); check("boom".equals(e.getMessage()), "(String) ctor message"); }

        // common Throwable contract
        ClassCastException t = new ClassCastException();
        check(t.getCause() == null, "fresh cause null");
        check(t.initCause(new Exception("cause")) == t, "initCause returns this");
        check(t.getCause() != null && "cause".equals(t.getCause().getMessage()), "cause set");
        expect(java.lang.IllegalStateException.class, () -> t.initCause(new Exception("x")), "initCause twice");
        Throwable sup = new Exception("suppressed");
        t.addSuppressed(sup);
        check(t.getSuppressed().length == 1 && t.getSuppressed()[0] == sup, "addSuppressed/getSuppressed");
        check(t.getStackTrace().length >= 1, "getStackTrace non-empty");
        check(t.toString().startsWith("java.lang.ClassCastException"), "toString prefix");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        t.printStackTrace(new PrintStream(bos));
        check(bos.size() > 0, "printStackTrace(PrintStream) writes");
        check(t.getLocalizedMessage() == t.getMessage(), "getLocalizedMessage == getMessage");

        // trigger: invalid reference cast
        expect(ClassCastException.class, () -> { Object o = Integer.valueOf(1); String s = (String) o; }, "trigger invalid reference cast");
    }


    // ---- Deepening (lcO1, 2026-11): ClassCastException contract matrix ----
    static final boolean MODERN = !System.getProperty("java.version").startsWith("1.");

    static void testCtorMatrixDeep() {
        ClassCastException none = new ClassCastException();
        check(none.getMessage() == null && none.getCause() == null, "no-arg: null message and cause");
        check(none.toString().equals("java.lang.ClassCastException"), "no-arg toString bare");
        check(none.getLocalizedMessage() == null, "no-arg localized null");
        ClassCastException m = new ClassCastException("m");
        check("m".equals(m.getMessage()), "(String): message");
        check(m.getCause() == null, "(String): cause null");
        check(m.toString().equals("java.lang.ClassCastException: m"), "(String): toString exact");
        check(m.getLocalizedMessage().equals("m"), "(String): localized");
        check(new ClassCastException((String) null).getMessage() == null, "(String null): message null");
        check(new ClassCastException((String) null).toString().equals("java.lang.ClassCastException"), "(String null): bare toString");
        check("".equals(new ClassCastException("").getMessage()), "empty message kept");
        check(new ClassCastException("m").getSuppressed().length == 0, "fresh suppressed empty");
        check(new ClassCastException("m").getStackTrace().length > 0, "stack trace captured at construction");
        check(new ClassCastException() instanceof RuntimeException, "is a RuntimeException");
        check(new ClassCastException() instanceof Exception && new ClassCastException() instanceof Throwable, "is Exception/Throwable");
        check(new ClassCastException("m") instanceof java.io.Serializable, "Serializable");
        check(ClassCastException.class.getSuperclass() == RuntimeException.class, "direct superclass RuntimeException");
    }

    static void testCauseContractDeep() {
        ClassCastException t = new ClassCastException("m");
        check(t.getCause() == null, "cause starts null");
        ClassCastException c = new ClassCastException("c");
        check(t.initCause(c) == t, "initCause returns this");
        check(t.getCause() == c, "initCause stores identity");
        check(t.getMessage().equals("m"), "initCause leaves message alone");
        try {
            t.initCause(new ClassCastException("other"));
            throw new AssertionError("unreachable: second initCause");
        } catch (IllegalStateException e) {
            check(e.getMessage().startsWith("Can't overwrite cause with"), "second initCause ISE message prefix");
        }
        ClassCastException self = new ClassCastException();
        try {
            self.initCause(self);
            throw new AssertionError("unreachable: self initCause");
        } catch (IllegalArgumentException e) {
            check("Self-causation not permitted".equals(e.getMessage()), "self initCause IAE message exact");
        }
        ClassCastException nullOk = new ClassCastException();
        check(nullOk.initCause(null) == nullOk && nullOk.getCause() == null, "initCause(null) accepted");
        ClassCastException chained = new ClassCastException("outer");
        chained.initCause(new ClassCastException("inner"));
        check(chained.getCause().getMessage().equals("inner"), "chained cause readable");
        check(chained.toString().equals("java.lang.ClassCastException: outer"), "toString shows only own message");
    }

    static void testSuppressedContractDeep() {
        ClassCastException t = new ClassCastException("m");
        check(t.getSuppressed().length == 0, "suppressed empty by default");
        try {
            t.addSuppressed(null);
            throw new AssertionError("unreachable: addSuppressed(null)");
        } catch (NullPointerException e) {
            check("Cannot suppress a null exception.".equals(e.getMessage()), "addSuppressed(null) NPE message exact");
        }
        try {
            t.addSuppressed(t);
            throw new AssertionError("unreachable: addSuppressed(self)");
        } catch (IllegalArgumentException e) {
            check("Self-suppression not permitted".equals(e.getMessage()), "addSuppressed(self) IAE message exact");
        }
        ClassCastException s1 = new ClassCastException("s1");
        ClassCastException s2 = new ClassCastException("s2");
        t.addSuppressed(s1);
        t.addSuppressed(s2);
        check(t.getSuppressed().length == 2, "two suppressed kept");
        check(t.getSuppressed()[0] == s1 && t.getSuppressed()[1] == s2, "suppressed insertion order");
        t.getSuppressed()[0] = null;
        check(t.getSuppressed()[0] == s1, "suppressed defensive copy");
    }

    static void testStackTraceContractDeep() {
        ClassCastException t = new ClassCastException("m");
        StackTraceElement[] st = t.getStackTrace();
        check(st.length > 0, "stack trace captured");
        check(st[0].getClassName().equals("ClassCastExceptionTest"), "top frame class exact");
        check(st[0].getMethodName().equals("testStackTraceContractDeep"), "top frame method exact");
        StackTraceElement[] copy = t.getStackTrace();
        copy[0] = null;
        check(t.getStackTrace()[0] != null, "getStackTrace independent copy");
        t.setStackTrace(new StackTraceElement[] { new StackTraceElement("C", "m", "F.java", 1) });
        check(t.getStackTrace().length == 1 && t.getStackTrace()[0].getClassName().equals("C"), "setStackTrace roundtrip");
        t.setStackTrace(new StackTraceElement[0]);
        check(t.getStackTrace().length == 0, "setStackTrace(empty) clears");
        check(t.fillInStackTrace() == t, "fillInStackTrace returns this");
        check(t.getStackTrace().length > 0, "fillInStackTrace refills after clear");
    }

    static void testPrintStackTraceContractDeep() {
        ClassCastException t = new ClassCastException("boom");
        t.initCause(new ClassCastException("root"));
        t.addSuppressed(new ClassCastException("sup"));
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        t.printStackTrace(new java.io.PrintStream(bos));
        String out = new String(bos.toByteArray(), java.nio.charset.StandardCharsets.UTF_8);
        check(out.startsWith("java.lang.ClassCastException: boom"), "first line is toString");
        check(out.contains("\tat "), "frame lines present");
        check(out.contains("Suppressed: java.lang.ClassCastException: sup"), "Suppressed section present");
        check(out.contains("Caused by: java.lang.ClassCastException: root"), "Caused by section present");
        java.io.ByteArrayOutputStream bos2 = new java.io.ByteArrayOutputStream();
        java.io.PrintWriter pw = new java.io.PrintWriter(bos2);
        t.printStackTrace(pw);
        pw.flush();
        check(new String(bos2.toByteArray(), java.nio.charset.StandardCharsets.UTF_8).startsWith("java.lang.ClassCastException: boom"),
              "PrintWriter overload works");
    }

    static void testSuidAndSerializationDeep() {
        check(java.io.ObjectStreamClass.lookup(ClassCastException.class).getSerialVersionUID() == -9223365651070458532L,
              "serialVersionUID locked via ObjectStreamClass (same on JDK-8 and JDK-27)");
        try {
            ClassCastException t = new ClassCastException("ser");
            t.initCause(new ClassCastException("root"));
            t.addSuppressed(new ClassCastException("sup"));
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
            oos.writeObject(t);
            oos.close();
            ClassCastException copy = (ClassCastException) new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
            check("ser".equals(copy.getMessage()), "round-trip keeps message");
            check(copy.getCause() != null && "root".equals(copy.getCause().getMessage()), "round-trip keeps cause");
            check(copy.getSuppressed().length == 1 && "sup".equals(copy.getSuppressed()[0].getMessage()),
                  "round-trip keeps suppressed");
            check(copy.toString().equals("java.lang.ClassCastException: ser"), "round-trip toString exact");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static String castIntToString(Object o) {
        try { @SuppressWarnings("unused") String s = (String) o; return "OK"; }
        catch (ClassCastException e) { return "EX:" + e.getMessage(); }
    }
    static String castIntArrayToString(Object o) {
        try { @SuppressWarnings("unused") String s = (String) o; return "OK"; }
        catch (ClassCastException e) { return "EX:" + e.getMessage(); }
    }
    static String castThreadToComparable(Object o) {
        try { @SuppressWarnings("unchecked") Comparable<Object> c = (Comparable<Object>) o; return "OK" + c;
        } catch (ClassCastException e) { return "EX:" + e.getMessage(); }
    }

    static void testJvmTriggerMatrix() {
        String m1 = castIntToString(Integer.valueOf(1));
        if (MODERN) check(m1.startsWith("EX:class java.lang.Integer cannot be cast to class java.lang.String"),
              "27: CCE message uses 'class X cannot be cast to class Y' form");
        else check(m1.equals("EX:java.lang.Integer cannot be cast to java.lang.String"),
              "8: CCE message is 'X cannot be cast to Y' (exact)");
        String m2 = castIntArrayToString(new int[] { 1 });
        if (MODERN) check(m2.startsWith("EX:class [I cannot be cast to class java.lang.String"),
              "27: array CCE names the array type [I");
        else check(m2.equals("EX:[I cannot be cast to java.lang.String"),
              "8: array CCE names the array type [I (exact)");
        String m3 = castThreadToComparable(new Thread());
        if (MODERN) check(m3.startsWith("EX:class java.lang.Thread cannot be cast to class java.lang.Comparable"),
              "27: failed interface cast message form");
        else check(m3.equals("EX:java.lang.Thread cannot be cast to java.lang.Comparable"),
              "8: failed interface cast message (exact)");
        if (MODERN) check(m1.contains("module java.base of loader 'bootstrap'"),
              "27: CCE message names module and loader");
        else check(m1.indexOf("module") < 0, "8: CCE message has no module info");
    }

    static void testLegalCasts() {
        Object n = null;
        String ns = (String) n;
        check(ns == null, "null reference casts to any type without CCE");
        Object str = "x";
        String same = (String) str;
        check(same == str, "correct cast returns the same reference");
        Object up = "sub";
        CharSequence cs = (CharSequence) up;
        check(cs.equals("sub"), "upcast to interface succeeds");
        Object boxed = Integer.valueOf(7);
        Number num = (Number) boxed;
        check(num.intValue() == 7, "cast to implemented supertype succeeds");
        Object obj = (Object) "anything";
        check(obj.equals("anything"), "cast to Object always succeeds");
    }

    static void testThrownInstanceDetails() {
        try {
            Object o = Integer.valueOf(1);
            @SuppressWarnings("unused") String s = (String) o;
            throw new AssertionError("unreachable");
        } catch (ClassCastException e) {
            check(e.getCause() == null, "JVM-thrown CCE has null cause");
            check(e.getStackTrace().length > 0, "JVM-thrown CCE has stack trace");
            check(e.getSuppressed().length == 0, "JVM-thrown CCE has no suppressed");
            check(e instanceof RuntimeException, "JVM-thrown CCE is RuntimeException");
            check(ClassCastException.class.getSuperclass() == RuntimeException.class,
                  "direct superclass RuntimeException");
        }
    }

    static void testMoreCastTargets() {
        String d;
        try {
            Object o = Double.valueOf(1.5);
            String s = (String) o;
            d = "OK";
        } catch (ClassCastException e) {
            d = e.getMessage();
        }
        if (MODERN) check(d.startsWith("class java.lang.Double cannot be cast to class java.lang.String"),
              "27: Double->String message form");
        else check(d.equals("java.lang.Double cannot be cast to java.lang.String"), "8: Double->String exact");
        String al;
        try {
            Object o = new java.util.ArrayList<Object>();
            CharSequence c = (CharSequence) o;
            al = "OK";
        } catch (ClassCastException e) {
            al = e.getMessage();
        }
        if (MODERN) check(al.startsWith("class java.util.ArrayList cannot be cast to class java.lang.CharSequence"),
              "27: ArrayList->CharSequence message form");
        else check(al.equals("java.util.ArrayList cannot be cast to java.lang.CharSequence"), "8: ArrayList->CharSequence exact");
        String si;
        try {
            Object o = "s";
            Integer i = (Integer) o;
            si = "OK";
        } catch (ClassCastException e) {
            si = e.getMessage();
        }
        if (MODERN) check(si.startsWith("class java.lang.String cannot be cast to class java.lang.Integer"),
              "27: String->Integer message form");
        else check(si.equals("java.lang.String cannot be cast to java.lang.Integer"), "8: String->Integer exact");
    }
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testCtorMatrixDeep();
        testCauseContractDeep();
        testSuppressedContractDeep();
        testStackTraceContractDeep();
        testPrintStackTraceContractDeep();
        testSuidAndSerializationDeep();
        testJvmTriggerMatrix();
        testLegalCasts();
        testThrownInstanceDetails();
        testMoreCastTargets();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
