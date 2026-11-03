/*
 * @test
 * @summary javax.swing.Renderer conformance: white-box structure + black-box behavior.
 * @run main RendererTest
 */

import java.awt.Component;
import java.lang.reflect.Modifier;

import javax.swing.JLabel;
import javax.swing.Renderer;

public class RendererTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.Renderer");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        Renderer r = new Renderer() {
            private Component c;
            public void setValue(Object value, boolean selected) {
                c = new JLabel(String.valueOf(value));
            }
            public Component getComponent() { return c; }
        };
        check(r.getComponent() == null, "component initially null");
        r.setValue("value", true);
        check(r.getComponent() instanceof JLabel, "component after setValue");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
