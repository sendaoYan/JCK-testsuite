/*
 * @test
 * @summary JLS 8.4.8.4 (Java SE 8): an abstract method declared in a superclass trumps a
 *          default method of a superinterface with the same signature, so the
 *          class may inherit both without conflict and implement the abstract.
 * @run main InheritAbstractFromClassAndDefaultFromInterface
 */
interface I { default String m() { return "default"; } }
abstract class A { abstract String m(); }
public class InheritAbstractFromClassAndDefaultFromInterface extends A implements I {
    public String m() { return "impl"; }
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) {
        I i = new InheritAbstractFromClassAndDefaultFromInterface();
        check(i.m().equals("impl"), "abstract class method trumps default; class implements");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
