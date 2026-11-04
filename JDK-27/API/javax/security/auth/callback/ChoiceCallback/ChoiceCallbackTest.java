/*
 * @test
 * @summary javax.security.auth.callback.ChoiceCallback conformance: white-box structure + black-box behavior.
 * @run main ChoiceCallbackTest
 */

import java.lang.reflect.*;
import java.util.Arrays;

public class ChoiceCallbackTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static void checkMethods(Class<?> c, String[] ns) {
        for (String n : ns) check(hasMethod(c, n), "method " + n);
    }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.security.auth.callback.ChoiceCallback");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "is class");
        check(hasInterface(c, "javax.security.auth.callback.Callback"), "implements Callback");
        check(hasInterface(c, "java.io.Serializable"), "implements Serializable");
        check(c.getSuperclass() == Object.class, "extends Object");
        checkMethods(c, new String[]{
            "getPrompt", "getChoices", "getDefaultChoice",
            "allowMultipleSelections", "setSelectedIndex",
            "setSelectedIndexes", "getSelectedIndexes"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // Main construction and getters.
        String[] choices = new String[]{"A", "B", "C"};
        javax.security.auth.callback.ChoiceCallback c =
            new javax.security.auth.callback.ChoiceCallback("prompt", choices, 1, true);
        check("prompt".equals(c.getPrompt()), "getPrompt");
        check(Arrays.equals(new String[]{"A", "B", "C"}, c.getChoices()), "getChoices");
        check(c.getDefaultChoice() == 1, "getDefaultChoice");
        check(c.allowMultipleSelections(), "allowMultipleSelections true");

        // Constructor clones the choices argument.
        choices[0] = "Z";
        check("A".equals(c.getChoices()[0]), "constructor clones choices");

        // getChoices returns a defensive copy.
        String[] out1 = c.getChoices();
        out1[0] = "Y";
        check("A".equals(c.getChoices()[0]), "getChoices returns copy");

        // No selection made yet -> null.
        check(c.getSelectedIndexes() == null, "getSelectedIndexes null before set");

        // Single selection.
        c.setSelectedIndex(0);
        int[] single = c.getSelectedIndexes();
        check(single != null && single.length == 1 && single[0] == 0, "setSelectedIndex single");

        // Multiple selection + defensive copies.
        int[] sel = new int[]{0, 2};
        c.setSelectedIndexes(sel);
        sel[0] = 1; // mutating the argument must not affect the callback
        int[] got = c.getSelectedIndexes();
        check(got.length == 2 && got[0] == 0 && got[1] == 2, "setSelectedIndexes stores values");
        got[0] = 9; // mutating the returned copy must not affect the callback
        check(c.getSelectedIndexes()[0] == 0, "getSelectedIndexes returns copy");

        // Single selection when multiple not allowed.
        javax.security.auth.callback.ChoiceCallback singleOnly =
            new javax.security.auth.callback.ChoiceCallback("p", new String[]{"x", "y"}, 0, false);
        check(!singleOnly.allowMultipleSelections(), "allowMultipleSelections false");
        singleOnly.setSelectedIndex(1);
        check(singleOnly.getSelectedIndexes().length == 1 && singleOnly.getSelectedIndexes()[0] == 1,
              "single selection without multiple allowed");

        // setSelectedIndexes must throw when multiple selections are not allowed.
        boolean threw = false;
        try {
            singleOnly.setSelectedIndexes(new int[]{0, 1});
        } catch (UnsupportedOperationException expected) {
            threw = true;
        }
        check(threw, "setSelectedIndexes throws UnsupportedOperationException");

        // setSelectedIndexes(null) and empty array are accepted with multiple allowed.
        c.setSelectedIndexes(null);
        check(c.getSelectedIndexes() == null, "setSelectedIndexes(null)");
        c.setSelectedIndexes(new int[0]);
        check(c.getSelectedIndexes() != null && c.getSelectedIndexes().length == 0,
              "setSelectedIndexes(empty)");

        // Constructor validation: each invalid input throws IllegalArgumentException.
        // prompt null / empty
        for (String p : new String[]{null, ""}) {
            boolean t = false;
            try { new javax.security.auth.callback.ChoiceCallback(p, new String[]{"A"}, 0, false); }
            catch (IllegalArgumentException e) { t = true; }
            check(t, "IAE for prompt=" + (p == null ? "null" : "empty"));
        }
        // choices null / empty
        for (String[] ch : new String[][]{null, new String[0]}) {
            boolean t = false;
            try { new javax.security.auth.callback.ChoiceCallback("p", ch, 0, false); }
            catch (IllegalArgumentException e) { t = true; }
            check(t, "IAE for choices=" + (ch == null ? "null" : "empty"));
        }
        // null / empty choice element
        for (String[] ch : new String[][]{new String[]{null}, new String[]{""}}) {
            boolean t = false;
            try { new javax.security.auth.callback.ChoiceCallback("p", ch, 0, false); }
            catch (IllegalArgumentException e) { t = true; }
            check(t, "IAE for null/empty choice element");
        }
        // invalid defaultChoice bounds
        for (int d : new int[]{-1, 2}) {
            boolean t = false;
            try { new javax.security.auth.callback.ChoiceCallback("p", new String[]{"A", "B"}, d, false); }
            catch (IllegalArgumentException e) { t = true; }
            check(t, "IAE for defaultChoice=" + d);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
