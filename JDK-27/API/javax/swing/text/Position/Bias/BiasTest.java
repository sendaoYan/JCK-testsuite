/*
 * @test
 * @summary javax.swing.text.Position.Bias conformance: forward/backward constants + toString (headless-safe).
 * @run main BiasTest
 */

import java.lang.reflect.Modifier;
import javax.swing.text.Position;

public class BiasTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Position.Bias.class;
        check(!c.isInterface(), "is class");
        check(Modifier.isFinal(c.getModifiers()), "is final");

        check(Position.Bias.Forward != null, "Forward non-null");
        check(Position.Bias.Backward != null, "Backward non-null");
        check(Position.Bias.Forward != Position.Bias.Backward, "Forward != Backward");
        check("Forward".equals(Position.Bias.Forward.toString()), "Forward toString");
        check("Backward".equals(Position.Bias.Backward.toString()), "Backward toString");
        // The singleton constants are stable across repeated references.
        check(Position.Bias.Forward == Position.Bias.Forward, "Forward singleton");
        check(Position.Bias.Backward == Position.Bias.Backward, "Backward singleton");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
