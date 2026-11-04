/*
 * @test
 * @summary javax.security.sasl.RealmChoiceCallback conformance: white-box structure + black-box behavior.
 * @run main RealmChoiceCallbackTest
 */

import java.lang.reflect.*;
import java.util.Arrays;
import javax.security.sasl.RealmChoiceCallback;

public class RealmChoiceCallbackTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.security.sasl.RealmChoiceCallback");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass().getName().equals("javax.security.auth.callback.ChoiceCallback"), "extends ChoiceCallback");
        check(javax.security.auth.callback.Callback.class.isAssignableFrom(c), "implements Callback");
        check(java.io.Serializable.class.isAssignableFrom(c), "implements Serializable");
        boolean ctor = false;
        for (Constructor<?> k : c.getConstructors()) {
            Class<?>[] p = k.getParameterTypes();
            if (p.length == 4 && p[0] == String.class && p[1] == String[].class && p[2] == int.class && p[3] == boolean.class) ctor = true;
        }
        check(ctor, "String,String[],int,boolean constructor");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        RealmChoiceCallback c = new RealmChoiceCallback("prompt", new String[]{"R1", "R2"}, 0, false);
        check("prompt".equals(c.getPrompt()), "getPrompt");
        check(Arrays.equals(c.getChoices(), new String[]{"R1", "R2"}), "getChoices content");
        check(c.getDefaultChoice() == 0, "getDefaultChoice");
        check(!c.allowMultipleSelections(), "allowMultipleSelections false");

        // getChoices returns a defensive copy
        String[] copy = c.getChoices();
        copy[0] = "MUTATED";
        check(Arrays.equals(c.getChoices(), new String[]{"R1", "R2"}), "getChoices defensive copy");

        // single selection default
        check(c.getSelectedIndexes() == null, "selected indexes initially null");
        c.setSelectedIndex(1);
        check(Arrays.equals(c.getSelectedIndexes(), new int[]{1}), "setSelectedIndex/getSelectedIndexes");
        try { c.setSelectedIndexes(new int[]{0, 1}); check(false, "setSelectedIndexes single should throw"); }
        catch (UnsupportedOperationException e) { check(true, "setSelectedIndexes single throws UnsupportedOperationException"); }

        // multiple selection
        RealmChoiceCallback m = new RealmChoiceCallback("p", new String[]{"A", "B", "C"}, 2, true);
        check(m.allowMultipleSelections(), "allowMultipleSelections true");
        check(m.getDefaultChoice() == 2, "getDefaultChoice multiple");
        m.setSelectedIndexes(new int[]{0, 2});
        check(Arrays.equals(m.getSelectedIndexes(), new int[]{0, 2}), "setSelectedIndexes multiple");

        // invalid constructor arguments
        try { new RealmChoiceCallback("p", new String[]{"A", "B"}, -1, false); check(false, "negative default should throw"); }
        catch (IllegalArgumentException e) { check(true, "negative default throws IllegalArgumentException"); }
        try { new RealmChoiceCallback("p", new String[]{"A", "B"}, 5, false); check(false, "out-of-range default should throw"); }
        catch (IllegalArgumentException e) { check(true, "out-of-range default throws IllegalArgumentException"); }
        try { new RealmChoiceCallback("p", null, 0, false); check(false, "null choices should throw"); }
        catch (IllegalArgumentException e) { check(true, "null choices throws IllegalArgumentException"); }
        try { new RealmChoiceCallback("p", new String[0], 0, false); check(false, "empty choices should throw"); }
        catch (IllegalArgumentException e) { check(true, "empty choices throws IllegalArgumentException"); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
