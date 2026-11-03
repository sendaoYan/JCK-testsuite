/*
 * @test
 * @summary javax.swing.UIDefaults conformance: white-box structure + black-box behavior.
 * @run main UIDefaultsTest
 */

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.lang.reflect.Modifier;
import java.util.Locale;

import javax.swing.ImageIcon;
import javax.swing.UIDefaults;

public class UIDefaultsTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.UIDefaults");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.util.Hashtable"), "superclass");
        check(!Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        UIDefaults d = new UIDefaults();
        d.put("int", Integer.valueOf(7));
        d.put("bool", Boolean.TRUE);
        d.put("str", "hello");
        d.put("color", Color.RED);
        d.put("font", new Font("Dialog", 0, 12));
        d.put("icon", new ImageIcon());
        d.put("insets", new Insets(1, 2, 3, 4));
        d.put("dim", new Dimension(5, 6));

        check(d.getInt("int") == 7, "getInt");
        check(d.getBoolean("bool"), "getBoolean");
        check("hello".equals(d.getString("str")), "getString");
        check(Color.RED.equals(d.getColor("color")), "getColor");
        check(d.getFont("font") != null, "getFont");
        check(d.getIcon("icon") != null, "getIcon");
        check(d.getInsets("insets").equals(new Insets(1, 2, 3, 4)), "getInsets");
        check(d.getDimension("dim").equals(new Dimension(5, 6)), "getDimension");
        check(d.get("missing") == null, "missing null");

        d.putDefaults(new Object[] { "a", Integer.valueOf(1), "b", "two" });
        check(d.getInt("a") == 1, "putDefaults int");
        check("two".equals(d.getString("b")), "putDefaults string");

        UIDefaults arr = new UIDefaults(new Object[] { "k", "v" });
        check("v".equals(arr.getString("k")), "array ctor");

        d.setDefaultLocale(Locale.ENGLISH);
        check(Locale.ENGLISH.equals(d.getDefaultLocale()), "default locale");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
