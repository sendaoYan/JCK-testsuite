/*
 * @test
 * @summary java.applet.AppletContext conformance: interface reflection.
 * @run main AppletContextTest
 */

public class AppletContextTest {
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
        Class<?> c = Class.forName("java.applet.AppletContext");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getAudioClip"), "method getAudioClip");
        check(hasMethod(c, "getImage"), "method getImage");
        check(hasMethod(c, "getApplet"), "method getApplet");
        check(hasMethod(c, "getApplets"), "method getApplets");
        check(hasMethod(c, "showDocument"), "method showDocument");
        check(hasMethod(c, "showStatus"), "method showStatus");
        check(hasMethod(c, "setStream"), "method setStream");
        check(hasMethod(c, "getStream"), "method getStream");
        check(hasMethod(c, "getStreamKeys"), "method getStreamKeys");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
