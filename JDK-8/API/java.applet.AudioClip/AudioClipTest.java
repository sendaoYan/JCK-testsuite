/*
 * @test
 * @summary java.applet.AudioClip conformance: interface reflection.
 * @run main AudioClipTest
 */

public class AudioClipTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.applet.AudioClip");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "play"), "method play");
        check(hasMethod(c, "loop"), "method loop");
        check(hasMethod(c, "stop"), "method stop");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
