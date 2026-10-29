/*
 * @test
 * @summary JLS 4.12.5: every class variable, instance variable, or array
 *          component is initialized to a default value when created: zero for
 *          integral types, positive zero for float/double, '\u0000' for char,
 *          false for boolean, and null for all reference types.
 * @run main InitialValuesOfVariables
 */

class Defaults {
    static byte    b;
    static short   s;
    static int     i;
    static long    l;
    static float   f;
    static double  d;
    static char    c;
    static boolean z;
    static Object  o;
    static String  str;

    byte    ib;  short is;  int ii;  long il;  float iff;  double id;
    char    ic;  boolean iz; Object io;
}

public class InitialValuesOfVariables {
    public static void main(String[] args) {
        // Class variables default-initialized.
        if (Defaults.b != (byte) 0) throw new AssertionError("byte default");
        if (Defaults.s != (short) 0) throw new AssertionError("short default");
        if (Defaults.i != 0) throw new AssertionError("int default");
        if (Defaults.l != 0L) throw new AssertionError("long default");
        if (Defaults.f != 0.0f) throw new AssertionError("float default (positive zero)");
        // Positive zero specifically: 1.0f/positive zero is +infinity.
        if (1.0f / Defaults.f != Float.POSITIVE_INFINITY) {
            throw new AssertionError("float default must be positive zero");
        }
        if (Defaults.d != 0.0d) throw new AssertionError("double default (positive zero)");
        if (1.0 / Defaults.d != Double.POSITIVE_INFINITY) {
            throw new AssertionError("double default must be positive zero");
        }
        if (Defaults.c != '\u0000') throw new AssertionError("char default must be \\u0000");
        if (Defaults.z) throw new AssertionError("boolean default must be false");
        if (Defaults.o != null) throw new AssertionError("reference default must be null");
        if (Defaults.str != null) throw new AssertionError("String default must be null");

        // Instance variables default-initialized on object creation.
        Defaults dft = new Defaults();
        if (dft.ib != 0 || dft.ii != 0 || dft.il != 0L) throw new AssertionError("instance integral defaults");
        if (dft.iff != 0.0f || dft.id != 0.0d) throw new AssertionError("instance float defaults");
        if (dft.ic != '\u0000') throw new AssertionError("instance char default");
        if (dft.iz) throw new AssertionError("instance boolean default");
        if (dft.io != null) throw new AssertionError("instance reference default");

        // Array components default-initialized on array creation.
        int[] ints = new int[3];
        for (int v : ints) if (v != 0) throw new AssertionError("int array default");
        Object[] objs = new Object[3];
        for (Object v : objs) if (v != null) throw new AssertionError("Object array default");
        boolean[] bools = new boolean[3];
        for (boolean v : bools) if (v) throw new AssertionError("boolean array default");
        char[] chars = new char[3];
        for (char v : chars) if (v != '\u0000') throw new AssertionError("char array default");

        System.out.println("PASS");
    }
}
