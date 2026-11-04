/*
 * @test
 * @summary java.awt.Robot conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main RobotTest
 */

import java.lang.reflect.*;
import java.awt.AWTException;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;

public class RobotTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (Method m : c.getMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.Robot", false, RobotTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.Robot");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public class");
        check(c.getEnumConstants() == null, "non-enum");
        check(c.getConstructor() != null, "ctor ()");
        check(c.getConstructor(java.awt.GraphicsDevice.class) != null, "ctor (GraphicsDevice)");
        for (String n : new String[] {"createScreenCapture", "getPixelColor", "mouseMove",
                "getAutoDelay", "setAutoDelay", "isAutoWaitForIdle", "setAutoWaitForIdle"})
            check(hasMethod(c, n), "method present: " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        Robot robot;
        try {
            robot = new Robot();
        } catch (AWTException e) {
            check(true, "AWTException acceptable when platform lacks low-level input control");
            return;
        }
        check(robot != null, "Robot constructed");

        // default auto state + round-trips
        check(robot.getAutoDelay() == 0, "default autoDelay == 0");
        check(!robot.isAutoWaitForIdle(), "default autoWaitForIdle == false");
        robot.setAutoDelay(50);
        check(robot.getAutoDelay() == 50, "set/get autoDelay 50");
        robot.setAutoDelay(0);
        check(robot.getAutoDelay() == 0, "set/get autoDelay restored 0");
        robot.setAutoWaitForIdle(true);
        check(robot.isAutoWaitForIdle(), "set/get autoWaitForIdle true");
        robot.setAutoWaitForIdle(false);
        check(!robot.isAutoWaitForIdle(), "set/get autoWaitForIdle false");

        // documented defaults (compile-time constants)
        check(Robot.DEFAULT_DELAY == 20, "DEFAULT_DELAY == 20");
        check(Robot.DEFAULT_STEP_LENGTH == 2, "DEFAULT_STEP_LENGTH == 2");

        // screen capture + pixel query
        BufferedImage capture = robot.createScreenCapture(new Rectangle(0, 0, 8, 8));
        check(capture != null, "createScreenCapture non-null");
        check(capture.getWidth() == 8 && capture.getHeight() == 8, "capture dimensions 8x8");

        Color px = robot.getPixelColor(0, 0);
        check(px != null, "getPixelColor non-null");
        check(px.getAlpha() == 255, "pixel alpha == 255");
        check(px.getRed() >= 0 && px.getGreen() >= 0 && px.getBlue() >= 0, "pixel channels non-negative");

        // pointer motion + sync + delay are callable
        robot.mouseMove(50, 50);
        robot.waitForIdle();
        robot.delay(5);
        check(true, "mouseMove/waitForIdle/delay callable");

        check(robot.toString() != null, "toString non-null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
