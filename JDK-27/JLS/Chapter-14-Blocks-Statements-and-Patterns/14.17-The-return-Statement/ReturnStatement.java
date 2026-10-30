/*
 * @test
 * @summary JLS 14.17: a return statement transfers control to the invoker; the
 *          value of the expression is computed before any finally block runs,
 *          and an abrupt completion of finally discards the pending return
 *          value.
 * @run main ReturnStatement
 */

public class ReturnStatement {
    static int f() {
        try {
            return 1;
        } finally {
            // completes normally -> the value 1 is preserved
        }
    }

    static int g() {
        try {
            return 1;
        } finally {
            return 2;   // abrupt completion of finally discards the value 1
        }
    }

    static String valueComputedBeforeFinally() {
        StringBuilder sb = new StringBuilder();
        try {
            sb.append("a");
            return sb.toString();   // value "a" computed now
        } finally {
            sb.append("b");
        }
    }

    public static void main(String[] args) {
        if (f() != 1) throw new AssertionError("f() == " + f());
        if (g() != 2) throw new AssertionError("g() == " + g());
        if (!"a".equals(valueComputedBeforeFinally()))
            throw new AssertionError("return value not computed before finally");
        System.out.println("PASS");
    }
}
