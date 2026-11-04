/*
 * @test
 * @summary JLS 6.5.6.2 (Java SE 8): a qualified expression name accesses a
 *          static field via a type name (including an enum constant), an
 *          instance field via an expression name, and array length.
 * @run main QualifiedExpressionName
 */

enum Color { RED, GREEN }

public class QualifiedExpressionName {
    static int passed = 0;
    static void check(boolean c, String w) { if (c) passed++; else throw new AssertionError(w); }

    static int counter = 7;
    int inst = 8;

    public static void main(String[] args) {
        QualifiedExpressionName t = new QualifiedExpressionName();

        check(QualifiedExpressionName.counter == 7, "static field via type name");
        check(t.inst == 8, "instance field via expression name");
        check(Color.RED.ordinal() == 0 && Color.GREEN.ordinal() == 1, "enum constants via type name");

        int[] arr = new int[3];
        check(arr.length == 3, "array length via expression name");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
