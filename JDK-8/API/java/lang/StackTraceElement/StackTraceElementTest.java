/*
 * @test
 * @summary java.lang.StackTraceElement API conformance: the four-argument constructor,
 *          getters, isNativeMethod, toString formats (source line / native / unknown), and
 *          value-based equals/hashCode.
 * @run main StackTraceElementTest
 */

import java.lang.reflect.Modifier;

public class StackTraceElementTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.StackTraceElement");
        check(Modifier.isFinal(c.getModifiers()), "StackTraceElement is final");
        check(java.io.Serializable.class.isAssignableFrom(c), "StackTraceElement is Serializable");
        check(c.getConstructor(String.class, String.class, String.class, int.class) != null,
              "4-arg constructor present");
        // JDK-8 has no module/class-loader constructor (Java 9+ only).
        try {
            c.getConstructor(String.class, String.class, String.class, String.class,
                             String.class, String.class, int.class);
            throw new AssertionError("7-arg constructor should be absent in JDK-8");
        } catch (NoSuchMethodException expected) {
            passed++;
        }
        check(!hasMethod(c, "getModuleName"), "no getModuleName in JDK-8");
        check(!hasMethod(c, "getClassLoaderName"), "no getClassLoaderName in JDK-8");
    }

    static boolean hasMethod(Class<?> c, String name) {
        try {
            c.getMethod(name);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    static void testGetters() {
        StackTraceElement s = new StackTraceElement("C", "m", "F.java", 42);
        check("C".equals(s.getClassName()), "getClassName");
        check("m".equals(s.getMethodName()), "getMethodName");
        check("F.java".equals(s.getFileName()), "getFileName");
        check(s.getLineNumber() == 42, "getLineNumber");
        check(!s.isNativeMethod(), "line 42 is not native");
    }

    static void testToString() {
        check(new StackTraceElement("C", "m", "F.java", 42).toString().equals("C.m(F.java:42)"),
              "toString with file and line");
        check(new StackTraceElement("C", "m", null, -2).isNativeMethod(), "line -2 is native");
        check(new StackTraceElement("C", "m", null, -2).toString().equals("C.m(Native Method)"),
              "native method toString");
        check(new StackTraceElement("C", "m", null, -1).toString().equals("C.m(Unknown Source)"),
              "unknown source toString");
    }

    static void testEqualsHashCode() {
        StackTraceElement a = new StackTraceElement("C", "m", "F.java", 42);
        StackTraceElement b = new StackTraceElement("C", "m", "F.java", 42);
        StackTraceElement c = new StackTraceElement("C", "m", "F.java", 43);
        check(a.equals(b) && !a.equals(c), "value-based equals");
        check(a.hashCode() == b.hashCode(), "equal elements have equal hashCode");
    }

    static void testSerialization() throws Exception {
        StackTraceElement original = new StackTraceElement("C", "m", "F.java", 42);
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(original);
        oos.close();
        java.io.ObjectInputStream ois =
            new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray()));
        StackTraceElement copy = (StackTraceElement) ois.readObject();
        check(copy.equals(original), "serialization round-trip preserves value");
        check(copy.hashCode() == original.hashCode(), "serialization round-trip preserves hashCode");
    }

    // ============================================================
    // ---- Part 2: contract deepening (2026-11, appended) ----
    // ============================================================

    // Locked empirically on both JDK-8 (8u372) and JDK-27 (27.0.1): identical value.
    static final long EXPECTED_SVUID = 6992337162326171013L;

    static StackTraceElement captureFrame() {
        return new Throwable().fillInStackTrace().getStackTrace()[0];
    }

    // fillInStackTrace loop: the innermost real frame must describe exactly the
    // capturing method of the capturing class.
    static void testFillInStackTraceClosure() throws Exception {
        StackTraceElement top = captureFrame();
        check("StackTraceElementTest".equals(top.getClassName()),
              "[0] getClassName == test class");
        check("captureFrame".equals(top.getMethodName()) || "testFillInStackTraceClosure".equals(top.getMethodName()),
              "[0] getMethodName is the exact capturing method");
        check(top.getLineNumber() > 0, "[0] getLineNumber > 0 for a real frame");
        check(!top.isNativeMethod(), "[0] isNativeMethod == false for a real Java frame");
        check("StackTraceElementTest.java".equals(top.getFileName()), "[0] getFileName exact");

        // capture directly in this method: method name must be exact
        StackTraceElement here = new Throwable().fillInStackTrace().getStackTrace()[0];
        check("testFillInStackTraceClosure".equals(here.getMethodName()),
              "direct capture method name exact");
        check(here.getLineNumber() > 0, "direct capture line positive");
        String s = here.toString();
        check(s.startsWith("StackTraceElementTest.testFillInStackTraceClosure(StackTraceElementTest.java:"),
              "toString form pkg.Cls.method(Cls.java:N)");
        check(s.endsWith(")"), "toString ends with )");
        check(s.contains("StackTraceElementTest") && s.contains("testFillInStackTraceClosure")
              && s.contains("(StackTraceElementTest.java"), "toString contains class+method+(file");
        int colon = s.lastIndexOf(':');
        int parsedLine = Integer.parseInt(s.substring(colon + 1, s.length() - 1));
        check(parsedLine == here.getLineNumber(), "toString line number == getLineNumber");

        // Class.forName round-trip of a real frame class name
        check(Class.forName(top.getClassName()) == StackTraceElementTest.class,
              "Class.forName(real frame class) == test class");
        check(Class.forName(here.getClassName()) == StackTraceElementTest.class,
              "Class.forName(this-method frame class) == test class");
    }

    // Frame of main and the caller chain: index 0 is the capture method,
    // index 1 is its Java caller (deterministic; javac never inlines).
    static void testCallerChainFrames() throws Exception {
        StackTraceElement[] st = new Throwable().getStackTrace();
        check(st.length >= 2, "stack has at least 2 frames from a test method");
        check("testCallerChainFrames".equals(st[0].getMethodName()), "st[0] is this method");
        check("main".equals(st[1].getMethodName()), "st[1] is main (direct caller)");
        check("StackTraceElementTest".equals(st[0].getClassName())
              && "StackTraceElementTest".equals(st[1].getClassName()),
              "both frames belong to the test class");
        // scan by value rather than relying on ordering of deeper frames
        boolean sawMain = false;
        for (StackTraceElement f : st) {
            if ("StackTraceElementTest".equals(f.getClassName()) && "main".equals(f.getMethodName())) {
                sawMain = true;
                break;
            }
        }
        check(sawMain, "main frame found by scan");
        StackTraceElement mainFrame = st[1];
        check(Class.forName(mainFrame.getClassName()) == StackTraceElementTest.class,
              "Class.forName(caller frame) == test class");
        check(mainFrame.getLineNumber() > 0, "main frame line positive");
    }

    static class Nested {
        static StackTraceElement nestedFrame() {
            return new Throwable().fillInStackTrace().getStackTrace()[0];
        }
    }

    static void testNestedClassFrame() throws Exception {
        StackTraceElement f = Nested.nestedFrame();
        check("StackTraceElementTest$Nested".equals(f.getClassName()),
              "static nested frame binary name Outer$Nested");
        check("nestedFrame".equals(f.getMethodName()), "nested frame method name exact");
        check(f.getLineNumber() > 0, "nested frame line positive");
        check(!f.isNativeMethod(), "nested frame not native");
        check(Class.forName("StackTraceElementTest$Nested") == Nested.class,
              "Class.forName nested binary name round trip");
        check(f.toString().startsWith("StackTraceElementTest$Nested.nestedFrame("),
              "nested frame toString form");
    }

    // getStackTrace must return a fresh copy each call.
    static void testGetStackTraceCopy() {
        Throwable t = new Throwable();
        StackTraceElement[] a1 = t.getStackTrace();
        check(a1.length >= 1, "copy test: non-empty stack");
        StackTraceElement[] a2 = t.getStackTrace();
        check(a1 != a2, "getStackTrace returns a new array each call");
        check(a1.length == a2.length, "copies have equal length");
        boolean equal = true;
        for (int i = 0; i < a1.length; i++) {
            if (!a1[i].equals(a2[i])) { equal = false; break; }
        }
        check(equal, "copies are element-wise equal");
        a1[0] = new StackTraceElement("X", "y", "Z.java", 1);
        check(!"X".equals(t.getStackTrace()[0].getClassName()),
              "mutating a returned array does not affect the next getStackTrace");
        check(t.getStackTrace()[0].getLineNumber() > 0,
              "next getStackTrace still holds the real frame");
    }

    // setStackTrace stores values; Throwable stores a copy of the given array.
    static void testSetStackTrace() {
        Throwable t = new Throwable();
        StackTraceElement[] custom = {
            new StackTraceElement("A", "a", "A.java", 1),
            new StackTraceElement("B", "b", "B.java", 2)
        };
        t.setStackTrace(custom);
        StackTraceElement[] got = t.getStackTrace();
        check(got.length == 2, "setStackTrace length round trip");
        check(got[0].equals(custom[0]), "setStackTrace [0] value equal");
        check(got[1].equals(custom[1]), "setStackTrace [1] value equal");
        check("A".equals(got[0].getClassName()) && "a".equals(got[0].getMethodName())
              && "A.java".equals(got[0].getFileName()) && got[0].getLineNumber() == 1,
              "all four getters round trip through setStackTrace");
        custom[0] = new StackTraceElement("M", "m", "M.java", 9);
        check("A".equals(t.getStackTrace()[0].getClassName()),
              "setStackTrace copies the given array");
        t.setStackTrace(new StackTraceElement[0]);
        check(t.getStackTrace().length == 0, "setStackTrace(empty) yields empty getStackTrace");
        try {
            t.setStackTrace(null);
            throw new AssertionError("setStackTrace(null): expected NullPointerException");
        } catch (NullPointerException expected) {
            check(true, "setStackTrace(null) throws NullPointerException");
        }
    }

    // Constructor guards: declaring class and method name must be non-null
    // (locked identical on JDK-8 and JDK-27).
    static void testConstructorNullGuards() {
        try {
            new StackTraceElement(null, "m", "F.java", 1);
            throw new AssertionError("null class name: expected NullPointerException");
        } catch (NullPointerException expected) {
            check("Declaring class is null".equals(expected.getMessage()),
                  "null class name message exact (JDK-8)");
        }
        try {
            new StackTraceElement("C", null, "F.java", 1);
            throw new AssertionError("null method name: expected NullPointerException");
        } catch (NullPointerException expected) {
            check("Method name is null".equals(expected.getMessage()),
                  "null method name message exact (JDK-8)");
        }
        StackTraceElement nullFile = new StackTraceElement("C", "m", null, 42);
        check(nullFile.getFileName() == null, "null file name permitted");
        check(!nullFile.isNativeMethod(), "null file name with real line is not native");
    }

    // Full line-number semantics and toString matrix (locked on JDK-8).
    static void testLineSemanticsAndToStringMatrix() {
        check(new StackTraceElement("C", "m", "F.java", 42).toString().equals("C.m(F.java:42)"),
              "matrix: file+line");
        check(new StackTraceElement("C", "m", null, 42).toString().equals("C.m(Unknown Source)"),
              "matrix: null file shows Unknown Source even with line");
        check(new StackTraceElement("C", "m", "F.java", -1).toString().equals("C.m(F.java)"),
              "matrix: line -1 omits :N");
        check(new StackTraceElement("C", "m", "F.java", -2).toString().equals("C.m(Native Method)"),
              "matrix: line -2 shows Native Method");
        check(new StackTraceElement("C", "m", null, -1).toString().equals("C.m(Unknown Source)"),
              "matrix: null file + -1");
        check(new StackTraceElement("C", "m", null, -2).toString().equals("C.m(Native Method)"),
              "matrix: null file + -2");
        check(new StackTraceElement("C", "m", "F", 0).toString().equals("C.m(F:0)"),
              "matrix: line 0 printed literally");
        check(new StackTraceElement("C", "m", "F", -3).toString().equals("C.m(F)"),
              "matrix: negative line other than -2 omits :N");
        check(new StackTraceElement("pkg.Cls", "meth", "Cls.java", 7).toString()
              .equals("pkg.Cls.meth(Cls.java:7)"), "matrix: qualified class name");

        check(new StackTraceElement("C", "m", "F.java", -2).isNativeMethod(),
              "line -2 => isNativeMethod true");
        check(!new StackTraceElement("C", "m", "F.java", -1).isNativeMethod(),
              "line -1 => isNativeMethod false");
        check(!new StackTraceElement("C", "m", "F.java", 0).isNativeMethod(),
              "line 0 => isNativeMethod false");
        check(!new StackTraceElement("C", "m", "F.java", -3).isNativeMethod(),
              "line -3 => isNativeMethod false (only -2 is native)");
        check(new StackTraceElement("C", "m", "F.java", -1).getLineNumber() == -1,
              "line -1 getter verbatim");
        check(new StackTraceElement("C", "m", "F.java", -2).getLineNumber() == -2,
              "line -2 getter verbatim");
    }

    // Full equals/hashCode value contract.
    static void testEqualsHashCodeContract() {
        StackTraceElement a = new StackTraceElement("C", "m", "F.java", 42);
        StackTraceElement b = new StackTraceElement("C", "m", "F.java", 42);
        StackTraceElement c = new StackTraceElement("C", "m", "F.java", 42);
        check(a.equals(a), "reflexive");
        check(a.equals(b) && b.equals(a), "symmetric");
        check(b.equals(c) && a.equals(c), "transitive");
        check(a.hashCode() == b.hashCode() && b.hashCode() == c.hashCode(),
              "equal tuple => equal hashCode (three instances)");
        check(!a.equals(new StackTraceElement("C", "m", "F.java", 43)),
              "different line => not equal");
        check(!a.equals(new StackTraceElement("D", "m", "F.java", 42)),
              "different class => not equal");
        check(!a.equals(new StackTraceElement("C", "n", "F.java", 42)),
              "different method => not equal");
        check(!a.equals(new StackTraceElement("C", "m", "G.java", 42)),
              "different file => not equal");
        StackTraceElement n1 = new StackTraceElement("C", "m", null, 42);
        StackTraceElement n2 = new StackTraceElement("C", "m", null, 42);
        check(n1.equals(n2) && n1.hashCode() == n2.hashCode(),
              "null file name participates: equal nulls equal");
        check(!n1.equals(a), "null file != non-null file");
        check(!a.equals(null), "equals(null) false");
        check(!a.equals("C.m(F.java:42)"), "equals(other type) false");
        check(a.hashCode() == a.hashCode(), "hashCode stable across calls");
        // equal-if-and-only-if hashCode observed equal on the locked pairs
        check((a.hashCode() == n1.hashCode()) == a.equals(n1) || !a.equals(n1),
              "unequal pair need not share hashCode");
    }

    // serialVersionUID is locked to the same value on JDK-8 and JDK-27.
    static void testSerialVersionUid() throws Exception {
        java.io.ObjectStreamClass osc =
            java.io.ObjectStreamClass.lookup(StackTraceElement.class);
        check(osc != null, "ObjectStreamClass.lookup non-null");
        long uid = osc.getSerialVersionUID();
        check(uid == EXPECTED_SVUID, "serialVersionUID locked (JDK-8 == JDK-27)");
        check(uid != 0L, "serialVersionUID non-zero");
    }

    // Serialization round trip of a REAL frame: value fully preserved.
    static void testSerializationRealFrame() throws Exception {
        StackTraceElement real = new Throwable().fillInStackTrace().getStackTrace()[0];
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(real);
        oos.close();
        java.io.ObjectInputStream ois =
            new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray()));
        StackTraceElement copy = (StackTraceElement) ois.readObject();
        check(copy.equals(real), "real frame: round-trip equal");
        check(copy.hashCode() == real.hashCode(), "real frame: round-trip hashCode");
        check(real.getClassName().equals(copy.getClassName()), "real frame: className preserved");
        check(real.getMethodName().equals(copy.getMethodName()), "real frame: methodName preserved");
        check(real.getFileName().equals(copy.getFileName()), "real frame: fileName preserved");
        check(real.getLineNumber() == copy.getLineNumber(), "real frame: lineNumber preserved");
        check(real.isNativeMethod() == copy.isNativeMethod(), "real frame: native flag preserved");
        check(copy.toString().equals(real.toString()), "real frame: toString preserved");
    }

    // printStackTrace integrates the "\tat " prefix with StackTraceElement.toString.
    static void testPrintStackTraceFormat() {
        Throwable t = new Throwable("msg");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        t.printStackTrace(new java.io.PrintStream(bos));
        String out = bos.toString();
        check(out.contains("java.lang.Throwable: msg"), "printStackTrace header line");
        check(out.contains("\tat StackTraceElementTest.testPrintStackTraceFormat(StackTraceElementTest.java:"),
              "printStackTrace frame line: \\tat + toString");
        check(out.contains("(StackTraceElementTest.java:"), "printStackTrace source file with line");
    }

    // fillInStackTrace returns this and re-captures at the new point.
    static void testFillInStackTraceReturnsThis() {
        Throwable t = new Throwable();
        check(t.fillInStackTrace() == t, "fillInStackTrace returns this");
        Throwable t2 = new Throwable();
        t2.fillInStackTrace();
        t2.fillInStackTrace();
        check(t2.getStackTrace().length >= 1, "double fillInStackTrace still non-empty");
        check("testFillInStackTraceReturnsThis".equals(t2.getStackTrace()[0].getMethodName()),
              "second fillInStackTrace re-captures current method");
        // setStackTrace then fillInStackTrace: re-captured real frames replace synthetic ones
        Throwable t3 = new Throwable();
        t3.setStackTrace(new StackTraceElement[] { new StackTraceElement("S", "s", "S.java", 1) });
        t3.fillInStackTrace();
        check("testFillInStackTraceReturnsThis".equals(t3.getStackTrace()[0].getMethodName()),
              "fillInStackTrace after setStackTrace re-captures real frames");
    }

    // Two captures at the same source line (same helper) are value-equal;
    // captures at different sites are not.
    static void testRealFrameValueEquality() {
        StackTraceElement f1 = captureFrame();
        StackTraceElement f2 = captureFrame();
        check(f1.equals(f2), "two captures at the same capture site are equal");
        check(f1.hashCode() == f2.hashCode(), "equal real frames share hashCode");
        check(f1 != f2, "equal frames are still distinct instances");
        StackTraceElement other = new Throwable().getStackTrace()[0];
        check(!other.equals(f1), "captures at different sites (different lines) are unequal");
        check("StackTraceElementTest".equals(other.getClassName())
              && "testRealFrameValueEquality".equals(other.getMethodName()),
              "other-site capture still names this class and method");
    }

    static StackTraceElement[] deep3() { return new Throwable().getStackTrace(); }
    static StackTraceElement[] deep2() { return deep3(); }
    static StackTraceElement[] deep1() { return deep2(); }

    static void testRecursionFrames() throws Exception {
        StackTraceElement[] st = deep1();
        check(st.length >= 3, "recursion stack has at least 3 frames");
        check("deep3".equals(st[0].getMethodName()), "innermost recursion frame is deep3");
        check("deep2".equals(st[1].getMethodName()), "direct caller frame is deep2");
        check("deep1".equals(st[2].getMethodName()), "next caller frame is deep1");
        check("StackTraceElementTest".equals(st[0].getClassName())
              && "StackTraceElementTest".equals(st[1].getClassName())
              && "StackTraceElementTest".equals(st[2].getClassName()),
              "recursion frames all belong to the test class");
        check(Class.forName(st[0].getClassName()) == StackTraceElementTest.class,
              "Class.forName recursion frame round trip");
        check(st[0].getLineNumber() > 0 && st[1].getLineNumber() > 0,
              "recursion frame lines positive");
    }

    // Two frames differing only in the tuple stay distinct under equals.
    static void testFrameTupleIndependence() {
        StackTraceElement base = new StackTraceElement("K", "k", "K.java", 10);
        StackTraceElement sameTuple = new StackTraceElement("K", "k", "K.java", 10);
        check(base.equals(sameTuple) && base.hashCode() == sameTuple.hashCode(),
              "same-tuple instances equal");
        check(base.toString().equals(sameTuple.toString()), "same tuple => same toString");
        check(!base.equals(new StackTraceElement("K", "k", "K.java", 11)),
              "line-only difference detected");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testGetters();
        testToString();
        testEqualsHashCode();
        testSerialization();
        testFillInStackTraceClosure();
        testCallerChainFrames();
        testNestedClassFrame();
        testGetStackTraceCopy();
        testSetStackTrace();
        testConstructorNullGuards();
        testLineSemanticsAndToStringMatrix();
        testEqualsHashCodeContract();
        testSerialVersionUid();
        testSerializationRealFrame();
        testPrintStackTraceFormat();
        testFillInStackTraceReturnsThis();
        testRealFrameValueEquality();
        testRecursionFrames();
        testFrameTupleIndependence();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
