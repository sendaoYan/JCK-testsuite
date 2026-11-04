/*
 * @test
 * @summary JLS 8.4.8.3: an overriding method may not throw more checked
 *          exceptions than the overridden method, but may throw fewer.
 * @run main OverrideDoesNotThrowMoreChecked
 */
import java.io.IOException;
class Base { void m() throws IOException { } }
public class OverrideDoesNotThrowMoreChecked extends Base {
    @Override void m() { }
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) {
        new OverrideDoesNotThrowMoreChecked().m();
        check(true, "override may throw fewer checked exceptions");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
