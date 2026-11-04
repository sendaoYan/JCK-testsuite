/*
 * @test
 * @summary javax.swing.tree.ExpandVetoException conformance: white-box structure + black-box behavior.
 * @run main ExpandVetoExceptionTest
 */

import java.lang.reflect.Modifier;

import javax.swing.event.TreeExpansionEvent;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;

public class ExpandVetoExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.tree.ExpandVetoException");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Exception"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        TreePath path = new TreePath(new Object[] { "root", "child" });
        TreeExpansionEvent event = new TreeExpansionEvent("source", path);

        // Two-arg constructor round-trips the message.
        ExpandVetoException e = new ExpandVetoException(event, "veto message");
        check("veto message".equals(e.getMessage()), "getMessage round-trip");
        check(e.getCause() == null, "no cause by default");

        // One-arg constructor -> null message.
        ExpandVetoException e2 = new ExpandVetoException(event);
        check(e2.getMessage() == null, "no-arg message null");

        // Both are Throwable / Exception.
        check(e instanceof Exception, "is an Exception");
        check(e instanceof Throwable, "is a Throwable");

        // toString includes the class name.
        check(e.toString().contains("ExpandVetoException"), "toString names class");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
