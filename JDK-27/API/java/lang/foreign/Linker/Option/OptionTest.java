/*
 * @test
 * @summary java.lang.foreign.Linker.Option conformance: white-box structure + black-box behavior.
 * @run main OptionTest
 */
import java.lang.foreign.*;

public class OptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.foreign.Linker$Option");
        check(c.isInterface(), "interface");
        check(c.getMethod("firstVariadicArg", int.class).getReturnType() == Linker.Option.class,
              "firstVariadicArg returns Option");
        check(c.getMethod("captureCallState", String[].class).getReturnType() == Linker.Option.class,
              "captureCallState returns Option");
        check(c.getMethod("captureStateLayout").getReturnType() == StructLayout.class,
              "captureStateLayout returns StructLayout");
        check(c.getMethod("critical", boolean.class).getReturnType() == Linker.Option.class,
              "critical returns Option");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        Linker.Option vararg = Linker.Option.firstVariadicArg(0);
        check(vararg != null, "firstVariadicArg non-null");

        Linker.Option state = Linker.Option.captureCallState("errno");
        check(state != null, "captureCallState non-null");

        StructLayout sl = Linker.Option.captureStateLayout();
        check(sl != null, "captureStateLayout non-null");
        check(sl.byteSize() >= 0, "captureStateLayout has non-negative size");
        check(!sl.memberLayouts().isEmpty(), "captureStateLayout has members");

        Linker.Option cTrue = Linker.Option.critical(true);
        Linker.Option cFalse = Linker.Option.critical(false);
        check(cTrue != null && cFalse != null, "critical non-null");
        check(!cTrue.equals(cFalse), "critical(true) and critical(false) distinct");

        check(Linker.Option.firstVariadicArg(0).equals(Linker.Option.firstVariadicArg(0)),
              "firstVariadicArg(0) stable equals");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
