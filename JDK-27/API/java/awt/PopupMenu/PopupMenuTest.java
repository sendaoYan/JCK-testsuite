/*
 * @test
 * @summary java.awt.PopupMenu conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main PopupMenuTest
 */

import java.awt.*;
import java.lang.reflect.*;

public class PopupMenuTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.PopupMenu");
        check(c != null, "type loads: java.awt.PopupMenu");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public");
        check(!Modifier.isAbstract(m), "not abstract");
        check(c.getSuperclass() == Menu.class, "extends Menu");
        for (String name : new String[]{"getParent", "addNotify", "show"}) {
            check(hasMethod(c, name), "has " + name);
        }
    }

    // Windows 上 PopupMenu.show() 会走到原生 TrackPopupMenu 模态消息循环，
    // 需要用户选择/取消才返回，而且 PopupMenu.show() 是 synchronized
    // (getTreeLock()) 包住 native _show 的，所以整个模态期间一直持有
    // AWTTreeLock。无交互桌面上无法完成，还会让后续 dispose() 里的
    // EventQueue.invokeAndWait 等待 EDT 抢同一把锁而永久阻塞。因此在
    // Windows 上不真正进入原生 show()，只校验可移植的前置/状态契约。
    static boolean isWindows() {
        return System.getProperty("os.name", "").startsWith("Windows");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        PopupMenu p = new PopupMenu();
        check("".equals(p.getLabel()), "default empty label");
        PopupMenu p2 = new PopupMenu("pop");
        check("pop".equals(p2.getLabel()), "constructor label");

        MenuItem a = new MenuItem("a");
        p2.add(a);
        check(p2.getItemCount() == 1 && p2.getItem(0) == a, "add/getItemCount/getItem");

        Frame f = new Frame("PopupMenuTest");
        f.setSize(200, 200);
        f.setVisible(true);
        try {
            Canvas c = new Canvas();
            f.add(c);
            c.add(p2);
            check(p2.getParent() == c, "getParent after add to component");
            if (isWindows()) {
                check(p2.getItemCount() == 1 && p2.getParent() == c,
                      "popup intact (native modal show not exercised on Windows)");
            } else {
                check(showOffThread(p2, c, 0, 0) == null, "show on a showing origin must not throw");
            }
            check(p2.getItemCount() == 1, "popup still intact after show");
        } finally {
            f.dispose();
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

    // X11 上 show() 立即返回；这里仍放到独立线程执行，避免实现差异让测试线程被卡住。
    // （Windows 上不调用它，原因见 isWindows() 上方注释。）
    static Throwable showOffThread(PopupMenu menu, Component origin, int x, int y) {
        final Throwable[] err = new Throwable[1];
        Thread t = new Thread(() -> {
            try { menu.show(origin, x, y); } catch (Throwable e) { err[0] = e; }
        }, "popup-show");
        t.setDaemon(true);
        t.start();
        try {
            t.join(2000); // enough to surface a synchronous show() failure
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        return err[0];
    }

    // ---- Part 3: contract deepening ----
    static void testContractPopupMenu() throws Exception {
        PopupMenu p = new PopupMenu("p");
        check("p".equals(p.getLabel()), "ctor label");
        check(p.getParent() == null, "no parent before attach");
        p.add("one"); p.addSeparator();
        check(p.getItemCount() == 2, "inherited Menu item plumbing");
        Frame f = new Frame("PopupMenuContract");
        f.setSize(160, 160);
        Canvas host = new Canvas();
        f.add(host);
        host.add(p);
        check(p.getParent() == host, "add wires popup parent");
        // 父组件尚未显示时 show() 在进入原生层之前就抛 RuntimeException，
        // 该检查与平台无关，Windows 上同样不会阻塞。
        expectExact(RuntimeException.class, "show() with non-showing origin", () -> p.show(host, 5, 5));
        f.setVisible(true);
        try {
            if (isWindows()) {
                check(p.getItemCount() == 2 && p.getParent() == host,
                      "popup intact (native modal show not exercised on Windows)");
            } else {
                check(showOffThread(p, host, 5, 5) == null, "show on a showing origin must not throw");
            }
            check(p.getItemCount() == 2, "items intact after show");
        } finally {
            f.dispose();
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractPopupMenu();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
