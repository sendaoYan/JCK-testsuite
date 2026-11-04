/*
 * @test
 * @summary javax.swing.ProgressMonitorInputStream conformance: white-box structure + black-box behavior.
 * @run main ProgressMonitorInputStreamTest
 */

import java.io.ByteArrayInputStream;
import java.lang.reflect.Modifier;

import javax.swing.ProgressMonitor;
import javax.swing.ProgressMonitorInputStream;

public class ProgressMonitorInputStreamTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.ProgressMonitorInputStream");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.io.FilterInputStream"), "superclass");
        check(!Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[] { 1, 2, 3 });
        ProgressMonitorInputStream in = new ProgressMonitorInputStream(null, "message", bais);
        ProgressMonitor pm = in.getProgressMonitor();
        check(pm != null, "getProgressMonitor");
        check(in.read() == 1, "read 1");
        check(in.read() == 2, "read 2");
        check(in.read() == 3, "read 3");
        check(in.read() == -1, "read eof");
        in.close();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
