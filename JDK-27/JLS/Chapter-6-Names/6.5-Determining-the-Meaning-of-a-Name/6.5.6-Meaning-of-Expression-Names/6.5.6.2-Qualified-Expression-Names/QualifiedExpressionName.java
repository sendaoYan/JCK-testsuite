/*
 * @test
 * @summary JLS 6.5.6.2: qualified expression names access static fields via a
 *          type name (including enum constants), instance fields via an
 *          expression name, and array length.
 * @run main QualifiedExpressionName
 */

enum Color { RED, GREEN }

public class QualifiedExpressionName {
    static int counter = 7;
    public static void main(String[] args) {
        if (QualifiedExpressionName.counter != 7) throw new AssertionError("static field");

        if (Color.RED.ordinal() != 0) throw new AssertionError("enum constant");
        if (Color.GREEN.ordinal() != 1) throw new AssertionError("enum constant");

        int[] arr = new int[3];
        if (arr.length != 3) throw new AssertionError("array length");

        System.out.println("PASS");
    }
}
