/*
 * @test
 * @summary JVMS 2.5.3/2.5.4: objects and arrays live on the heap (multiple
 *          references may denote the same object), while the method area holds
 *          per-class data so a static field is shared by every instance.
 * @run main HeapAndMethodArea
 */

public class HeapAndMethodArea {
    static int passed = 0;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Box {
        static int shared = 0;
        int value;
        Box(int v) { value = v; }
    }

    public static void main(String[] args) {
        // Heap: two references can denote the same object/array.
        int[] a = new int[3];
        int[] b = a;
        a[0] = 42;
        check(b[0] == 42, "array is shared through two references");

        Object o1 = new Object();
        Object o2 = o1;
        check(o1 == o2, "object identity is shared on the heap");

        // Method area: a static field is per-class, not per-instance.
        Box x = new Box(1);
        Box y = new Box(2);
        check(Box.shared == 0, "static field starts at its default value");
        x.shared = 9;
        check(y.shared == 9, "static field is shared across instances");
        check(x.value == 1 && y.value == 2, "instance fields are per-instance");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
