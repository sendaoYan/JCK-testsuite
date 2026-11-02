/*
 * @test
 * @summary java.lang.foreign.Linker.Option conformance: interface and option factories.
 * @run main OptionTest
 */
import java.lang.foreign.Linker;

public class OptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        check(Linker.Option.class.isInterface(), "is interface");
        check(Linker.Option.firstVariadicArg(2) != null, "firstVariadicArg");
        check(Linker.Option.captureCallState("errno") != null, "captureCallState");
        check(Linker.Option.captureStateLayout() != null, "captureStateLayout");
        check(Linker.Option.critical(true) != null, "critical");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
