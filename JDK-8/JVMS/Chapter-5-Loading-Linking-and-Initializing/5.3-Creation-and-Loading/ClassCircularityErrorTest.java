/*
 * @test
 * @summary JVMS 5.3: a class whose superclass chain is circular (a class that
 *          is its own superclass) is rejected with ClassCircularityError when
 *          it is loaded.
 * @compile ClassCircularityErrorSelf.jasm
 * @run main ClassCircularityErrorTest
 */

public class ClassCircularityErrorTest {
    static int passed = 0;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        boolean threw = false;
        try {
            Class.forName("ClassCircularityErrorSelf");
        } catch (ClassCircularityError e) {
            threw = true;
        }
        check(threw, "circular superclass rejected with ClassCircularityError");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
