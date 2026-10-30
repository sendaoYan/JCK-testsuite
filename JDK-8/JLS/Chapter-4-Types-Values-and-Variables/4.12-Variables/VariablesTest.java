/*
 * @test
 * @summary JLS 4.12.3/4.12.5 (Java SE 8): fields receive default initial values;
 *          a final variable is assigned at most once and blank finals are
 *          assigned by constructors.
 * @run main VariablesTest
 */

public class VariablesTest {
    static int sf;
    static boolean sb;
    static Object so;
    static char sc;
    static float sfl;
    static double sdl;
    static long sl;

    final int instanceFinal = 7;
    final int blankFinal;

    VariablesTest() { blankFinal = 9; }

    public static void main(String[] args) {
        if (sf != 0 || sl != 0L) throw new AssertionError("integral default 0");
        if (sfl != 0.0f || sdl != 0.0d) throw new AssertionError("float default 0.0");
        if (sb != false) throw new AssertionError("boolean default false");
        if (so != null) throw new AssertionError("reference default null");
        if ((int) sc != 0) throw new AssertionError("char default U+0000");

        VariablesTest v = new VariablesTest();
        if (v.instanceFinal != 7 || v.blankFinal != 9) throw new AssertionError("final variables");

        System.out.println("PASS");
    }
}
