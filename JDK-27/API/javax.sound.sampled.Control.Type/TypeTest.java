/*
 * @test
 * @summary javax.sound.sampled.Control.Type conformance: white-box structure + black-box behavior.
 * @run main TypeTest
 */

import java.lang.reflect.Modifier;
import javax.sound.sampled.Control;

public class TypeTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.sampled.Control$Type");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods) && Modifier.isStatic(mods), "public static");
        check(c.getEnclosingClass() == Control.class, "enclosing class");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        // the constructor is protected; subclass-only instantiation
        check(Modifier.isProtected(c.getDeclaredConstructors()[0].getModifiers()), "protected constructor");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Control.Type gain = new Control.Type("Gain") {};
        Control.Type balance = new Control.Type("Balance") {};

        check("Gain".equals(gain.toString()), "toString name");
        check("Balance".equals(balance.toString()), "toString name 2");

        // equality is identity (Object.equals), not name comparison
        Control.Type gain2 = new Control.Type("Gain") {};
        check(!gain.equals(gain2), "same-name instances not equal");
        check(gain.equals(gain), "reflexive");
        check(!gain.equals(balance), "different names not equal");
        check(!gain.equals(null), "not equal null");
        check(gain.hashCode() == gain.hashCode(), "hashCode stable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
