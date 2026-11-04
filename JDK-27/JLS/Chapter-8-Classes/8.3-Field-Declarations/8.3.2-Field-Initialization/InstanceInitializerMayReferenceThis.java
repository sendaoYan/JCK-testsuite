/*
 * @test
 * @summary JLS 8.3.2: an instance variable initializer may refer to the current
 *          object using this.
 * @run main InstanceInitializerMayReferenceThis
 */
public class InstanceInitializerMayReferenceThis {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    int x = 1;
    int y = this.x;
    public static void main(String[] args) {
        check(new InstanceInitializerMayReferenceThis().y == 1, "instance initializer may reference this");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
