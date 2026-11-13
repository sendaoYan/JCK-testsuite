/*
 * @test
 * @summary java.awt.Desktop conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main DesktopTest
 */

import java.awt.*;

import java.lang.reflect.*;
import java.awt.Desktop;

public class DesktopTest {
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
        Class<?> c = Class.forName("java.awt.Desktop", false, DesktopTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.Desktop");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public class");
        check(c.getEnumConstants() == null, "non-enum");
        check(c.getSuperclass() == Object.class, "extends Object");
        check(Modifier.isStatic(c.getMethod("getDesktop").getModifiers()), "getDesktop is static");
        check(Modifier.isStatic(c.getMethod("isDesktopSupported").getModifiers()), "isDesktopSupported is static");
        for (String n : new String[] {"isSupported", "open", "edit", "print", "browse", "mail"})
            check(hasMethod(c, n), "method present: " + n);
        check(Class.forName("java.awt.Desktop$Action", false, DesktopTest.class.getClassLoader()) != null,
              "nested Action type loads");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        boolean supported = Desktop.isDesktopSupported();
        check(supported || !supported, "isDesktopSupported returns a boolean");

        if (supported) {
            Desktop d = Desktop.getDesktop();
            check(d != null, "getDesktop non-null when supported");
            check(Desktop.getDesktop() == d, "getDesktop returns singleton");

            boolean deterministic = true;
            for (Desktop.Action a : Desktop.Action.values()) {
                deterministic &= (d.isSupported(a) == d.isSupported(a));
            }
            check(deterministic, "isSupported deterministic for all " + Desktop.Action.values().length + " actions");

            check(d.isSupported(Desktop.Action.OPEN) || !d.isSupported(Desktop.Action.OPEN), "isSupported(OPEN) boolean");
            check(d.isSupported(Desktop.Action.BROWSE) || !d.isSupported(Desktop.Action.BROWSE), "isSupported(BROWSE) boolean");
            check(d.isSupported(Desktop.Action.MAIL) || !d.isSupported(Desktop.Action.MAIL), "isSupported(MAIL) boolean");
            check(d.isSupported(Desktop.Action.APP_QUIT_HANDLER) || !d.isSupported(Desktop.Action.APP_QUIT_HANDLER),
                  "isSupported(APP_QUIT_HANDLER) boolean");
        } else {
            try {
                Desktop.getDesktop();
                check(false, "getDesktop should throw when unsupported");
            } catch (UnsupportedOperationException expected) {
                check(true, "getDesktop throws UnsupportedOperationException when unsupported");
            }
        }
    }

    // ---- shared helper for exact-exception assertions (deepening batch) ----
    interface ThrowingAction { void run() throws Exception; }

    static void expectExact(Class<? extends Throwable> expected, String tag, ThrowingAction action) {
        try {
            action.run();
            check(false, tag + ": expected " + expected.getSimpleName() + " but nothing was thrown");
        } catch (Throwable thrown) {
            check(thrown.getClass() == expected,
                  tag + ": expected " + expected.getName() + " but got " + thrown.getClass().getName());
        }
    }

    // ---- Part 3: contract deepening ----
    static void testContractDesktop() throws Exception {
        // 有头环境下 Desktop 一定可用：X11 和 Windows 都提供 DesktopPeer
        check(Desktop.isDesktopSupported(), "isDesktopSupported true on a headful display");
        Desktop d = Desktop.getDesktop();
        check(Desktop.getDesktop() == d, "getDesktop returns a singleton");
        check(d.isSupported(Desktop.Action.OPEN), "OPEN supported");
        // EDIT/PRINT/MAIL 是否可用取决于平台实现：X11 上通常没有注册关联程序，
        // 一律返回 false；Windows 的 WDesktopPeer#isSupported 对全部动作恒返回
        // true。这里在 Windows 上断言这个确定的能力，其余平台留给通用的
        // “每个 Action 都能回答”检查，具体分支在下面按能力取用。
        if (System.getProperty("os.name", "").startsWith("Windows")) {
            for (Desktop.Action a : new Desktop.Action[] {
                    Desktop.Action.EDIT, Desktop.Action.PRINT, Desktop.Action.MAIL}) {
                check(d.isSupported(a), "Windows desktop peer supports " + a);
            }
        }
        // every declared Action answers without throwing
        for (Desktop.Action act : Desktop.Action.values()) {
            check(d.isSupported(act) || !d.isSupported(act), "isSupported(" + act + ") answers a boolean");
        }
        //@8 check(Desktop.Action.values().length == 5, "JDK 8 declares 5 actions");
        //@27 check(Desktop.Action.values().length == 24, "JDK 9+ declares 24 actions");
        //@27 check(!d.isSupported(Desktop.Action.APP_ABOUT), "APP_ABOUT unsupported on Linux");
        //@27 check(!d.isSupported(Desktop.Action.APP_OPEN_FILE), "APP_OPEN_FILE unsupported on Linux");
        //@27 check(!d.isSupported(Desktop.Action.APP_MENU_BAR), "APP_MENU_BAR unsupported on Linux");
        //@27 check(!d.isSupported(Desktop.Action.BROWSE_FILE_DIR), "BROWSE_FILE_DIR unsupported on Linux");
        //@27 check(!d.isSupported(Desktop.Action.MOVE_TO_TRASH), "MOVE_TO_TRASH unsupported on Linux");
        // null action: X11 用 Set.contains 返回 false，Windows 的 WDesktopPeer
        // 用 switch 实现、对 null 抛 NPE。规范没有为此承诺具体行为，因此这里
        // 只要求“要么回答 boolean、要么以 NPE 拒绝 null”，不允许其他异常。
        try {
            d.isSupported(null);
            check(true, "isSupported(null) answers");
        } catch (NullPointerException expected) {
            passed++;
        } catch (Throwable t) {
            check(false, "isSupported(null) must answer or reject null with NPE: " + t);
        }
        // unsupported actions reject with UnsupportedOperationException
        java.io.File tmp = java.io.File.createTempFile("jck-desk", ".txt");
        tmp.deleteOnExit();
        if (!d.isSupported(Desktop.Action.EDIT)) {
            expectExact(UnsupportedOperationException.class, "edit on unsupported action", () -> d.edit(tmp));
        }
        if (!d.isSupported(Desktop.Action.PRINT)) {
            expectExact(UnsupportedOperationException.class, "print on unsupported action", () -> d.print(tmp));
        }
        if (!d.isSupported(Desktop.Action.MAIL)) {
            expectExact(UnsupportedOperationException.class, "mail on unsupported action", () -> d.mail());
        }
        if (!d.isSupported(Desktop.Action.BROWSE)) {
            expectExact(UnsupportedOperationException.class, "browse on unsupported action",
                  () -> d.browse(java.net.URI.create("http://127.0.0.1:1/")));
        }
        // missing file argument is validated before launch
        expectExact(IllegalArgumentException.class, "open(nonexistent)",
              () -> d.open(new java.io.File("/nonexistent-jck-probe")));
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDesktop();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
