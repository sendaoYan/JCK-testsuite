/*
 * @test
 * @summary JVMS 4.3: a method descriptor encodes the parameter types and return
 *          type of a method; reflection exposes the same types the descriptor
 *          encodes.
 * @run main Descriptors
 */

public class Descriptors {
    public static String f(int a, long b, Object c) { return "x"; }
    public int g(double d, String[] s) { return 0; }

    public static void main(String[] args) throws Exception {
        java.lang.reflect.Method m =
            Descriptors.class.getMethod("f", int.class, long.class, Object.class);
        if (m.getReturnType() != String.class) throw new AssertionError("return type");
        Class<?>[] params = m.getParameterTypes();
        if (params.length != 3
            || params[0] != int.class
            || params[1] != long.class
            || params[2] != Object.class)
            throw new AssertionError("parameter types");

        // array-typed parameter has descriptor component [Ljava/lang/String;
        java.lang.reflect.Method g =
            Descriptors.class.getMethod("g", double.class, String[].class);
        if (g.getParameterTypes()[1] != String[].class) throw new AssertionError("array param");

        System.out.println("PASS");
    }
}
