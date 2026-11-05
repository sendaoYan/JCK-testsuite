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

    // ---- Part 3: deep contract (immutability, value semantics, null matrix) ----
    static void testDeepContract() {
        FunctionDescriptor base = FunctionDescriptor.of(ValueLayout.JAVA_INT,
                ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE);

        // every mutator returns a new descriptor; the original is untouched
        FunctionDescriptor appended = base.appendArgumentLayouts(ValueLayout.JAVA_BYTE);
        check(base.argumentLayouts().size() == 2, "appendArgumentLayouts is pure");
        check(appended.argumentLayouts().size() == 3
                      && appended.argumentLayouts().get(2) == ValueLayout.JAVA_BYTE,
              "appended layout lands at the tail");
        FunctionDescriptor inserted = base.insertArgumentLayouts(1, ValueLayout.JAVA_SHORT);
        check(inserted.argumentLayouts().get(1) == ValueLayout.JAVA_SHORT
                      && inserted.argumentLayouts().get(2) == ValueLayout.JAVA_DOUBLE,
              "insertArgumentLayouts(1, SHORT) shifts the tail");
        FunctionDescriptor changed = base.changeReturnLayout(ValueLayout.ADDRESS);
        check(changed.returnLayout().get() == ValueLayout.ADDRESS
                      && base.returnLayout().get() == ValueLayout.JAVA_INT,
              "changeReturnLayout is pure");
        check(changed.argumentLayouts().equals(base.argumentLayouts()),
              "changeReturnLayout keeps arguments identical");
        FunctionDescriptor dropped = base.dropReturnLayout();
        check(dropped.returnLayout().isEmpty() && dropped.argumentLayouts().size() == 2,
              "dropReturnLayout keeps arguments");

        // insertArgumentLayouts bounds: negative or too-large index is rejected
        for (int bad : new int[]{-1, 3, 100}) {
            try {
                base.insertArgumentLayouts(bad, ValueLayout.JAVA_BYTE);
                check(false, "insertArgumentLayouts(" + bad + ") must be rejected");
            } catch (IllegalArgumentException expected) {
                check(true, "insertArgumentLayouts(" + bad + ") -> IAE");
            }
        }
        // null return layout is rejected
        try {
            base.changeReturnLayout(null);
            check(false, "changeReturnLayout(null) rejected");
        } catch (NullPointerException expected) {
            check(true, "changeReturnLayout(null) -> NPE");
        }
        try {
            FunctionDescriptor.of(null, ValueLayout.JAVA_INT);
            check(false, "of(null return) rejected");
        } catch (NullPointerException expected) {
            check(true, "of(null, ...) -> NPE");
        }

        // ofVoid with no arguments: ()void
        FunctionDescriptor noArgs = FunctionDescriptor.ofVoid();
        check(noArgs.returnLayout().isEmpty() && noArgs.argumentLayouts().isEmpty(),
              "ofVoid() has neither return nor arguments");
        check(noArgs.toMethodType() == MethodType.methodType(void.class),
              "ofVoid() toMethodType is ()void");

        // value semantics: same construction equals, hashCode agrees
        FunctionDescriptor again = FunctionDescriptor.of(ValueLayout.JAVA_INT,
                ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE);
        check(base.equals(again) && base.hashCode() == again.hashCode(),
              "value-equal descriptors");
        check(!base.equals(appended), "different shapes not equals");
        check(!base.equals(null) && !base.equals("fd"), "!equals unrelated");
        // ofVoid vs of-void-return are both void-returning but differ in shape
        check(FunctionDescriptor.ofVoid(ValueLayout.JAVA_INT)
                      .equals(FunctionDescriptor.ofVoid(ValueLayout.JAVA_INT)),
              "ofVoid value equality");
        // ADDRESS argument maps to MemorySegment.class in the method type
        check(FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS)
                      .toMethodType().parameterType(0) == MemorySegment.class,
              "ADDRESS argument maps to MemorySegment.class");
        // argumentLayouts is an unmodifiable view (measured)
        try {
            base.argumentLayouts().add(ValueLayout.JAVA_BYTE);
            check(false, "argumentLayouts must be unmodifiable");
        } catch (UnsupportedOperationException expected) {
            check(true, "argumentLayouts.add -> UnsupportedOperationException");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
