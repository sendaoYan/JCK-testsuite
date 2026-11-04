/*
 * @test
 * @summary JLS 8.3.1.4: a field may be declared volatile; the modifier is
 *          reflected in the field's flags and a volatile field can be declared
 *          without final (final+volatile is a compile-time error elsewhere).
 * @run main VolatileFieldModifier
 */
import java.lang.reflect.Modifier;
public class VolatileFieldModifier {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public volatile int v = 0;
    public int n = 1;
    public static void main(String[] args) throws Exception {
        int vf = VolatileFieldModifier.class.getField("v").getModifiers();
        int nf = VolatileFieldModifier.class.getField("n").getModifiers();
        check(Modifier.isVolatile(vf), "volatile modifier present");
        check(!Modifier.isVolatile(nf), "non-volatile field lacks volatile modifier");
        VolatileFieldModifier o = new VolatileFieldModifier();
        o.v = 5;
        check(o.v == 5, "volatile field read/write");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
