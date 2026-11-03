/*
 * @test
 * @summary javax.swing.tree.TreePath conformance: white-box structure + black-box behavior.
 * @run main TreePathTest
 */

import java.lang.reflect.Modifier;

import javax.swing.tree.TreePath;

public class TreePathTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.tree.TreePath");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.io.Serializable"), "interface 0");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // Single-node constructor.
        TreePath p1 = new TreePath("root");
        check(p1.getPathCount() == 1, "single-node path count 1");
        check("root".equals(p1.getPathComponent(0)), "single-node component 0");
        check("root".equals(p1.getLastPathComponent()), "single-node last component");
        check(p1.getParentPath() == null, "single-node parent null");
        check("root".equals(p1.getPath()[0]), "getPath single");

        // Object[] constructor (defensive copy).
        Object[] elems = { "root", "a", "leaf" };
        TreePath p = new TreePath(elems);
        elems[1] = "MUTATED";
        check("a".equals(p.getPathComponent(1)), "array ctor copies input");
        check(p.getPathCount() == 3, "path count 3");
        check("root".equals(p.getPathComponent(0)), "component 0");
        check("a".equals(p.getPathComponent(1)), "component 1");
        check("leaf".equals(p.getPathComponent(2)), "component 2");
        check("leaf".equals(p.getLastPathComponent()), "last component leaf");

        // getPath returns a copy.
        Object[] copy = p.getPath();
        copy[0] = "MUTATED";
        check("root".equals(p.getPathComponent(0)), "getPath returns copy");

        // getParentPath.
        TreePath parent = p.getParentPath();
        check(parent.getPathCount() == 2, "parent path count 2");
        check("root".equals(parent.getPathComponent(0)) && "a".equals(parent.getPathComponent(1)), "parent components");

        // pathByAddingChild (does not mutate receiver).
        TreePath child = p.pathByAddingChild("leafChild");
        check(p.getPathCount() == 3, "pathByAddingChild does not mutate");
        check(child.getPathCount() == 4, "child path count 4");
        check("leafChild".equals(child.getLastPathComponent()), "child last component");
        check("leaf".equals(p.getLastPathComponent()), "receiver unchanged");

        // isDescendant.
        check(p.isDescendant(child) == true, "child is descendant of p");
        check(child.isDescendant(p) == false, "p not descendant of child");
        check(p.isDescendant(p) == true, "path descendant of itself");
        check(p.isDescendant(new TreePath("other")) == false, "unrelated not descendant");

        // equals / hashCode round-trip.
        TreePath same = new TreePath(new Object[] { "root", "a", "leaf" });
        check(p.equals(same) == true, "equals same components");
        check(same.equals(p) == true, "equals symmetric");
        check(p.hashCode() == same.hashCode(), "equal paths same hashCode");
        check(p.equals(child) == false, "not equals different length");
        check(p.equals(null) == false, "not equals null");
        check(p.equals("root") == false, "not equals non-TreePath");

        // toString is non-null and contains last component.
        String s = p.toString();
        check(s != null && s.contains("leaf"), "toString contains last component");

        // getPathComponent out of bounds.
        try {
            p.getPathComponent(3);
            throw new AssertionError("expected exception for getPathComponent(3)");
        } catch (IllegalArgumentException expected) {
            passed++;
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
