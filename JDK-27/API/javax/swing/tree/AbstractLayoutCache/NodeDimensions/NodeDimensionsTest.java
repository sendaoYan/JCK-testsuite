/*
 * @test
 * @summary javax.swing.tree.AbstractLayoutCache.NodeDimensions conformance: white-box structure + black-box behavior.
 * @run main NodeDimensionsTest
 */

import java.awt.Rectangle;
import java.lang.reflect.Modifier;

import javax.swing.tree.AbstractLayoutCache;

public class NodeDimensionsTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.tree.AbstractLayoutCache$NodeDimensions");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == true, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // Anonymous NodeDimensions returning fixed, depth-dependent sizes.
        AbstractLayoutCache.NodeDimensions nd = new AbstractLayoutCache.NodeDimensions() {
            public Rectangle getNodeDimensions(Object value, int row, int depth, boolean expanded, Rectangle placeIn) {
                Rectangle r = (placeIn != null) ? placeIn : new Rectangle();
                r.x = depth * 5;
                r.y = 0;
                r.width = 12;
                r.height = 20;
                return r;
            }
        };

        // Reuse supplied Rectangle when provided.
        Rectangle reused = new Rectangle();
        Rectangle out = nd.getNodeDimensions("value", 2, 3, true, reused);
        check(out == reused, "reuses supplied rectangle");
        check(out.x == 15 && out.y == 0 && out.width == 12 && out.height == 20, "dimensions computed from depth");

        // Allocates a new Rectangle when placeIn is null.
        Rectangle fresh = nd.getNodeDimensions("value", 0, 0, false, null);
        check(fresh != null && fresh.x == 0 && fresh.width == 12 && fresh.height == 20, "allocates when null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
