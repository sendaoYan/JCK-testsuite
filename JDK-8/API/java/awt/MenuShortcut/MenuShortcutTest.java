/*
 * @test
 * @summary java.awt.MenuShortcut conformance: key and shift modifier.
 * @run main MenuShortcutTest
 */


import java.awt.MenuShortcut;
public class MenuShortcutTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) {
        MenuShortcut s = new MenuShortcut(65);
        check(s.getKey() == 65, "key");
        check(!s.usesShiftModifier(), "no shift");
        MenuShortcut sh = new MenuShortcut(65, true);
        check(sh.getKey() == 65 && sh.usesShiftModifier(), "shift");
        check(sh.equals(new MenuShortcut(65, true)), "equals");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
