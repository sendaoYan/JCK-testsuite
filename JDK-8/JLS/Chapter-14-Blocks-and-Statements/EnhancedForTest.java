/*
 * @test
 * @summary JLS 14.14.2 (Java SE 8): the enhanced for statement iterates over an
 *          array or Iterable in order.
 * @run main EnhancedForTest
 */

public class EnhancedForTest {
    public static void main(String[] args) {
        int[] a = {1, 2, 3, 4};
        int sum = 0;
        for (int v : a) sum += v;
        if (sum != 10) throw new AssertionError("enhanced for over array");

        java.util.List<String> l = java.util.Arrays.asList("x", "y", "z");
        StringBuilder sb = new StringBuilder();
        for (String s : l) sb.append(s);
        if (!sb.toString().equals("xyz")) throw new AssertionError("enhanced for over Iterable");

        System.out.println("PASS");
    }
}
