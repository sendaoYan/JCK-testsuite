/*
 * @test
 * @summary JLS 12.5 (step 3, Java SE 27): a field assigned in the prologue of a
 *          constructor (before the explicit superclass constructor invocation) is
 *          initialized before the superclass constructor runs.
 * @run main PrologueFieldInit
 */

public class PrologueFieldInit {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static StringBuilder log = new StringBuilder();

    static class Super {
        Super() { printThree(); }
        void printThree() { log.append("super"); }
    }

    static class Sub extends Super {
        int three;
        Sub() {
            three = 3;   // prologue: before super()
            super();
        }
        void printThree() { log.append(three); }
    }

    public static void main(String[] args) {
        Sub s = new Sub();
        check(s.three == 3, "field initialized in prologue");
        check(log.toString().equals("3"), "superclass constructor observes prologue-initialized field: " + log);
        System.out.println("PASS (" + passed + " assertions)");
    }
}
