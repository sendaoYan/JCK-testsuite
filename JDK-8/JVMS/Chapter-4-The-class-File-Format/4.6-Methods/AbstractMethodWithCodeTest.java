/*
 * @test
 * @summary JVMS 4.x: JVMS 4.6: an abstract or native method must not have a Code attribute; one that does is rejected with ClassFormatError
 * @compile AbstractMethodWithCode.jasm
 * @run main AbstractMethodWithCodeTest
 */

public class AbstractMethodWithCodeTest {
    static int passed;
    static void check(boolean c, String m) { if (c) passed++; else throw new AssertionError(m); }

    public static void main(String[] args) throws Exception {
        try {
            Class.forName("AbstractMethodWithCode");
            check(false, "AbstractMethodWithCode not rejected");
        } catch (ClassFormatError e) {
            check(true, "AbstractMethodWithCode rejected with ClassFormatError: " + e.getMessage());
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
