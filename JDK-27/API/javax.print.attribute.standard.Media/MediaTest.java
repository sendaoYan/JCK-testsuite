/*
 * @test
 * @summary javax.print.attribute.standard.Media conformance: white-box structure + black-box behavior.
 * @run main MediaTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class MediaTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.standard.Media");
        check(Modifier.isPublic(c.getModifiers()) && Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "public abstract class");
        check(EnumSyntax.class.isAssignableFrom(c), "extends EnumSyntax");
        check(DocAttribute.class.isAssignableFrom(c) && PrintRequestAttribute.class.isAssignableFrom(c) && PrintJobAttribute.class.isAssignableFrom(c), "implements interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Media m = MediaName.ISO_A4_WHITE;
        check(m.getCategory() == Media.class, "getCategory");
        check("media".equals(m.getName()), "getName");
        check(m.getValue() >= 0, "value non-negative");
        check(m.clone() == m, "EnumSyntax clone returns singleton");
        check(m.hashCode() == m.getValue(), "hashCode == value");
        check(m.equals(m) && !m.equals(MediaName.NA_LETTER_WHITE), "identity equals");
        check(m.toString() != null && m.toString().length() > 0, "toString non-empty");
        MediaSizeName n = MediaSizeName.ISO_A4;
        check(Media.class.isInstance(n) && "media".equals(n.getName()), "subclass is-a Media");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
