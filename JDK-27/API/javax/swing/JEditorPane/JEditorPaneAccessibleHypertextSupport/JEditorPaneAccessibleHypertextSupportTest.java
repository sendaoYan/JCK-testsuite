/*
 * @test
 * @summary javax.swing.JEditorPane.JEditorPaneAccessibleHypertextSupport conformance: type shape and supertypes.
 * @run main JEditorPaneAccessibleHypertextSupportTest
 */

import java.lang.reflect.Modifier;

public class JEditorPaneAccessibleHypertextSupportTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.JEditorPane$JEditorPaneAccessibleHypertextSupport");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == false, "public modifier");
        check(Modifier.isProtected(mods) == true, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JEditorPane$AccessibleJEditorPane"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.accessibility.AccessibleHypertext"), "interface 0");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
