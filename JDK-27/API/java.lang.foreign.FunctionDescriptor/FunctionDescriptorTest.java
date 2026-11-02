/*
 * @test
 * @summary java.lang.foreign.FunctionDescriptor conformance: of/ofVoid/toMethodType and layout transforms.
 * @run main FunctionDescriptorTest
 */
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodType;

public class FunctionDescriptorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        check(FunctionDescriptor.class.isInterface(), "is interface");
        FunctionDescriptor fd = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT);
        check(fd.returnLayout().get() == ValueLayout.JAVA_INT, "returnLayout");
        check(fd.argumentLayouts().size() == 2, "two args");
        FunctionDescriptor vd = FunctionDescriptor.ofVoid(ValueLayout.JAVA_LONG);
        check(vd.returnLayout().isEmpty(), "ofVoid return empty");
        check(vd.argumentLayouts().size() == 1, "ofVoid one arg");
        MethodType mt = fd.toMethodType();
        check(mt.returnType() == int.class && mt.parameterCount() == 2, "toMethodType");
        check(fd.dropReturnLayout().returnLayout().isEmpty(), "dropReturnLayout");
        check(fd.changeReturnLayout(ValueLayout.JAVA_LONG).returnLayout().get() == ValueLayout.JAVA_LONG, "changeReturnLayout");
        check(fd.appendArgumentLayouts(ValueLayout.JAVA_FLOAT).argumentLayouts().size() == 3, "appendArgumentLayouts");
        check(fd.insertArgumentLayouts(0, ValueLayout.JAVA_FLOAT).argumentLayouts().size() == 3, "insertArgumentLayouts");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
