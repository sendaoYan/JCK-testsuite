/*
 * @test
 * @summary JLS 8.4.3.1 (Java SE 8): an instance method that is not abstract can be
 *          overridden by an abstract method, forcing subclasses to re-implement
 *          it.
 * @run main ConcreteOverriddenByAbstract
 */
abstract class Base { String s() { return "base"; } }
abstract class AbstractSub extends Base {
    public abstract String s();
}
class Concrete extends AbstractSub {
    public String s() { return "concrete"; }
}
public class ConcreteOverriddenByAbstract {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) {
        Base b = new Concrete();
        check(b.s().equals("concrete"), "abstract method overriding concrete is implemented by subclass");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
