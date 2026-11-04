/*
 * @test
 * @summary JLS 8.1.3: an inner class instance is associated with an immediately
 *          enclosing instance, created by a qualified class instance creation
 *          expression (enclosingInstance.new Inner()).
 * @run main InnerClassQualifiedInstantiation
 */
class Outer {
    class Inner { int v = 7; }
}
public class InnerClassQualifiedInstantiation {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) {
        Outer o = new Outer();
        Outer.Inner i = o.new Inner();
        check(i != null, "qualified instantiation of inner class");
        check(i.v == 7, "inner instance field");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
