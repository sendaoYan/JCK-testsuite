/*
 * @test
 * @summary javax.sound.sampled.CompoundControl.Type conformance: white-box structure + black-box behavior.
 * @run main TypeTest
 */

import java.lang.reflect.Modifier;
import javax.sound.sampled.CompoundControl;
import javax.sound.sampled.Control;

public class TypeTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.sampled.CompoundControl$Type");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods) && Modifier.isStatic(mods), "public static");
        check(c.getEnclosingClass() == CompoundControl.class, "enclosing class");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.sound.sampled.Control$Type"), "superclass Control.Type");
        check(c.getDeclaredConstructors().length == 1, "constructor count 1");
        check(Modifier.isProtected(c.getDeclaredConstructors()[0].getModifiers()), "protected constructor");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // no public static constants; type is created by subclassing
        CompoundControl.Type eq = new CompoundControl.Type("Equalizer") {};
        CompoundControl.Type graph = new CompoundControl.Type("Graphic EQ") {};

        check(eq instanceof Control.Type, "is a Control.Type");
        check("Equalizer".equals(eq.toString()), "toString name");
        check("Graphic EQ".equals(graph.toString()), "toString name 2");

        // identity equality inherited from Control.Type
        check(eq.equals(eq), "reflexive");
        check(!eq.equals(graph), "different instances not equal");
        check(!eq.equals(null), "not equal null");
        check(eq.hashCode() == eq.hashCode(), "hashCode stable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
