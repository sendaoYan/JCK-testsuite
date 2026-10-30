/*
 * @test
 * @summary JVMS 3.10: a switch with dense case labels compiles to tableswitch and
 *          a switch with sparse labels to lookupswitch; both dispatch correctly.
 * @run main CompilingSwitches
 */

public class CompilingSwitches {
    static int dense(int x) {
        switch (x) {
            case 0: return 100;
            case 1: return 101;
            case 2: return 102;
            case 3: return 103;
            default: return -1;
        }
    }

    static int sparse(int x) {
        switch (x) {
            case 1: return 1;
            case 1000: return 1000;
            case 100000: return 100000;
            default: return -1;
        }
    }

    public static void main(String[] args) {
        for (int i = 0; i <= 3; i++)
            if (dense(i) != 100 + i) throw new AssertionError("dense case " + i);
        if (dense(99) != -1) throw new AssertionError("dense default");

        if (sparse(1) != 1 || sparse(1000) != 1000 || sparse(100000) != 100000)
            throw new AssertionError("sparse cases");
        if (sparse(2) != -1) throw new AssertionError("sparse default");

        System.out.println("PASS");
    }
}
