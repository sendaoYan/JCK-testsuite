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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testGetters();
        testToString();
        testEqualsHashCode();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
