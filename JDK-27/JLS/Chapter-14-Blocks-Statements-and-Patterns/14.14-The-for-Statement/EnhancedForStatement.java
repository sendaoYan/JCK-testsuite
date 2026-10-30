/*
 * @test
 * @summary JLS 14.14.2: an enhanced for statement iterates over an array or an
 *          Iterable, supports var for the element type, and may declare an
 *          unnamed variable.
 * @run main EnhancedForStatement
 */

import java.util.List;

public class EnhancedForStatement {
    public static void main(String[] args) {
        int sum = 0;
        for (int i : new int[]{1, 2, 3}) sum += i;
        if (sum != 6) throw new AssertionError("array: " + sum);

        StringBuilder sb = new StringBuilder();
        for (String s : List.of("a", "b")) sb.append(s);
        if (!"ab".equals(sb.toString())) throw new AssertionError("Iterable: " + sb);

        int total = 0;
        for (var v : List.of(1, 2)) total += v;
        if (total != 3) throw new AssertionError("var element: " + total);

        int n = 0;
        for (int _ : new int[]{1, 2, 3}) n++;   // unnamed variable
        if (n != 3) throw new AssertionError("unnamed variable: " + n);

        System.out.println("PASS");
    }
}
