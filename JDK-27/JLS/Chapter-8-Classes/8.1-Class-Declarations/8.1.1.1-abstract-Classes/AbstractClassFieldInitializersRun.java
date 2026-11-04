/*
 * @test
 * @summary JLS 8.1.1.1: instantiating a concrete subclass of an abstract class
 *          runs the abstract class's constructor and its instance field
 *          initializers.
 * @run main AbstractClassFieldInitializersRun
 */
abstract class Base {
    int x = 10;
    int y = initY();
    int initY() { return 20; }
    abstract int z();
}
class Sub extends Base {
    int z() { return 30; }
}
public class AbstractClassFieldInitializersRun {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) {
        Sub s = new Sub();
        check(s.x == 10, "abstract superclass instance field initializer runs");
        check(s.y == 20, "abstract superclass instance field initializer via method runs");
        check(s.z() == 30, "abstract method implemented by subclass");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
