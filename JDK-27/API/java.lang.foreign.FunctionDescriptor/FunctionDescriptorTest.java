/*
 * @test
 * @summary java.lang.foreign.FunctionDescriptor conformance: white-box structure + black-box behavior.
 * @run main FunctionDescriptorTest
 */
import java.lang.foreign.*;
import java.lang.invoke.MethodType;
import java.util.List;
import java.util.Optional;

public class FunctionDescriptorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.foreign.FunctionDescriptor");
        check(c.isInterface(), "interface");
        check(c.getMethod("returnLayout").getReturnType() == Optional.class, "returnLayout returns Optional");
        check(c.getMethod("argumentLayouts").getReturnType() == List.class, "argumentLayouts returns List");
        check(c.getMethod("appendArgumentLayouts", MemoryLayout[].class).getReturnType() == FunctionDescriptor.class,
              "appendArgumentLayouts returns FunctionDescriptor");
        check(c.getMethod("insertArgumentLayouts", int.class, MemoryLayout[].class).getReturnType() == FunctionDescriptor.class,
              "insertArgumentLayouts returns FunctionDescriptor");
        check(c.getMethod("changeReturnLayout", MemoryLayout.class).getReturnType() == FunctionDescriptor.class,
              "changeReturnLayout returns FunctionDescriptor");
        check(c.getMethod("dropReturnLayout").getReturnType() == FunctionDescriptor.class,
              "dropReturnLayout returns FunctionDescriptor");
        check(c.getMethod("toMethodType").getReturnType() == MethodType.class, "toMethodType returns MethodType");
        check(c.getMethod("of", MemoryLayout.class, MemoryLayout[].class) != null, "static of exists");
        check(c.getMethod("ofVoid", MemoryLayout[].class) != null, "static ofVoid exists");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        FunctionDescriptor fd = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG);
        check(fd.returnLayout().isPresent() && fd.returnLayout().get() == ValueLayout.JAVA_INT,
              "of(ret, args) sets return layout");
        check(fd.argumentLayouts().size() == 1 && fd.argumentLayouts().get(0) == ValueLayout.JAVA_LONG,
              "of(ret, args) sets argument layouts");
        check(fd.toMethodType().equals(MethodType.methodType(int.class, long.class)),
              "toMethodType maps carriers (long)int");

        FunctionDescriptor fd2 = fd.appendArgumentLayouts(ValueLayout.JAVA_SHORT);
        check(fd2.argumentLayouts().size() == 2, "appendArgumentLayouts appends");
        check(fd2.toMethodType().equals(MethodType.methodType(int.class, long.class, short.class)),
              "append toMethodType");

        FunctionDescriptor fd3 = fd.insertArgumentLayouts(0, ValueLayout.JAVA_CHAR);
        check(fd3.argumentLayouts().size() == 2, "insertArgumentLayouts inserts");
        check(fd3.argumentLayouts().get(0) == ValueLayout.JAVA_CHAR, "insert at head");
        check(fd3.toMethodType().equals(MethodType.methodType(int.class, char.class, long.class)),
              "insert toMethodType");

        FunctionDescriptor changed = fd.changeReturnLayout(ValueLayout.JAVA_SHORT);
        check(changed.returnLayout().get() == ValueLayout.JAVA_SHORT, "changeReturnLayout");
        check(changed.toMethodType().equals(MethodType.methodType(short.class, long.class)),
              "changeReturnLayout toMethodType");

        FunctionDescriptor dropped = changed.dropReturnLayout();
        check(dropped.returnLayout().isEmpty(), "dropReturnLayout empties return");
        check(dropped.toMethodType().equals(MethodType.methodType(void.class, long.class)),
              "dropReturnLayout toMethodType void");

        FunctionDescriptor fdv = FunctionDescriptor.ofVoid(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG);
        check(fdv.returnLayout().isEmpty(), "ofVoid has no return layout");
        check(fdv.argumentLayouts().size() == 2, "ofVoid sets args");
        check(fdv.toMethodType().equals(MethodType.methodType(void.class, int.class, long.class)),
              "ofVoid toMethodType");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
