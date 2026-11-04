/*
 * @test
 * @summary java.awt.MediaTracker conformance: white-box structure + black-box behavior.
 * @run main MediaTrackerTest
 */

import java.lang.reflect.*;
import java.awt.Canvas;
import java.awt.Component;
import java.awt.MediaTracker;
import java.awt.image.BufferedImage;

public class MediaTrackerTest {
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
        Class<?> c = Class.forName("java.awt.MediaTracker", false, MediaTrackerTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.MediaTracker");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) && !Modifier.isAbstract(m), "public concrete class");
        check(c.getEnumConstants() == null, "non-enum");
        check(java.io.Serializable.class.isAssignableFrom(c), "implements Serializable");
        check(c.getConstructor(java.awt.Component.class) != null, "ctor (Component)");
        for (String n : new String[] {"addImage", "checkAll", "checkID", "statusAll", "statusID",
                "waitForAll", "waitForID", "isErrorAny", "isErrorID", "removeImage"})
            check(hasMethod(c, n), "method present: " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        check(MediaTracker.LOADING == 1, "LOADING == 1");
        check(MediaTracker.ABORTED == 2, "ABORTED == 2");
        check(MediaTracker.ERRORED == 4, "ERRORED == 4");
        check(MediaTracker.COMPLETE == 8, "COMPLETE == 8");

        Component comp = new Canvas();
        MediaTracker mt = new MediaTracker(comp);
        check(mt != null, "ctor with dummy Component");

        BufferedImage img = new BufferedImage(4, 5, BufferedImage.TYPE_INT_ARGB);
        BufferedImage img2 = new BufferedImage(6, 7, BufferedImage.TYPE_INT_ARGB);
        mt.addImage(img, 1);
        mt.addImage(img2, 2);

        // BufferedImage is already complete -> tracker reports complete without real loading
        check(mt.checkID(1), "checkID(1) complete for BufferedImage");
        check(mt.checkAll(), "checkAll complete");
        check(mt.checkID(999), "checkID for untracked id is vacuously true");
        check(mt.statusID(1, true) == MediaTracker.COMPLETE, "statusID(1,true) == COMPLETE");
        check(mt.statusID(1, false) == MediaTracker.COMPLETE, "statusID(1,false) == COMPLETE");
        check(mt.statusAll(true) == MediaTracker.COMPLETE, "statusAll(true) == COMPLETE");

        // no errors
        check(!mt.isErrorID(1), "isErrorID(1) false");
        check(!mt.isErrorAny(), "isErrorAny false");
        check(mt.getErrorsID(1) == null, "getErrorsID(1) null");
        check(mt.getErrorsAny() == null, "getErrorsAny null");

        try {
            check(mt.waitForID(1, 0), "waitForID(1,0) returns true (already loaded)");
            check(mt.waitForAll(0), "waitForAll(0) returns true (already loaded)");
            mt.waitForAll();
            check(true, "waitForAll() no-throw");
        } catch (InterruptedException e) {
            check(false, "unexpected InterruptedException");
        }

        // scaled addImage overload then removal
        mt.addImage(img2, 3, 8, 8);
        check(mt.checkID(3), "checkID(3) after scaled addImage");
        mt.removeImage(img);
        check(mt.checkID(1), "checkID(1) vacuously true after removeImage(Image)");
        mt.removeImage(img2, 2);
        check(mt.checkID(2), "checkID(2) vacuously true after removeImage(Image,int)");
        mt.removeImage(img2, 3, 8, 8);
        check(mt.checkID(3), "checkID(3) vacuously true after removeImage(Image,int,int,int)");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
