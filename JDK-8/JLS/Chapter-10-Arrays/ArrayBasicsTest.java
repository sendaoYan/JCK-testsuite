/*
 * @test
 * @summary JLS 10.1-10.4 (Java SE 8): arrays have a length, index from zero, get
 *          default initial values, may be multi-dimensional, and throw
 *          ArrayIndexOutOfBoundsException when indexed out of bounds.
 * @run main ArrayBasicsTest
 */

public class ArrayBasicsTest {
    public static void main(String[] args) {
        int[] a = new int[5];
        if (a.length != 5) throw new AssertionError("length");
        if (a[0] != 0 || a[4] != 0) throw new AssertionError("default int 0");
        a[2] = 9;
        if (a[2] != 9) throw new AssertionError("element store");

        boolean[] bs = new boolean[2];
        if (bs[0] != false) throw new AssertionError("default boolean false");
        String[] ss = new String[2];
        if (ss[0] != null) throw new AssertionError("default reference null");

        int[][] m = new int[3][4];
        if (m.length != 3 || m[0].length != 4) throw new AssertionError("multi-dim");
        m[1][2] = 7;
        if (m[1][2] != 7) throw new AssertionError("multi-dim store");

        int[][] jag = new int[3][];
        jag[0] = new int[1];
        jag[1] = new int[2];
        if (jag[0].length != 1 || jag[1].length != 2 || jag[2] != null) throw new AssertionError("jagged");

        boolean caught = false;
        try {
            int z = a[5];
        } catch (ArrayIndexOutOfBoundsException expected) {
            caught = true;
        }
        if (!caught) throw new AssertionError("ArrayIndexOutOfBoundsException expected");

        System.out.println("PASS");
    }
}
