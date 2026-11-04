/*
 * @test
 * @summary JLS 15.12.2.5 (Java SE 8): overload resolution chooses the most
 *          specific applicable method: an exact match is preferred over
 *          widening, a fixed-arity method is preferred over a variable-arity
 *          method, and a boxed parameter is chosen only when no primitive
 *          overload applies.
 * @run main MostSpecificMethod
 */

public class MostSpecificMethod {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static String m(int x)      { return "int"; }
    static String m(long x)     { return "long"; }
    static String m(Integer x)  { return "Integer"; }
    static String m(Object x)   { return "Object"; }
    static String m(int x, int y)      { return "int,int"; }
    static String m(long x, long y)    { return "long,long"; }
    static String m(double x, double y){ return "double,double"; }
    static String m(int... xs)         { return "int..."; }

    static String mo(Object... xs)     { return "Object..."; }

    public static void main(String[] args) {
        check(m(1).equals("int"), "exact int wins over long/Integer/Object");
        check(m(1L).equals("long"), "long wins over Object");
        check(m(Integer.valueOf(1)).equals("Integer"), "boxed Integer wins over Object");
        check(m("s").equals("Object"), "String widens to Object");
        check(m((byte) 1).equals("int"), "byte widens to int (more specific than long)");
        check(m((short) 1).equals("int"), "short widens to int");
        check(m((char) 1).equals("int"), "char widens to int");
        check(m(1.0f).equals("Object"), "float boxes to Float then widens to Object");

        check(m(1, 1).equals("int,int"), "fixed int,int preferred over varargs");
        check(m(1L, 1L).equals("long,long"), "fixed long,long");
        check(m(1.0, 1.0).equals("double,double"), "fixed double,double");
        check(m(1, 2, 3).equals("int..."), "varargs int...");

        check(mo("a", "b").equals("Object..."), "varargs Object... with reference args");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
