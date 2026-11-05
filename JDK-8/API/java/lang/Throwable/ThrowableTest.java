/*
 * @test
 * @summary java.lang.Throwable API conformance: constructors, getMessage/getCause/initCause,
 *          addSuppressed/getSuppressed, getStackTrace/setStackTrace/fillInStackTrace,
 *          toString/getLocalizedMessage/printStackTrace, and argument validation.
 * @run main ThrowableTest
 */

import java.lang.reflect.Modifier;

public class ThrowableTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Custom extends Throwable {
        Custom(String m) { super(m); }
    }

    // Exposes the protected (String, Throwable, boolean, boolean) constructor.
    static class SuppressionThrowable extends Throwable {
        SuppressionThrowable(String m, Throwable c, boolean enableSuppression, boolean writableStackTrace) {
            super(m, c, enableSuppression, writableStackTrace);
        }
    }

    static void testConstructors() {
        Throwable t1 = new Throwable();
        check(t1.getMessage() == null && t1.getCause() == null, "no-arg ctor");
        Throwable t2 = new Throwable("msg");
        check("msg".equals(t2.getMessage()) && t2.getCause() == null, "(String) ctor");
        Exception cause = new Exception("cause");
        Throwable t3 = new Throwable("msg", cause);
        check("msg".equals(t3.getMessage()) && t3.getCause() == cause, "(String,Throwable) ctor");
        Throwable t4 = new Throwable(cause);
        check(t4.getCause() == cause, "(Throwable) ctor sets cause");
        check(cause.toString().equals(t4.getMessage()), "(Throwable) ctor message == cause.toString()");
    }

    static void testInitCause() {
        Throwable t = new Throwable();
        check(t.getCause() == null, "cause null initially");
        Exception c = new Exception("cause");
        check(t.initCause(c) == t, "initCause returns this");
        check(t.getCause() == c, "getCause returns set cause");
        try {
            t.initCause(new Exception("other"));
            throw new AssertionError("initCause twice: expected IllegalStateException");
        } catch (IllegalStateException expected) {
            passed++;
        }

        Throwable fresh = new Throwable();
        try {
            fresh.initCause(fresh);
            throw new AssertionError("initCause(self): expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            passed++;
        }

        Throwable withCause = new Throwable(c);
        try {
            withCause.initCause(new Exception("x"));
            throw new AssertionError("initCause on already-initialized: expected IllegalStateException");
        } catch (IllegalStateException expected) {
            passed++;
        }
    }

    static void testSuppressed() {
        Throwable t = new Throwable("primary");
        check(t.getSuppressed().length == 0, "no suppressed initially");
        try {
            t.addSuppressed(null);
            throw new AssertionError("addSuppressed(null): expected NullPointerException");
        } catch (NullPointerException expected) {
            passed++;
        }
        try {
            t.addSuppressed(t);
            throw new AssertionError("addSuppressed(self): expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            passed++;
        }
        Exception s = new Exception("suppressed");
        t.addSuppressed(s);
        check(t.getSuppressed().length == 1 && t.getSuppressed()[0] == s, "addSuppressed adds");
        t.getSuppressed()[0] = null;
        check(t.getSuppressed()[0] == s, "getSuppressed returns a clone");
    }

    static void testStackTrace() {
        Throwable t = new Throwable();
        try {
            t.setStackTrace(null);
            throw new AssertionError("setStackTrace(null): expected NullPointerException");
        } catch (NullPointerException expected) {
            passed++;
        }
        try {
            t.setStackTrace(new StackTraceElement[] { null });
            throw new AssertionError("setStackTrace with null element: expected NullPointerException");
        } catch (NullPointerException expected) {
            passed++;
        }

        StackTraceElement[] st = new StackTraceElement[] {
            new StackTraceElement("C", "m", "F.java", 1)
        };
        t.setStackTrace(st);
        check(t.getStackTrace().length == 1 && t.getStackTrace()[0].equals(st[0]),
              "setStackTrace/getStackTrace roundtrip");
        st[0] = new StackTraceElement("X", "y", "Z.java", 2);
        check(t.getStackTrace()[0].equals(new StackTraceElement("C", "m", "F.java", 1)),
              "setStackTrace clones the array");

        Throwable t2 = new Throwable();
        check(t2.fillInStackTrace() == t2, "fillInStackTrace returns this");
        check(t2.getStackTrace().length > 0, "fillInStackTrace records a frame");
    }

    static void testToStringAndMessage() {
        check(new Throwable("msg").getMessage().equals("msg"), "getMessage");
        check(new Throwable("msg").getLocalizedMessage().equals("msg"),
              "getLocalizedMessage defaults to getMessage");
        check(new Throwable().toString().equals("java.lang.Throwable"), "toString no message");
        check(new Throwable("boom").toString().equals("java.lang.Throwable: boom"),
              "toString with message");
        check(new Custom("m").toString().equals("ThrowableTest$Custom: m"),
              "toString subclass with message");
    }

    static void testPrintStackTrace() {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        Throwable t = new Throwable("x");
        t.printStackTrace(new java.io.PrintStream(bos));
        check(bos.toString().contains("Throwable: x"), "printStackTrace writes to stream");
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.Throwable");
        check(!c.isInterface(), "Throwable is a class");
        check(c.getSuperclass() == Object.class, "Throwable extends Object");
        check(!Modifier.isFinal(c.getModifiers()), "Throwable is subclassable");
        check(java.io.Serializable.class.isAssignableFrom(c), "Throwable is Serializable");
        check(c.getConstructor() != null && c.getConstructor(String.class) != null
              && c.getConstructor(String.class, Throwable.class) != null
              && c.getConstructor(Throwable.class) != null, "four public constructors present");
        // Serialization stability marker.
        java.lang.reflect.Field f = c.getDeclaredField("serialVersionUID");
        f.setAccessible(true);
        check(((Long) f.get(null)).longValue() == -3042686055658047285L,
              "serialVersionUID stable across JDK-8");
    }

    // ---- Part 2b: protected (String, Throwable, boolean, boolean) ctor ----
    static void testBlackBox() {
        Exception cause = new Exception("cause");

        SuppressionThrowable e = new SuppressionThrowable("m", cause, false, false);
        check("m".equals(e.getMessage()) && e.getCause() == cause, "4-arg ctor sets message and cause");
        e.addSuppressed(new Exception("s"));
        check(e.getSuppressed().length == 0, "enableSuppression=false -> addSuppressed is a no-op");
        e.setStackTrace(new StackTraceElement[]{ new StackTraceElement("C", "m", "F.java", 1) });
        check(e.getStackTrace().length == 0, "writableStackTrace=false -> stack trace stays empty");

        SuppressionThrowable e2 = new SuppressionThrowable("m2", cause, true, true);
        e2.addSuppressed(new Exception("s2"));
        check(e2.getSuppressed().length == 1, "enableSuppression=true -> addSuppressed records");
        check(e2.getStackTrace().length > 0, "writableStackTrace=true -> stack trace filled");
    }

    // ---- Part 3: serialization round-trip ----
    static void testSerialization() throws Exception {
        Custom original = new Custom("boom");
        Exception cause = new Exception("cause");
        original.initCause(cause);
        original.addSuppressed(new Exception("suppressed"));

        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(original);
        oos.close();

        java.io.ObjectInputStream ois =
            new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray()));
        Custom copy = (Custom) ois.readObject();
        check("boom".equals(copy.getMessage()), "serialization preserves message");
        check(copy.getCause() != null && "cause".equals(copy.getCause().getMessage()),
              "serialization preserves cause");
        check(copy.getSuppressed().length == 1
              && "suppressed".equals(copy.getSuppressed()[0].getMessage()),
              "serialization preserves suppressed");
    }

    // ---- Deepening (lcO1, 2026-11): full Throwable contract matrix ----
    // MODERN is false on JDK-8 ("1.8.0_372") and true on JDK-27; lets one source
    // file lock per-side behavior where JVM messages differ.
    static final boolean MODERN = !System.getProperty("java.version").startsWith("1.");

    static class Localized extends Throwable {
        Localized(String m) { super(m); }
        public String getLocalizedMessage() { return "ZZZ"; }
    }

    static class FailingClose implements AutoCloseable {
        final RuntimeException boom;
        FailingClose(RuntimeException boom) { this.boom = boom; }
        public void close() { throw boom; }
    }

    static class CloseOrder implements AutoCloseable {
        final java.util.List<String> log;
        final String name;
        CloseOrder(java.util.List<String> log, String name) { this.log = log; this.name = name; }
        public void close() { log.add(name); }
    }

    static void testCtorMatrix() {
        Exception cause = new Exception("cause");
        Throwable[] forms = {
            new Throwable(),
            new Throwable("m"),
            new Throwable("m", cause),
            new Throwable(cause)
        };
        for (int i = 0; i < forms.length; i++) {
            check(forms[i] instanceof Throwable, "form " + i + " is a Throwable");
            check(forms[i].getStackTrace().length > 0, "form " + i + " captures a stack trace");
            check(forms[i].getLocalizedMessage() == forms[i].getMessage()
                  || forms[i].getLocalizedMessage().equals(forms[i].getMessage()),
                  "form " + i + " getLocalizedMessage defaults to getMessage");
        }
        check(forms[0].getMessage() == null && forms[0].toString().equals("java.lang.Throwable"),
              "no-arg form message null, bare toString");
        check(forms[1].getMessage().equals("m") && forms[1].toString().equals("java.lang.Throwable: m"),
              "(String) form message and toString exact");
        check(forms[2].getMessage().equals("m") && forms[2].getCause() == cause,
              "(String,Throwable) form message and cause identity");
        check(forms[3].getCause() == cause && cause.toString().equals(forms[3].getMessage()),
              "(Throwable) form message mirrors cause.toString()");
    }

    static void testCtorNullForms() {
        Throwable n1 = new Throwable((String) null);
        check(n1.getMessage() == null && n1.getCause() == null, "(String)null -> null message, null cause");
        check(n1.toString().equals("java.lang.Throwable"), "(String)null toString has no colon");
        Throwable n2 = new Throwable((String) null, new Exception("c"));
        check(n2.getMessage() == null && n2.getCause() != null, "(null,cause) -> null message, cause set");
        Throwable n3 = new Throwable((Throwable) null);
        check(n3.getMessage() == null && n3.getCause() == null, "(Throwable)null -> null message and cause");
        check(new Throwable("msg", (Throwable) null).getCause() == null, "(msg,null) -> cause null");
        check(new Throwable("").toString().equals("java.lang.Throwable: "), "empty message keeps ': ' in toString");
        check(new Throwable("msg") instanceof java.io.Serializable, "Throwable is Serializable");
        check(new Throwable("m").getLocalizedMessage().equals("m"), "getLocalizedMessage default equals message");
    }

    static void testCausePrecise() {
        Throwable t = new Throwable("m");
        try {
            t.initCause(t);
            throw new AssertionError("unreachable: initCause(self) must throw");
        } catch (IllegalArgumentException e) {
            check("Self-causation not permitted".equals(e.getMessage()),
                  "initCause(self) message is exactly 'Self-causation not permitted'");
        }
        Throwable ok = new Throwable();
        check(ok.initCause(null) == ok && ok.getCause() == null, "initCause(null) accepted, cause stays null");
        Throwable chain = new Throwable("outer", new IllegalStateException("inner"));
        try {
            chain.initCause(new Exception("other"));
            throw new AssertionError("unreachable: re-initCause must throw");
        } catch (IllegalStateException e) {
            check(e.getMessage() != null && e.getMessage().startsWith("Can't overwrite cause with"),
                  "initCause twice ISE message starts with 'Can't overwrite cause with'");
        }
        Throwable ctorCause = new Throwable(new IllegalArgumentException("root"));
        try {
            ctorCause.initCause(new Exception("x"));
            throw new AssertionError("unreachable");
        } catch (IllegalStateException expected) {
            passed++; // ctor-set cause also blocks initCause
        }
        Throwable selfCtor = new Throwable("m", new Throwable("c"));
        check(selfCtor.getCause().getMessage().equals("c"), "chained cause readable");
        check(selfCtor.getMessage().equals("m"), "cause does not overwrite explicit message");
        Throwable late = new Throwable();
        Exception c1 = new Exception("one");
        check(late.initCause(c1) == late && late.getCause() == c1, "initCause returns this and stores identity");
        check(late.getMessage() == null, "initCause does not synthesize a message");
    }

    static void testMessageChain() {
        Exception root = new IllegalArgumentException("root");
        Exception mid = new IllegalStateException("mid");
        mid.initCause(root);
        Throwable top = new Exception("top", mid);
        check(top.getCause() == mid && mid.getCause() == root, "three-level cause chain navigable");
        check(root.getCause() == null, "chain terminates in null");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        top.printStackTrace(new java.io.PrintStream(bos));
        String out = new String(bos.toByteArray(), java.nio.charset.StandardCharsets.UTF_8);
        check(out.contains("Caused by: java.lang.IllegalStateException: mid"), "first Caused by is mid");
        check(out.contains("Caused by: java.lang.IllegalArgumentException: root"), "second Caused by is root");
        check(top.toString().equals("java.lang.Exception: top"), "toString only shows direct message");
    }

    static void testSuppressedDeep() {
        Throwable t = new Throwable("m");
        check(t.getSuppressed().length == 0, "getSuppressed empty by default");
        try {
            t.addSuppressed(null);
            throw new AssertionError("unreachable");
        } catch (NullPointerException e) {
            check("Cannot suppress a null exception.".equals(e.getMessage()),
                  "addSuppressed(null) NPE message exact");
        }
        Throwable self = new Throwable("s");
        try {
            self.addSuppressed(self);
            throw new AssertionError("unreachable");
        } catch (IllegalArgumentException e) {
            check("Self-suppression not permitted".equals(e.getMessage()),
                  "addSuppressed(self) IAE message exact");
        }
        Exception s1 = new Exception("s1");
        Exception s2 = new Exception("s2");
        t.addSuppressed(s1);
        t.addSuppressed(s2);
        check(t.getSuppressed().length == 2, "two suppressed recorded");
        check(t.getSuppressed()[0] == s1 && t.getSuppressed()[1] == s2, "suppressed keep insertion order");
        check(t.getSuppressed() instanceof Throwable[], "getSuppressed returns Throwable[]");
        t.getSuppressed()[0] = null;
        check(t.getSuppressed()[0] == s1, "getSuppressed defensive copy");
        // suppression and cause coexist independently
        check(t.getCause() == null, "suppressed does not fabricate a cause");
        Throwable both = new Throwable("b", new Exception("bc"));
        both.addSuppressed(new Exception("bs"));
        check(both.getCause() != null && both.getSuppressed().length == 1, "cause and suppressed coexist");
    }

    static void testStackTraceDeep() {
        Throwable t = new Throwable();
        StackTraceElement[] st = t.getStackTrace();
        check(st.length > 0, "fresh stack trace non-empty");
        check(st[0].getClassName().equals("ThrowableTest"), "top frame class exact");
        check(st[0].getMethodName().equals("testStackTraceDeep"), "top frame method exact");
        check(st[0].getFileName().equals("ThrowableTest.java"), "top frame file name exact");
        check(st[0].toString().startsWith("ThrowableTest.testStackTraceDeep"), "top frame toString starts with class.method");
        StackTraceElement[] copy = t.getStackTrace();
        check(copy.length == st.length, "two getStackTrace calls agree");
        copy[0] = null;
        check(t.getStackTrace()[0] != null, "getStackTrace returns independent copy");
        Throwable empty = new Throwable();
        empty.setStackTrace(new StackTraceElement[0]);
        check(empty.getStackTrace().length == 0, "setStackTrace(empty array) clears trace");
        Throwable two = new Throwable();
        StackTraceElement e1 = new StackTraceElement("A", "a", "A.java", 1);
        StackTraceElement e2 = new StackTraceElement("B", "b", "B.java", 2);
        two.setStackTrace(new StackTraceElement[] { e1, e2 });
        check(two.getStackTrace().length == 2 && e1.equals(two.getStackTrace()[0]) && e2.equals(two.getStackTrace()[1]),
              "setStackTrace two elements round-trip");
        check(two.getStackTrace()[0].toString().equals("A.a(A.java:1)"), "StackTraceElement toString format exact");
        two.fillInStackTrace();
        check(two.getStackTrace()[0].getClassName().equals("ThrowableTest"),
              "fillInStackTrace discards a previously set stack trace");
        Throwable nullEl = new Throwable();
        try {
            nullEl.setStackTrace(new StackTraceElement[] { null });
            throw new AssertionError("unreachable");
        } catch (NullPointerException expected) {
            passed++;
        }
    }

    static void testFillInFrames() {
        Throwable t = new Throwable();
        check(t.fillInStackTrace() == t, "fillInStackTrace returns this");
        StackTraceElement[] st = t.getStackTrace();
        check(st.length > 0, "frames recorded");
        check(st[0].getClassName().equals("ThrowableTest") && st[0].getMethodName().equals("testFillInFrames"),
              "top frame class+method exact after fillInStackTrace");
        check(st[0].getLineNumber() > 0, "top frame line number positive");
        Throwable cleared = new Throwable();
        cleared.setStackTrace(new StackTraceElement[0]);
        cleared.fillInStackTrace();
        check(cleared.getStackTrace().length > 0, "fillInStackTrace after clearing restores real frames");
    }

    static void testPrintStackTraceDeep() {
        Throwable t = new Throwable("boom");
        IllegalArgumentException root = new IllegalArgumentException("root");
        t.initCause(root);
        IllegalStateException sup = new IllegalStateException("sup1");
        t.addSuppressed(sup);
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        t.printStackTrace(new java.io.PrintStream(bos));
        String out = new String(bos.toByteArray(), java.nio.charset.StandardCharsets.UTF_8);
        check(out.startsWith("java.lang.Throwable: boom"), "printStackTrace first line is toString");
        check(out.contains("\tat "), "printStackTrace contains '\\tat ' frame lines");
        check(out.contains("Suppressed: java.lang.IllegalStateException: sup1"), "Suppressed: section present");
        check(out.contains("Caused by: java.lang.IllegalArgumentException: root"), "Caused by: section present");
        check(out.indexOf("Suppressed: ") < out.indexOf("Caused by: "), "Suppressed printed before Caused by");
        java.io.ByteArrayOutputStream bos2 = new java.io.ByteArrayOutputStream();
        java.io.PrintWriter pw = new java.io.PrintWriter(bos2);
        t.printStackTrace(pw);
        pw.flush();
        String out2 = new String(bos2.toByteArray(), java.nio.charset.StandardCharsets.UTF_8);
        check(out2.startsWith("java.lang.Throwable: boom") && out2.contains("Caused by:"), "PrintWriter overload works");
        check(out2.contains("\tat "), "PrintWriter overload prints frame lines");
        Localized loc = new Localized("orig");
        check(loc.toString().endsWith("ZZZ"), "toString uses getLocalizedMessage");
        java.io.ByteArrayOutputStream bos3 = new java.io.ByteArrayOutputStream();
        loc.printStackTrace(new java.io.PrintStream(bos3));
        check(new String(bos3.toByteArray(), java.nio.charset.StandardCharsets.UTF_8).contains("ZZZ"),
              "printStackTrace uses getLocalizedMessage");
    }

    static void testPrintStackTraceDefault() {
        java.io.PrintStream saved = System.err;
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try {
            System.setErr(new java.io.PrintStream(bos));
            new Throwable("stderr").printStackTrace();
        } finally {
            System.setErr(saved);
        }
        String out = new String(bos.toByteArray(), java.nio.charset.StandardCharsets.UTF_8);
        check(out.startsWith("java.lang.Throwable: stderr"), "no-arg printStackTrace writes to System.err");
        check(out.contains("\tat "), "no-arg printStackTrace prints frames");
    }

    static void testTwrSuppressionReal() {
        RuntimeException closeEx = new IllegalStateException("close-failure");
        RuntimeException bodyEx = new UnsupportedOperationException("body-failure");
        try (FailingClose r = new FailingClose(closeEx)) {
            throw bodyEx;
        } catch (RuntimeException caught) {
            check(caught == bodyEx, "TWR primary is the body exception (same instance)");
            check(caught.getSuppressed().length == 1, "TWR suppresses exactly the close exception");
            check(caught.getSuppressed()[0] == closeEx, "TWR suppressed[0] is the close exception (same instance)");
            check(caught.getCause() == null, "TWR does not touch the cause chain");
        }
        RuntimeException solo = new IllegalStateException("solo");
        try (FailingClose r = new FailingClose(solo)) {
            passed++; // body completes normally
        } catch (RuntimeException caught) {
            check(caught == solo, "normal body: close exception propagates");
            check(caught.getSuppressed().length == 0, "normal body: nothing to suppress");
        }
        java.util.List<String> log = new java.util.ArrayList<String>();
        try (CloseOrder a = new CloseOrder(log, "A"); CloseOrder b = new CloseOrder(log, "B")) {
            log.add("body");
        }
        check(log.size() == 3, "TWR: body + two closes executed");
        check(log.get(0).equals("body") && log.get(1).equals("B") && log.get(2).equals("A"),
              "TWR closes resources in reverse declaration order");
        RuntimeException e1 = new RuntimeException("body");
        RuntimeException e2 = new RuntimeException("closeB");
        RuntimeException e3 = new RuntimeException("closeA");
        try (FailingClose x = new FailingClose(e3); FailingClose y = new FailingClose(e2)) {
            throw e1;
        } catch (RuntimeException caught) {
            check(caught == e1, "dual-failing-close: primary is body exception");
            check(caught.getSuppressed().length == 2, "dual-failing-close: both close exceptions suppressed");
            check(caught.getSuppressed()[0] == e2 && caught.getSuppressed()[1] == e3,
                  "dual-failing-close: suppression order is reverse close order");
        }
        // pre-existing suppression on the close exception survives being suppressed
        Exception preSup = new Exception("pre");
        RuntimeException withPre = new IllegalStateException("with-pre");
        withPre.addSuppressed(preSup);
        RuntimeException primary = new RuntimeException("primary");
        try (FailingClose r = new FailingClose(withPre)) {
            throw primary;
        } catch (RuntimeException caught) {
            check(caught == primary && caught.getSuppressed()[0] == withPre, "close ex attached as suppressed");
            check(withPre.getSuppressed().length == 1 && withPre.getSuppressed()[0] == preSup,
                  "suppression of a close exception keeps its own suppressed list");
        }
    }

    static void testSuppressionFlagMatrix() {
        Exception cause = new Exception("c");
        // suppression enabled, stack not writable
        SuppressionThrowable a = new SuppressionThrowable("a", cause, true, false);
        a.addSuppressed(new Exception("s"));
        check(a.getSuppressed().length == 1, "enableSuppression=true, writable=false: suppression works");
        check(a.getStackTrace().length == 0, "enableSuppression=true, writable=false: no stack trace");
        // suppression disabled, stack writable
        SuppressionThrowable b = new SuppressionThrowable("b", cause, false, true);
        b.addSuppressed(new Exception("s"));
        check(b.getSuppressed().length == 0, "enableSuppression=false, writable=true: suppression disabled");
        check(b.getStackTrace().length > 0, "enableSuppression=false, writable=true: stack captured");
        b.setStackTrace(new StackTraceElement[] { new StackTraceElement("Q", "q", "Q.java", 3) });
        check(b.getStackTrace()[0].getClassName().equals("Q"), "writable stack accepts setStackTrace");
    }

    static void testSuidLock() {
        check(java.io.ObjectStreamClass.lookup(Throwable.class).getSerialVersionUID() == -3042686055658047285L,
              "Throwable serialVersionUID locked via ObjectStreamClass (same on JDK-8 and JDK-27)");
    }

    static void testSerializationTrace() throws Exception {
        Throwable t = new Throwable("traced");
        t.setStackTrace(new StackTraceElement[] { new StackTraceElement("K", "k", "K.java", 9) });
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(t);
        oos.close();
        Object back = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        Throwable copy = (Throwable) back;
        check("traced".equals(copy.getMessage()), "serialization keeps message");
        check(copy.getCause() == null, "serialization keeps null cause");
        check(copy.getStackTrace().length == 1 && "K".equals(copy.getStackTrace()[0].getClassName()),
              "serialization keeps explicitly set stack trace");
        check(copy.getSuppressed().length == 0, "serialization keeps empty suppressed list");
    }

    static void testRethrowContract() {
        RuntimeException orig = null;
        try {
            throw new IllegalStateException("rethrow-me");
        } catch (IllegalStateException e) {
            orig = e;
        }
        StackTraceElement[] before = orig.getStackTrace();
        boolean sameTop = before.length > 0;
        try {
            throw orig;
        } catch (IllegalStateException e) {
            check(e == orig, "rethrown exception keeps identity");
            check(e.getStackTrace().length == before.length || !sameTop,
                  "rethrow does not refill the stack trace");
            check("rethrow-me".equals(e.getMessage()), "rethrown message preserved");
        }
        Throwable refilled = orig.fillInStackTrace();
        check(refilled == orig, "fillInStackTrace on caught exception returns this");
        check(orig.getStackTrace()[0].getMethodName().equals("testRethrowContract"),
              "fillInStackTrace on caught exception re-anchors the top frame here");
    }

    static void testNestedTwr() {
        java.util.List<String> log = new java.util.ArrayList<String>();
        RuntimeException outerClose = new IllegalStateException("outer-close");
        try (CloseOrder a = new CloseOrder(log, "A")) {
            try (CloseOrder b = new CloseOrder(log, "B")) {
                log.add("inner-body");
            } // b closed here, inner block completes normally
            log.add("outer-body");
        } catch (IllegalStateException e) {
            check(e == outerClose, "nested TWR: outer close exception propagates");
        }
        // note: outerClose never thrown in this variant; verify pure ordering instead
        check(log.toString().equals("[inner-body, B, outer-body, A]"),
              "nested TWR: inner resource closed before outer body continues");
        RuntimeException innerBody = new RuntimeException("inner-body-ex");
        try (FailingClose f = new FailingClose(outerClose)) {
            try {
                throw innerBody;
            } catch (RuntimeException inner) {
                check(inner == innerBody, "inner catch intercepts before resource suppression applies");
                check(inner.getSuppressed().length == 0, "caught-in-body exception has no suppression");
            }
            throw innerBody; // rethrow the same instance as the resource-body exception
        } catch (RuntimeException e) {
            check(e == innerBody, "rethrown instance propagates as the primary exception");
            check(e.getSuppressed().length == 1 && e.getSuppressed()[0] == outerClose,
                  "close exception is suppressed onto the rethrown primary");
        }
    }

    static void testSuppressedSerializationNested() throws Exception {
        Exception innerSup = new Exception("inner-sup");
        Exception closeEx = new IllegalStateException("close-with-sup");
        closeEx.addSuppressed(innerSup);
        Exception primary = new Exception("primary");
        primary.addSuppressed(closeEx);
        primary.initCause(new Exception("root"));
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(primary);
        oos.close();
        Exception copy = (Exception) new java.io.ObjectInputStream(
            new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check("primary".equals(copy.getMessage()), "nested-suppression serialization keeps message");
        check(copy.getCause() != null && "root".equals(copy.getCause().getMessage()),
              "nested-suppression serialization keeps cause");
        check(copy.getSuppressed().length == 1, "nested-suppression serialization keeps suppressed");
        Throwable copyClose = copy.getSuppressed()[0];
        check("close-with-sup".equals(copyClose.getMessage()), "suppressed close exception survives");
        check(copyClose.getSuppressed().length == 1 && "inner-sup".equals(copyClose.getSuppressed()[0].getMessage()),
              "suppressed list of a suppressed exception survives");
    }

    static void testStackTraceElementContract() {
        StackTraceElement a = new StackTraceElement("C", "m", "F.java", 1);
        StackTraceElement b = new StackTraceElement("C", "m", "F.java", 1);
        StackTraceElement c = new StackTraceElement("C", "m", "G.java", 1);
        check(a.equals(b) && b.equals(a), "StackTraceElement equals symmetric");
        check(a.hashCode() == b.hashCode(), "equal elements share hashCode");
        check(!a.equals(c), "different file breaks equals");
        check(!a.equals(new StackTraceElement("C", "m", "F.java", 2)), "different line breaks equals");
        check(!a.equals(null), "not equal to null");
        check(a.toString().equals("C.m(F.java:1)"), "toString format exact");
        Throwable t = new Throwable();
        t.setStackTrace(new StackTraceElement[] { a });
        check(t.getStackTrace()[0].equals(b), "set element compares equal to identical reconstruction");
    }

    static void testManySuppressedOrder() {
        Throwable t = new Throwable("many");
        Exception[] all = new Exception[5];
        for (int i = 0; i < all.length; i++) {
            all[i] = new Exception("e" + i);
            t.addSuppressed(all[i]);
        }
        check(t.getSuppressed().length == 5, "five suppressed recorded");
        boolean ordered = true;
        for (int i = 0; i < 5; i++) ordered &= t.getSuppressed()[i] == all[i];
        check(ordered, "five suppressed preserve insertion order");
    }

    static void testFillInOnCaught() {
        Exception caught;
        try {
            throw new Exception("anchor");
        } catch (Exception e) {
            caught = e;
        }
        String firstMethod = caught.getStackTrace()[0].getMethodName();
        check(firstMethod.equals("testFillInOnCaught"), "original top frame is the throw site");
        helperRefill(caught);
        check(caught.getStackTrace()[0].getMethodName().equals("helperRefill"),
              "fillInStackTrace inside helper re-anchors top frame to helper");
    }

    static void helperRefill(Throwable t) {
        t.fillInStackTrace();
    }

    static void testCustomMessageOverrides() {
        class OverrideMsg extends Throwable {
            OverrideMsg(String m) { super(m); }
            public String getMessage() { return "OVERRIDDEN"; }
        }
        OverrideMsg o = new OverrideMsg("hidden");
        check("OVERRIDDEN".equals(o.getMessage()), "getMessage override wins");
        check("OVERRIDDEN".equals(o.getLocalizedMessage()),
              "getLocalizedMessage delegates to the overridden getMessage");
        check(o.toString().endsWith("OVERRIDDEN"), "toString shows the overridden message");
        class OverrideLoc extends Throwable {
            OverrideLoc(String m) { super(m); }
            public String getLocalizedMessage() { return "LOC"; }
        }
        OverrideLoc l = new OverrideLoc("plain");
        check(l.toString().endsWith("LOC"), "getLocalizedMessage override shows in toString");
        check(l.getMessage().equals("plain"), "getMessage untouched by localized override");
    }

    static void testCauseCycleOfTwo() {
        // Two-exception cause cycle is constructible (only self-causation is rejected);
        // navigation is safe, printing is not attempted here.
        Throwable a = new Throwable("a");
        Throwable b = new Throwable("b");
        a.initCause(b);
        b.initCause(a);
        check(a.getCause() == b, "cycle: a -> b");
        check(b.getCause() == a, "cycle: b -> a");
        check(a.getCause().getCause() == a, "cycle: a -> b -> a");
    }

    static void testGetStackTraceStability() {
        Throwable t = new Throwable();
        StackTraceElement[] s1 = t.getStackTrace();
        StackTraceElement[] s2 = t.getStackTrace();
        check(s1.length == s2.length, "repeat getStackTrace same length");
        boolean same = true;
        for (int i = 0; i < s1.length; i++) same &= s1[i].equals(s2[i]);
        check(same, "repeat getStackTrace same elements");
        check(s1 != s2, "each call returns a fresh array");
    }

    static void testTwrCloseOrderThree() {
        java.util.List<String> log = new java.util.ArrayList<String>();
        try (CloseOrder a = new CloseOrder(log, "A"); CloseOrder b = new CloseOrder(log, "B"); CloseOrder c = new CloseOrder(log, "C")) {
            log.add("body");
        }
        check(log.size() == 4, "three-resource TWR: body + three closes");
        check(log.get(1).equals("C") && log.get(2).equals("B") && log.get(3).equals("A"),
              "three-resource TWR closes C then B then A");
    }

    static void testPrintBareForm() {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        new Throwable("plain").printStackTrace(new java.io.PrintStream(bos));
        String out = new String(bos.toByteArray(), java.nio.charset.StandardCharsets.UTF_8);
        check(!out.contains("Caused by"), "no cause section without a cause");
        check(!out.contains("Suppressed"), "no suppressed section without suppression");
        check(out.startsWith("java.lang.Throwable: plain"), "bare first line exact");
        check(out.endsWith("\n"), "output ends with newline");
    }

    static void testFourFormsInstanceof() {
        Exception cause = new Exception("c");
        Object[] forms = { new Throwable(), new Throwable("m"), new Throwable("m", cause), new Throwable(cause) };
        for (int i = 0; i < forms.length; i++) {
            check(forms[i] instanceof Throwable && forms[i] instanceof java.io.Serializable,
                  "form " + i + " is Throwable and Serializable");
            check(!(forms[i] instanceof Exception) && !(forms[i] instanceof Error),
                  "form " + i + " is neither Exception nor Error");
        }
    }

    static void testInitCauseVariousTargets() {
        Error errCause = new Error("err");
        Throwable t1 = new Throwable();
        t1.initCause(errCause);
        check(t1.getCause() == errCause, "Error as cause accepted");
        check(t1.getCause().toString().equals("java.lang.Error: err"), "cause toString exact");
        Throwable t2 = new Throwable();
        Throwable selfNested = new Throwable("n", new Throwable("inner"));
        t2.initCause(selfNested);
        check(t2.getCause() == selfNested && selfNested.getCause().getMessage().equals("inner"),
              "Throwable-with-cause as cause keeps nested chain");
        Throwable t3 = new Throwable();
        t3.initCause(t3.getCause()); // initCause(null) is legal
        check(t3.getCause() == null, "initCause(current null cause) stays null");
    }

    static void testCombinedContract() {
        Exception cause = new Exception("root-cause");
        Throwable t = new Throwable("combined", cause);
        t.addSuppressed(new Exception("sup-a"));
        t.addSuppressed(new Exception("sup-b"));
        t.setStackTrace(new StackTraceElement[] { new StackTraceElement("Z", "z", "Z.java", 42) });
        check(t.getMessage().equals("combined"), "combined: message intact");
        check(t.getCause() == cause, "combined: cause intact after suppression+trace set");
        check(t.getSuppressed().length == 2, "combined: suppressed intact after trace set");
        check(t.getStackTrace().length == 1 && t.getStackTrace()[0].getLineNumber() == 42,
              "combined: explicit trace intact");
        check(t.getSuppressed() != t.getSuppressed(), "getSuppressed returns a fresh array each call");
        Throwable[] arr1 = t.getSuppressed();
        Throwable[] arr2 = t.getSuppressed();
        check(arr1[0] == arr2[0] && arr1[1] == arr2[1], "fresh arrays carry identical elements");
        check(t.toString().equals("java.lang.Throwable: combined"), "combined: toString exact");
        check(t.getCause().getSuppressed().length == 0, "cause's own suppressed list stays independent");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testConstructors();
        testInitCause();
        testSuppressed();
        testStackTrace();
        testToStringAndMessage();
        testPrintStackTrace();
        testBlackBox();
        testSerialization();
        testCtorMatrix();
        testCtorNullForms();
        testCausePrecise();
        testMessageChain();
        testSuppressedDeep();
        testStackTraceDeep();
        testFillInFrames();
        testPrintStackTraceDeep();
        testPrintStackTraceDefault();
        testTwrSuppressionReal();
        testSuppressionFlagMatrix();
        testSuidLock();
        testSerializationTrace();
        testRethrowContract();
        testNestedTwr();
        testSuppressedSerializationNested();
        testStackTraceElementContract();
        testManySuppressedOrder();
        testFillInOnCaught();
        testCustomMessageOverrides();
        testCauseCycleOfTwo();
        testGetStackTraceStability();
        testTwrCloseOrderThree();
        testPrintBareForm();
        testFourFormsInstanceof();
        testInitCauseVariousTargets();
        testCombinedContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
