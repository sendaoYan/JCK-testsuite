/*
 * @test
 * @summary JLS 13.4.9/4.12.4 (Java SE 8): a static final field with a constant
 *          expression initializer is a constant variable, usable in switch case
 *          labels and inlined by clients.
 * @run main ConstantVariableTest
 */

public class ConstantVariableTest {
    static final int CONST = 7;
    static final int NOT_CONST = Integer.parseInt("7");

    static int classify(int x) {
        switch (x) {
            case CONST: return 1;   // CONST is a constant variable
            default: return 0;
        }
    }

    public static void main(String[] args) {
        if (CONST != 7) throw new AssertionError("constant value");
        if (classify(7) != 1 || classify(8) != 0) throw new AssertionError("constant case label");
        if (NOT_CONST != 7) throw new AssertionError("non-constant value");
        System.out.println("PASS");
    }
}
