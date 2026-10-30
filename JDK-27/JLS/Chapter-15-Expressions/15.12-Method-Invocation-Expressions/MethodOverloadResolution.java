/*
 * @test
 * @summary JLS 15.12.2: overload resolution picks the most specific applicable
 *          method (exact match preferred, then widening/boxing, then varargs).
 * @run main MethodOverloadResolution
 */

public class MethodOverloadResolution {
    static String m(int x) { return "int"; }
    static String m(long x) { return "long"; }
    static String m(Integer x) { return "Integer"; }
    static String m(Object x) { return "Object"; }
    static String m(int... x) { return "varargs"; }

    public static void main(String[] args) {
        if (!"int".equals(m(1))) throw new AssertionError("int");
        if (!"long".equals(m(1L))) throw new AssertionError("long");
        if (!"Integer".equals(m(Integer.valueOf(1)))) throw new AssertionError("Integer");
        if (!"Object".equals(m("s"))) throw new AssertionError("Object");
        if (!"varargs".equals(m(1, 2))) throw new AssertionError("varargs");

        System.out.println("PASS");
    }
}
