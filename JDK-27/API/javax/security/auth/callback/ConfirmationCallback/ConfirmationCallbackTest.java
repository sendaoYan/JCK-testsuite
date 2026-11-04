/*
 * @test
 * @summary javax.security.auth.callback.ConfirmationCallback conformance: white-box structure + black-box behavior.
 * @run main ConfirmationCallbackTest
 */

import java.lang.reflect.*;
import java.util.Arrays;

public class ConfirmationCallbackTest {
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
        Class<?> c = Class.forName("javax.security.auth.callback.ConfirmationCallback");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "is class");
        check(hasInterface(c, "javax.security.auth.callback.Callback"), "implements Callback");
        check(hasInterface(c, "java.io.Serializable"), "implements Serializable");
        check(c.getSuperclass() == Object.class, "extends Object");
        checkMethods(c, new String[]{
            "getPrompt", "getMessageType", "getOptionType", "getOptions",
            "getDefaultOption", "setSelectedIndex", "getSelectedIndex"});
        // Public option/message-type constants.
        check(hasField(c, "UNSPECIFIED_OPTION"), "constant UNSPECIFIED_OPTION");
        check(hasField(c, "YES_NO_OPTION"), "constant YES_NO_OPTION");
        check(hasField(c, "YES_NO_CANCEL_OPTION"), "constant YES_NO_CANCEL_OPTION");
        check(hasField(c, "OK_CANCEL_OPTION"), "constant OK_CANCEL_OPTION");
        check(hasField(c, "YES"), "constant YES");
        check(hasField(c, "NO"), "constant NO");
        check(hasField(c, "CANCEL"), "constant CANCEL");
        check(hasField(c, "OK"), "constant OK");
        check(hasField(c, "INFORMATION"), "constant INFORMATION");
        check(hasField(c, "WARNING"), "constant WARNING");
        check(hasField(c, "ERROR"), "constant ERROR");
    }
    static boolean hasField(Class<?> c, String n) {
        for (Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // Response / option-type / message-type constants.
        check(javax.security.auth.callback.ConfirmationCallback.YES == 0, "YES==0");
        check(javax.security.auth.callback.ConfirmationCallback.NO == 1, "NO==1");
        check(javax.security.auth.callback.ConfirmationCallback.CANCEL == 2, "CANCEL==2");
        check(javax.security.auth.callback.ConfirmationCallback.OK == 3, "OK==3");
        check(javax.security.auth.callback.ConfirmationCallback.UNSPECIFIED_OPTION == -1, "UNSPECIFIED_OPTION==-1");
        check(javax.security.auth.callback.ConfirmationCallback.YES_NO_OPTION == 0, "YES_NO_OPTION==0");
        check(javax.security.auth.callback.ConfirmationCallback.YES_NO_CANCEL_OPTION == 1, "YES_NO_CANCEL_OPTION==1");
        check(javax.security.auth.callback.ConfirmationCallback.OK_CANCEL_OPTION == 2, "OK_CANCEL_OPTION==2");
        check(javax.security.auth.callback.ConfirmationCallback.INFORMATION == 0, "INFORMATION==0");
        check(javax.security.auth.callback.ConfirmationCallback.WARNING == 1, "WARNING==1");
        check(javax.security.auth.callback.ConfirmationCallback.ERROR == 2, "ERROR==2");

        // Constructor 1: (messageType, optionType, defaultOption).
        javax.security.auth.callback.ConfirmationCallback c1 =
            new javax.security.auth.callback.ConfirmationCallback(
                javax.security.auth.callback.ConfirmationCallback.WARNING,
                javax.security.auth.callback.ConfirmationCallback.YES_NO_OPTION,
                javax.security.auth.callback.ConfirmationCallback.NO);
        check(c1.getPrompt() == null, "no-prompt ctor prompt null");
        check(c1.getMessageType() == javax.security.auth.callback.ConfirmationCallback.WARNING, "c1 messageType");
        check(c1.getOptionType() == javax.security.auth.callback.ConfirmationCallback.YES_NO_OPTION, "c1 optionType");
        check(c1.getDefaultOption() == javax.security.auth.callback.ConfirmationCallback.NO, "c1 defaultOption");
        check(c1.getOptions() == null, "c1 options null");

        // Constructor 2: (messageType, options[], defaultOption).
        String[] opts = new String[]{"Continue", "Abort"};
        javax.security.auth.callback.ConfirmationCallback c2 =
            new javax.security.auth.callback.ConfirmationCallback(
                javax.security.auth.callback.ConfirmationCallback.INFORMATION, opts, 1);
        check(c2.getPrompt() == null, "c2 prompt null");
        check(c2.getOptionType() == javax.security.auth.callback.ConfirmationCallback.UNSPECIFIED_OPTION, "c2 optionType unspecified");
        check(c2.getDefaultOption() == 1, "c2 defaultOption index");
        check(Arrays.equals(new String[]{"Continue", "Abort"}, c2.getOptions()), "c2 options");

        // Constructor clones the options argument.
        opts[0] = "Z";
        check("Continue".equals(c2.getOptions()[0]), "constructor clones options");

        // getOptions returns a defensive copy.
        String[] gotOpts = c2.getOptions();
        gotOpts[0] = "Y";
        check("Continue".equals(c2.getOptions()[0]), "getOptions returns copy");

        // Constructor 3: (prompt, messageType, optionType, defaultOption).
        javax.security.auth.callback.ConfirmationCallback c3 =
            new javax.security.auth.callback.ConfirmationCallback(
                "prompt", javax.security.auth.callback.ConfirmationCallback.ERROR,
                javax.security.auth.callback.ConfirmationCallback.OK_CANCEL_OPTION,
                javax.security.auth.callback.ConfirmationCallback.CANCEL);
        check("prompt".equals(c3.getPrompt()), "c3 prompt");
        check(c3.getMessageType() == javax.security.auth.callback.ConfirmationCallback.ERROR, "c3 messageType");
        check(c3.getOptionType() == javax.security.auth.callback.ConfirmationCallback.OK_CANCEL_OPTION, "c3 optionType");
        check(c3.getDefaultOption() == javax.security.auth.callback.ConfirmationCallback.CANCEL, "c3 defaultOption");

        // Constructor 4: (prompt, messageType, options[], defaultOption).
        javax.security.auth.callback.ConfirmationCallback c4 =
            new javax.security.auth.callback.ConfirmationCallback(
                "prompt", javax.security.auth.callback.ConfirmationCallback.INFORMATION,
                new String[]{"Retry", "Skip"}, 0);
        check("prompt".equals(c4.getPrompt()), "c4 prompt");
        check(c4.getOptionType() == javax.security.auth.callback.ConfirmationCallback.UNSPECIFIED_OPTION, "c4 optionType");
        check(Arrays.equals(new String[]{"Retry", "Skip"}, c4.getOptions()), "c4 options");
        check(c4.getDefaultOption() == 0, "c4 defaultOption");

        // Selection round-trip.
        check(c1.getSelectedIndex() == 0, "selected index defaults to 0");
        c1.setSelectedIndex(javax.security.auth.callback.ConfirmationCallback.YES);
        check(c1.getSelectedIndex() == javax.security.auth.callback.ConfirmationCallback.YES, "setSelectedIndex/getSelectedIndex");
        c4.setSelectedIndex(1);
        check(c4.getSelectedIndex() == 1, "custom options selection round-trip");

        // Validation errors -> IllegalArgumentException.
        boolean t;
        // invalid messageType
        t = false;
        try { new javax.security.auth.callback.ConfirmationCallback(99, 0, 0); } catch (IllegalArgumentException e) { t = true; }
        check(t, "IAE for invalid messageType");
        // invalid optionType
        t = false;
        try { new javax.security.auth.callback.ConfirmationCallback(0, 5, 0); } catch (IllegalArgumentException e) { t = true; }
        check(t, "IAE for invalid optionType");
        // invalid defaultOption for YES_NO_OPTION (OK not allowed)
        t = false;
        try { new javax.security.auth.callback.ConfirmationCallback(0, javax.security.auth.callback.ConfirmationCallback.YES_NO_OPTION, javax.security.auth.callback.ConfirmationCallback.OK); }
        catch (IllegalArgumentException e) { t = true; }
        check(t, "IAE for defaultOption not matching optionType");
        // null / empty options
        for (String[] o : new String[][]{null, new String[0]}) {
            t = false;
            try { new javax.security.auth.callback.ConfirmationCallback(0, o, 0); } catch (IllegalArgumentException e) { t = true; }
            check(t, "IAE for options=" + (o == null ? "null" : "empty"));
        }
        // null / empty option element
        for (String[] o : new String[][]{new String[]{null}, new String[]{""}}) {
            t = false;
            try { new javax.security.auth.callback.ConfirmationCallback(0, o, 0); } catch (IllegalArgumentException e) { t = true; }
            check(t, "IAE for null/empty option element");
        }
        // defaultOption out of range
        t = false;
        try { new javax.security.auth.callback.ConfirmationCallback(0, new String[]{"A", "B"}, 2); } catch (IllegalArgumentException e) { t = true; }
        check(t, "IAE for defaultOption out of range");
        // null / empty prompt in prompt constructors
        for (String p : new String[]{null, ""}) {
            t = false;
            try { new javax.security.auth.callback.ConfirmationCallback(p, 0, 0, 0); } catch (IllegalArgumentException e) { t = true; }
            check(t, "IAE for prompt=" + (p == null ? "null" : "empty"));
            t = false;
            try { new javax.security.auth.callback.ConfirmationCallback(p, 0, new String[]{"A"}, 0); } catch (IllegalArgumentException e) { t = true; }
            check(t, "IAE for prompt (options ctor)=" + (p == null ? "null" : "empty"));
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
