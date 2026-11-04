/*
 * @test
 * @summary javax.security.sasl.RealmCallback conformance: white-box structure + black-box behavior.
 * @run main RealmCallbackTest
 */

import java.lang.reflect.*;
import javax.security.sasl.RealmCallback;

public class RealmCallbackTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.security.sasl.RealmCallback");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass().getName().equals("javax.security.auth.callback.TextInputCallback"), "extends TextInputCallback");
        check(javax.security.auth.callback.Callback.class.isAssignableFrom(c), "implements Callback");
        check(java.io.Serializable.class.isAssignableFrom(c), "implements Serializable");
        boolean strArg = false, twoStr = false;
        for (Constructor<?> k : c.getConstructors()) {
            Class<?>[] p = k.getParameterTypes();
            if (p.length == 1 && p[0] == String.class) strArg = true;
            if (p.length == 2 && p[0] == String.class && p[1] == String.class) twoStr = true;
        }
        check(strArg, "String constructor");
        check(twoStr, "String,String constructor");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        RealmCallback c = new RealmCallback("prompt");
        check("prompt".equals(c.getPrompt()), "getPrompt");
        check(c.getDefaultText() == null, "single-arg default null");
        check(c.getText() == null, "initial text null");
        c.setText("REALM");
        check("REALM".equals(c.getText()), "getText after set");

        RealmCallback d = new RealmCallback("p", "dflt");
        check("p".equals(d.getPrompt()), "getPrompt two-arg");
        check("dflt".equals(d.getDefaultText()), "getDefaultText two-arg");
        check(d.getText() == null, "two-arg initial text null");

        try { new RealmCallback((String) null); check(false, "null prompt should throw"); }
        catch (IllegalArgumentException e) { check(true, "null prompt throws IllegalArgumentException"); }
        try { new RealmCallback(""); check(false, "empty prompt should throw"); }
        catch (IllegalArgumentException e) { check(true, "empty prompt throws IllegalArgumentException"); }
        try { new RealmCallback("p", (String) null); check(false, "null default should throw"); }
        catch (IllegalArgumentException e) { check(true, "null default throws IllegalArgumentException"); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
