/*
 * @test
 * @summary JLS 4.12.5 (Java SE 8): every class variable, instance variable, or
 *          array component is initialized to a default value when created: zero
 *          for integral types, positive zero for float/double, '\u0000' for
 *          char, false for boolean, and null for reference types.
 * @run main InitialValuesOfVariables
 */

class Defaults {
    static byte b;
    static short s;
    static int i;
    static long l;
    static float f;
    static double d;
    static char c;
    static boolean z;
    static Object o;
    static String str;

    byte ib; short is; int ii; long il; float iff; double id;
    char ic; boolean iz; Object io;
}

public class InitialValuesOfVariables {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check(Defaults.b == (byte) 0, "byte default is zero");
        check(Defaults.s == (short) 0, "short default is zero");
        check(Defaults.i == 0, "int default is zero");
        check(Defaults.l == 0L, "long default is zero");
        check(Defaults.f == 0.0f, "float default is positive zero");
        check(1.0f / Defaults.f == Float.POSITIVE_INFINITY, "float default is specifically positive zero");
        check(Defaults.d == 0.0d, "double default is positive zero");
        check(1.0 / Defaults.d == Double.POSITIVE_INFINITY, "double default is specifically positive zero");
        check(Defaults.c == '\u0000', "char default is \\u0000");
        check(!Defaults.z, "boolean default is false");
        check(Defaults.o == null, "reference default is null");
        check(Defaults.str == null, "String default is null");

        Defaults dft = new Defaults();
        check(dft.ib == 0 && dft.ii == 0 && dft.il == 0L, "instance integral defaults");
        check(dft.iff == 0.0f && dft.id == 0.0d, "instance float defaults");
        check(dft.ic == '\u0000', "instance char default");
        check(!dft.iz, "instance boolean default");
        check(dft.io == null, "instance reference default");

        int[] ints = new int[3];
        for (int v : ints) check(v == 0, "int array component default");
        Object[] objs = new Object[3];
        for (Object v : objs) check(v == null, "Object array component default");
        boolean[] bools = new boolean[3];
        for (boolean v : bools) check(!v, "boolean array component default");
        char[] chars = new char[3];
        for (char v : chars) check(v == '\u0000', "char array component default");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
