/*
 * @test
 * @summary java.awt.Robot conformance: class reflection.
 * @run main RobotTest
 */

public class RobotTest {
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
        Class<?> c = Class.forName("java.awt.Robot");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "mouseMove"), "method mouseMove");
        check(hasMethod(c, "mousePress"), "method mousePress");
        check(hasMethod(c, "mouseRelease"), "method mouseRelease");
        check(hasMethod(c, "mouseWheel"), "method mouseWheel");
        check(hasMethod(c, "keyPress"), "method keyPress");
        check(hasMethod(c, "keyRelease"), "method keyRelease");
        check(hasMethod(c, "getPixelColor"), "method getPixelColor");
        check(hasMethod(c, "createScreenCapture"), "method createScreenCapture");
        check(hasMethod(c, "isAutoWaitForIdle"), "method isAutoWaitForIdle");
        check(hasMethod(c, "setAutoWaitForIdle"), "method setAutoWaitForIdle");
        check(hasMethod(c, "getAutoDelay"), "method getAutoDelay");
        check(hasMethod(c, "setAutoDelay"), "method setAutoDelay");
        check(hasMethod(c, "delay"), "method delay");
        check(hasMethod(c, "waitForIdle"), "method waitForIdle");
        check(hasMethod(c, "toString"), "method toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
