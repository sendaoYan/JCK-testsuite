/*
 * @test
 * @summary javax.sound.sampled.Control conformance: white-box structure + black-box behavior.
 * @run main ControlTest
 */

import java.lang.reflect.Modifier;
import javax.sound.sampled.Control;

public class ControlTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.sampled.Control");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(Modifier.isAbstract(mods), "abstract modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getDeclaredConstructors().length == 1, "constructor count 1");

        Class<?> t = Class.forName("javax.sound.sampled.Control$Type");
        check(t != null && t.getEnclosingClass() == c, "Type nested");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Control.Type type = new Control.Type("Gain") {};
        Control control = new Control(type) {};

        check(control.getType() == type, "getType identity");
        check("Gain".equals(type.toString()), "Type toString name");
        check(control.toString().contains("Gain"), "Control.toString has type name");

        // Control.Type equality is identity, not name-based
        Control.Type a = new Control.Type("Gain") {};
        Control.Type b = new Control.Type("Gain") {};
        check(a != b, "two instances distinct");
        check(!a.equals(b), "identity equals");
        check(a.equals(a), "reflexive equals");
        check(!a.equals(null), "not equal null");
        check(a.hashCode() == a.hashCode(), "hashCode stable");

        Control c2 = new Control(new Control.Type("Pan") {}) {};
        check(!"Pan".equals(control.getType().toString()), "different type name");
        check("Pan".equals(c2.getType().toString()), "second type name");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
