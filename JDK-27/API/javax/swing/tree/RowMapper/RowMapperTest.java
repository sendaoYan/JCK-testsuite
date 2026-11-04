/*
 * @test
 * @summary javax.swing.tree.RowMapper conformance: white-box structure + black-box behavior.
 * @run main RowMapperTest
 */

import java.lang.reflect.Modifier;

import javax.swing.tree.AbstractLayoutCache;
import javax.swing.tree.FixedHeightLayoutCache;
import javax.swing.tree.RowMapper;
import javax.swing.tree.TreePath;

public class RowMapperTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.tree.RowMapper");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == true, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        final TreePath pA = new TreePath("a");
        final TreePath pB = new TreePath("b");

        RowMapper mapper = new RowMapper() {
            public int[] getRowsForPaths(TreePath[] paths) {
                if (paths == null) return null;
                int[] rows = new int[paths.length];
                for (int i = 0; i < paths.length; i++) {
                    if (paths[i] == pA) rows[i] = 3;
                    else if (paths[i] == pB) rows[i] = 7;
                    else rows[i] = -1;
                }
                return rows;
            }
        };

        int[] rows = mapper.getRowsForPaths(new TreePath[] { pA, pB, new TreePath("other") });
        check(rows != null && rows.length == 3 && rows[0] == 3 && rows[1] == 7 && rows[2] == -1, "getRowsForPaths mapping");
        check(mapper.getRowsForPaths(null) == null, "getRowsForPaths null");

        // Concrete implementations of RowMapper.
        check(RowMapper.class.isAssignableFrom(AbstractLayoutCache.class), "AbstractLayoutCache implements RowMapper");
        check(RowMapper.class.isAssignableFrom(FixedHeightLayoutCache.class), "FixedHeightLayoutCache implements RowMapper");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
