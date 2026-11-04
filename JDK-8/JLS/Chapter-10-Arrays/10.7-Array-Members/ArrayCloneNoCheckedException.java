/*
 * @test
 * @summary JLS 10.7 (Java SE 8): the array clone method overrides Object.clone,
 *          throws no checked exceptions, and returns T[] for an array of type T[].
 * @run main ArrayCloneNoCheckedException
 */

public class ArrayCloneNoCheckedException {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static String[] cloneStrings(String[] a) {
        return a.clone();
    }

    public static void main(String[] args) {
        String[] sa = { "a", "b" };
        String[] sb = cloneStrings(sa);
        check(sb != sa, "clone is distinct");
        check(sb instanceof String[], "clone return type is String[]");
        check(sb.length == 2 && "a".equals(sb[0]), "clone contents");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
