/*
 * @test
 * @summary javax.swing.ProgressMonitor conformance: white-box structure + black-box behavior.
 * @run main ProgressMonitorTest
 */

import java.lang.reflect.Modifier;

import javax.swing.ProgressMonitor;

public class ProgressMonitorTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.ProgressMonitor");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(!Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.accessibility.Accessible"), "interface 0");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        ProgressMonitor pm = new ProgressMonitor(null, "message", "note", 0, 100);
        check(pm.getMinimum() == 0, "getMinimum");
        check(pm.getMaximum() == 100, "getMaximum");
        pm.setProgress(50);
        check(!pm.isCanceled(), "isCanceled false");
        pm.setNote("n2");
        check("n2".equals(pm.getNote()), "setNote/getNote");
        pm.setMinimum(10);
        check(pm.getMinimum() == 10, "setMinimum");
        pm.setMaximum(90);
        check(pm.getMaximum() == 90, "setMaximum");
        pm.setMillisToDecideToPopup(100);
        check(pm.getMillisToDecideToPopup() == 100, "millisToDecideToPopup");
        pm.setMillisToPopup(200);
        check(pm.getMillisToPopup() == 200, "millisToPopup");
        pm.close();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
