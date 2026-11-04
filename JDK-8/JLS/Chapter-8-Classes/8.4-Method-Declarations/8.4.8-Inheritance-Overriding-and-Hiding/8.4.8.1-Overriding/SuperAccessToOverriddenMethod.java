/*
 * @test
 * @summary JLS 8.4.8.1 (Java SE 8): an overridden method can be accessed via a method
 *          invocation that uses the keyword super.
 * @run main SuperAccessToOverriddenMethod
 */
class Point { String m() { return "Point"; } }
public class SuperAccessToOverriddenMethod extends Point {
    @Override String m() { return "Sub+" + super.m(); }
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) {
        check(new SuperAccessToOverriddenMethod().m().equals("Sub+Point"), "super accesses overridden method");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
