/*
 * @test
 * @summary JLS 15.7.5: evaluation order for array access and field access.
 *          In an array access the array reference is evaluated before the index;
 *          in a field access the primary is evaluated before the field is
 *          resolved.
 * @run main ArrayIndexFieldAccessOrder
 */

public class ArrayIndexFieldAccessOrder {
    static int passed;
    static StringBuilder log = new StringBuilder();

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static int[] array(String tag, int[] a) { log.append(tag); return a; }
    static int index(String tag, int v)    { log.append(tag); return v; }
    static Box box(String tag, Box b)       { log.append(tag); return b; }

    static class Box { int x; Box(int x) { this.x = x; } }

    public static void main(String[] args) {
        int[] a = {10, 20, 30};
        log.setLength(0);
        check(array("R", a)[index("I", 1)] == 20, "array access value");
        check(log.toString().equals("RI"), "array reference before index: " + log);

        Box b = new Box(42);
        log.setLength(0);
        check(box("P", b).x == 42, "field access value");
        check(log.toString().equals("P"), "primary evaluated before field access: " + log);

        System.out.println("PASS (" + passed + " assertions)");
    }
}
