/*
 * @test
 * @summary java.lang.invoke.StringConcatFactory conformance: makeConcatWithConstants.
 * @run main StringConcatFactoryTest
 */
import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.StringConcatFactory;

public class StringConcatFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        CallSite cs = StringConcatFactory.makeConcatWithConstants(lookup, "concat", MethodType.methodType(String.class, int.class), "\u0001");
        check(cs != null, "makeConcatWithConstants non-null");
        check("42".equals((String) cs.dynamicInvoker().invokeExact(42)), "single arg concat");
        CallSite cs2 = StringConcatFactory.makeConcatWithConstants(lookup, "concat", MethodType.methodType(String.class, String.class, int.class), "\u0001-\u0001");
        check("a-7".equals((String) cs2.dynamicInvoker().invoke("a", 7)), "two arg concat");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
