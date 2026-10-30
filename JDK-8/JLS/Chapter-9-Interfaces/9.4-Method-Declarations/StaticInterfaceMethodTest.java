/*
 * @test
 * @summary JLS 9.4 (Java SE 8): an interface may declare static methods, which are
 *          invoked on the interface type.
 * @run main StaticInterfaceMethodTest
 */

public class StaticInterfaceMethodTest {
    interface Util {
        static int twice(int x) { return 2 * x; }
    }
    static class C implements Util {}

    public static void main(String[] args) {
        if (Util.twice(21) != 42) throw new AssertionError("interface static method");
        System.out.println("PASS");
    }
}
