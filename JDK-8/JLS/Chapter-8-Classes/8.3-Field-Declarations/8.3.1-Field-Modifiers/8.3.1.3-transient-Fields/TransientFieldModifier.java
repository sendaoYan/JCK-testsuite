/*
 * @test
 * @summary JLS 8.3.1.3 (Java SE 8): a transient field is not part of the persistent state of
 *          an object; the transient modifier is reflected in the field's flags
 *          and the field behaves normally in memory.
 * @run main TransientFieldModifier
 */
import java.lang.reflect.Modifier;
public class TransientFieldModifier {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public transient int t = 1;
    public int n = 2;
    public static void main(String[] args) throws Exception {
        int tf = TransientFieldModifier.class.getField("t").getModifiers();
        int nf = TransientFieldModifier.class.getField("n").getModifiers();
        check(Modifier.isTransient(tf), "transient modifier present");
        check(!Modifier.isTransient(nf), "non-transient field lacks transient modifier");
        TransientFieldModifier o = new TransientFieldModifier();
        check(o.t == 1, "transient field holds value in memory");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
