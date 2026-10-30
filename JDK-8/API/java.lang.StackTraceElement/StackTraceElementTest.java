/*
 * @test
 * @summary java.lang.StackTraceElement API conformance: the four-argument constructor,
 *          getters, isNativeMethod, toString formats (source line / native / unknown), and
 *          value-based equals/hashCode.
 * @run main StackTraceElementTest
 */

public class StackTraceElementTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
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

    public static void main(String[] args) {
        testGetters();
        testToString();
        testEqualsHashCode();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
