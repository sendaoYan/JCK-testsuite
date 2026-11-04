/*
 * @test
 * @summary JVMS 3.9: the compiler emits newarray/anewarray/multianewarray for
 *          array creation and invokes the clone logic for array copies; a
 *          primitive array is defaulted to zero, a reference array to null, and
 *          an array keeps its component type at run time.
 * @run main ArrayCreationAndClone
 */

public class ArrayCreationAndClone {
    static int passed = 0;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        int[] p = new int[3];
        check(p.length == 3, "primitive array length");
        check(p[0] == 0 && p[2] == 0, "primitive array elements default to zero");

        String[] r = new String[2];
        check(r[0] == null && r[1] == null, "reference array elements default to null");

        int[][] multi = new int[2][3];
        check(multi.length == 2 && multi[0].length == 3 && multi[1].length == 3,
                "multianewarray creates the declared rectangular shape");

        Object[] oa = new String[1];
        check(oa.getClass() == String[].class,
                "array runtime type preserves the component type");

        int[] src = {1, 2, 3};
        int[] cp = src.clone();
        check(cp != src && cp.length == 3 && cp[0] == 1 && cp[2] == 3,
                "array clone produces a distinct, equal shallow copy");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
