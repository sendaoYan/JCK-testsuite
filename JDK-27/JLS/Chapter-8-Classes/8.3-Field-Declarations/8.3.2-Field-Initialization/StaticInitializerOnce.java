/*
 * @test
 * @summary JLS 8.3.2: a class variable initializer is evaluated exactly once,
 *          when the class is initialized.
 * @run main StaticInitializerOnce
 */

class Counter {
    static int n = 0;
    static int once = ++n;
}

public class StaticInitializerOnce {
    public static void main(String[] args) {
        int a = Counter.once;
        int b = Counter.once;
        if (a != 1 || b != 1) throw new AssertionError("class variable init once");
        if (Counter.n != 1) throw new AssertionError("init side effect once");
        System.out.println("PASS");
    }
}
