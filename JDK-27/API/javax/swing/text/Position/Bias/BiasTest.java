/*
 * @test
 * @summary javax.swing.text.Position.Bias conformance: white-box structure + black-box behavior.
 * @run main BiasTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import javax.swing.text.Position;

public class BiasTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.Position$Bias");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isFinal(mods), "final modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.Position$Bias");

        // No public constructor: instances only via the two constants.
        check(c.getConstructors().length == 0, "no public constructor");

        Field fwd = c.getField("Forward");
        Field bwd = c.getField("Backward");
        check(Modifier.isStatic(fwd.getModifiers()) && Modifier.isFinal(fwd.getModifiers()),
                "Forward static final");
        check(Modifier.isStatic(bwd.getModifiers()) && Modifier.isFinal(bwd.getModifiers()),
                "Backward static final");

        Object fv = fwd.get(null);
        Object bv = bwd.get(null);
        check(fv != null && bv != null, "both constants non-null");
        check(fv != bv, "Forward and Backward are distinct");
        check(fv == Position.Bias.Forward, "Forward field identity");
        check(bv == Position.Bias.Backward, "Backward field identity");

        check("Forward".equals(fv.toString()), "Forward toString");
        check("Backward".equals(bv.toString()), "Backward toString");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
