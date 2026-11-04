/*
 * @test
 * @summary java.lang.Compiler conformance: static native method surface (JDK-8 only).
 * @run main CompilerTest
 */

public class CompilerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testCompiler() {
        Compiler.enable();
        Compiler.disable();
        check(true, "enable/disable callable");
        check(Compiler.compileClass(String.class) == false, "compileClass returns false (no JIT)");
        check(Compiler.compileClasses("java.lang.String") == false,
              "compileClasses returns false (no JIT)");
        check(Compiler.command("x") == null, "command returns null (no JIT)");
    }

    public static void main(String[] args) {
        testCompiler();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
