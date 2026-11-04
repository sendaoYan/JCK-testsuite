/*
 * @test
 * @summary java.lang.StackTraceElement conformance: white-box structure + black-box behavior.
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
        check(c.getConstructor(String.class, String.class, String.class, String.class,
                               String.class, String.class, int.class) != null,
              "7-arg constructor present");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testGetters() {
        StackTraceElement s = new StackTraceElement("C", "m", "F.java", 42);
        check("C".equals(s.getClassName()), "getClassName");
        check("m".equals(s.getMethodName()), "getMethodName");
        check("F.java".equals(s.getFileName()), "getFileName");
        check(s.getLineNumber() == 42, "getLineNumber");
        check(!s.isNativeMethod(), "line 42 is not native");
    }

    static void testSevenArgConstructor() {
        // Order: classLoaderName, moduleName, moduleVersion, className, methodName, fileName, lineNumber
        StackTraceElement s = new StackTraceElement("loader", "module", "1.0", "C", "m", "F.java", 3);
        check("loader".equals(s.getClassLoaderName()), "getClassLoaderName");
        check("module".equals(s.getModuleName()), "getModuleName");
        check("1.0".equals(s.getModuleVersion()), "getModuleVersion");
        check("C".equals(s.getClassName()), "7-arg getClassName");
        check("m".equals(s.getMethodName()), "7-arg getMethodName");
        check("F.java".equals(s.getFileName()) && s.getLineNumber() == 3, "7-arg file and line");
        // The 4-arg constructor leaves module/class-loader info unset.
        check(new StackTraceElement("C", "m", "F.java", 42).getModuleName() == null,
              "4-arg ctor has null module name");
        check(new StackTraceElement("C", "m", "F.java", 42).getClassLoaderName() == null,
              "4-arg ctor has null class loader name");
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

    
    static void testBlackBox() throws Exception {
        testGetters();
        testSevenArgConstructor();
        testToString();
        testEqualsHashCode();
    }
public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
