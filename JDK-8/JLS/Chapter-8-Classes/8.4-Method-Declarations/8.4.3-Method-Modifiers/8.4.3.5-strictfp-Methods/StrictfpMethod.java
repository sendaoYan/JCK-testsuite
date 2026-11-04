/*
 * @test
 * @summary JLS 8.4.3.5 (Java SE 8): a method may be declared strictfp; the
 *          modifier is reflected in the method's access flags.
 * @run main StrictfpMethod
 */
import java.lang.reflect.Modifier;
public class StrictfpMethod {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    strictfp double f() { return 0.1 + 0.2; }
    double g() { return 0.1 + 0.2; }
    public static void main(String[] args) throws Exception {
        check(Modifier.isStrict(StrictfpMethod.class.getDeclaredMethod("f").getModifiers()), "strictfp method modifier reflected");
        check(!Modifier.isStrict(StrictfpMethod.class.getDeclaredMethod("g").getModifiers()), "non-strictfp method lacks modifier");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
