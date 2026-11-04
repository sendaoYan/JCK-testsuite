/*
 * @test
 * @summary JLS 8.6 (Java SE 8): an instance initializer runs in an instance context,
 *          so it may use the keyword this and refer to instance members.
 * @run main InstanceInitializerThis
 */

public class InstanceInitializerThis {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    int x;
    {
        this.x = 42;   // 'this' is permitted in an instance initializer
    }

    public static void main(String[] args) {
        InstanceInitializerThis o = new InstanceInitializerThis();
        check(o.x == 42, "instance initializer assigned through this");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
