/*
 * @test
 * @summary JLS 15.13.1 (Java SE 8): when a method reference names an overloaded
 *          method, the applicable overload is selected using the target
 *          functional interface's function type.
 * @run main MethodReferenceOverloadResolution
 */

import java.util.function.Function;
import java.util.function.IntFunction;

public class MethodReferenceOverloadResolution {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static String m(int x)     { return "int"; }
    static String m(long x)    { return "long"; }
    static String m(Integer x) { return "Integer"; }

    public static void main(String[] args) {
        Function<Integer, String> f = MethodReferenceOverloadResolution::m;
        check(f.apply(1).equals("Integer"), "Function<Integer,String> selects the Integer overload");
        IntFunction<String> g = MethodReferenceOverloadResolution::m;
        check(g.apply(1).equals("int"), "IntFunction<String> selects the int overload");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
