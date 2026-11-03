/*
 * @test
 * @summary javax.swing.Spring conformance: white-box structure + black-box behavior.
 * @run main SpringTest
 */

import java.lang.reflect.Modifier;

import javax.swing.JPanel;
import javax.swing.Spring;

public class SpringTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.Spring");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        Spring c = Spring.constant(10);
        check(c.getValue() == 10, "constant getValue");
        check(c.getMinimumValue() == 10 && c.getPreferredValue() == 10 && c.getMaximumValue() == 10,
                "constant range");

        Spring r = Spring.constant(1, 10, 100);
        check(r.getMinimumValue() == 1 && r.getPreferredValue() == 10 && r.getMaximumValue() == 100,
                "constant(min,pref,max)");
        r.setValue(50);
        check(r.getValue() == 50, "setValue");

        check(Spring.sum(c, c).getValue() == 20, "sum");
        check(Spring.minus(c).getValue() == -10, "minus");
        check(Spring.max(c, Spring.constant(5)).getValue() == 10, "max");
        check(Spring.scale(c, 2.0f).getValue() == 20, "scale");

        check(Spring.width(new JPanel()) != null, "width non-null");
        check(Spring.height(new JPanel()) != null, "height non-null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
