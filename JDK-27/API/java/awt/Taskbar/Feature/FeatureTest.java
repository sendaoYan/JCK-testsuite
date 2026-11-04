/*
 * @test
 * @summary java.awt.Taskbar.Feature conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main FeatureTest
 */

import java.lang.reflect.*;
import java.awt.Taskbar;

public class FeatureTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.Taskbar$Feature", false, FeatureTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.Taskbar.Feature");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) && Modifier.isFinal(m), "public final");
        check(c.isEnum(), "is enum");
        check(c.getSuperclass() == Enum.class, "extends java.lang.Enum");
        check(c.getEnumConstants() != null && c.getEnumConstants().length == 10, "10 enum constants");
        check(c.getMethod("values") != null, "values() present");
        check(c.getMethod("valueOf", String.class) != null, "valueOf(String) present");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        Taskbar.Feature[] vals = Taskbar.Feature.values();
        check(vals.length == 10, "values().length == 10");
        check(Taskbar.Feature.ICON_BADGE_TEXT.ordinal() == 0, "ICON_BADGE_TEXT ordinal 0");
        check(Taskbar.Feature.ICON_BADGE_NUMBER.ordinal() == 1, "ICON_BADGE_NUMBER ordinal 1");
        check(Taskbar.Feature.ICON_BADGE_IMAGE_WINDOW.ordinal() == 2, "ICON_BADGE_IMAGE_WINDOW ordinal 2");
        check(Taskbar.Feature.ICON_IMAGE.ordinal() == 3, "ICON_IMAGE ordinal 3");
        check(Taskbar.Feature.MENU.ordinal() == 4, "MENU ordinal 4");

        check(Taskbar.Feature.PROGRESS_STATE_WINDOW != null, "PROGRESS_STATE_WINDOW non-null");
        check(Taskbar.Feature.PROGRESS_VALUE != null, "PROGRESS_VALUE non-null");
        check(Taskbar.Feature.USER_ATTENTION_WINDOW != null, "USER_ATTENTION_WINDOW non-null");

        check(Taskbar.Feature.valueOf("ICON_IMAGE") == Taskbar.Feature.ICON_IMAGE, "valueOf(ICON_IMAGE)");
        check(Taskbar.Feature.PROGRESS_VALUE.name().equals("PROGRESS_VALUE"), "name()");

        boolean distinct = true;
        for (int i = 0; i < vals.length; i++)
            for (int j = i + 1; j < vals.length; j++)
                distinct &= (vals[i] != vals[j]);
        check(distinct, "all 10 constants distinct");

        for (Taskbar.Feature f : vals) {
            check(Taskbar.Feature.valueOf(f.name()) == f, "valueOf(name) identity for " + f);
        }

        try {
            Taskbar.Feature.valueOf("BOGUS");
            check(false, "valueOf unknown should throw");
        } catch (IllegalArgumentException expected) {
            check(true, "valueOf unknown throws IllegalArgumentException");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
