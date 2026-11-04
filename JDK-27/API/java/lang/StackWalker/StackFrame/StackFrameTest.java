/*
 * @test
 * @summary java.lang.StackWalker.StackFrame conformance: white-box structure + black-box behavior.
 * @run main StackFrameTest
 */

import java.lang.StackWalker;

public class StackFrameTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.StackWalker$StackFrame");
        check(c.isInterface(), "StackFrame is an interface");
        check(c.getMethod("getClassName") != null, "getClassName declared");
        check(c.getMethod("getMethodName") != null, "getMethodName declared");
        check(c.getMethod("getDeclaringClass") != null, "getDeclaringClass declared");
        check(c.getMethod("getMethodType") != null, "getMethodType declared");
        check(c.getMethod("getDescriptor") != null, "getDescriptor declared");
        check(c.getMethod("toStackTraceElement") != null, "toStackTraceElement declared");
        check(c.getMethod("isNativeMethod") != null, "isNativeMethod declared");
    }

    // ---- Part 2: black-box (behavioral) ----
    static StackWalker.StackFrame firstFrame() {
        return StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
                          .walk(s -> s.findFirst().orElseThrow());
    }

    static void testFrame() {
        StackWalker.StackFrame frame = firstFrame();
        check("StackFrameTest".equals(frame.getClassName()), "className is the declaring class");
        check("firstFrame".equals(frame.getMethodName()), "methodName is the executing method");
        check(frame.getLineNumber() > 0, "lineNumber > 0");
        check(frame.getByteCodeIndex() >= 0, "byteCodeIndex >= 0");
        check(!frame.isNativeMethod(), "not native");
        check("StackFrameTest.java".equals(frame.getFileName()), "fileName");
        check(frame.getDeclaringClass() == StackFrameTest.class,
              "getDeclaringClass (RETAIN_CLASS_REFERENCE)");
        String d = frame.getDescriptor();
        check(d != null && d.startsWith("(") && d.contains(")"), "descriptor looks like a method descriptor: " + d);
        check(frame.getMethodType() != null
              && frame.getMethodType().returnType() == StackWalker.StackFrame.class,
              "methodType return type matches firstFrame()");
        StackTraceElement ste = frame.toStackTraceElement();
        check("StackFrameTest".equals(ste.getClassName()) && "firstFrame".equals(ste.getMethodName()),
              "toStackTraceElement maps class/method names");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testFrame();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
