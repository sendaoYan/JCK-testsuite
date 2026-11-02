/*
 * @test
 * @summary java.lang.StackWalker.StackFrame conformance: interface and frame accessors.
 * @run main StackFrameTest
 */
import java.lang.StackWalker;

public class StackFrameTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        check(StackWalker.StackFrame.class.isInterface(), "is interface");
        StackWalker.StackFrame frame = StackWalker.getInstance().walk(s -> s.findFirst().get());
        check("StackFrameTest".equals(frame.getClassName()), "className");
        check("main".equals(frame.getMethodName()), "methodName");
        check(frame.getLineNumber() >= 0, "lineNumber");
        check(frame.getByteCodeIndex() >= 0, "byteCodeIndex");
        check(!frame.isNativeMethod(), "not native");
        check(frame.toStackTraceElement() != null, "toStackTraceElement");
        check(frame.getFileName() != null, "fileName non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
