/*
 * @test
 * @summary javax.print.attribute.standard.MediaTray conformance: white-box structure + black-box behavior.
 * @run main MediaTrayTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class MediaTrayTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static final class MT extends MediaTray {
        MT(int v) { super(v); }
        String[] stringTable() { return super.getStringTable(); }
        javax.print.attribute.EnumSyntax[] enumValueTable() { return super.getEnumValueTable(); }
    }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.standard.MediaTray");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface() && !Modifier.isFinal(c.getModifiers()), "public class");
        check(Media.class.isAssignableFrom(c), "extends Media");
        check(Attribute.class.isAssignableFrom(c), "implements Attribute");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(MediaTray.TOP.getCategory() == Media.class, "getCategory");
        check("media".equals(MediaTray.TOP.getName()), "getName");
        check(MediaTray.TOP.getValue() == 0, "TOP value == 0");
        check("top".equals(MediaTray.TOP.toString()), "TOP toString");
        check(MediaTray.MIDDLE.getValue() == 1, "MIDDLE value == 1");
        check("middle".equals(MediaTray.MIDDLE.toString()), "MIDDLE toString");
        check(MediaTray.BOTTOM.getValue() == 2, "BOTTOM value == 2");
        check("bottom".equals(MediaTray.BOTTOM.toString()), "BOTTOM toString");
        check(MediaTray.ENVELOPE.getValue() == 3, "ENVELOPE value == 3");
        check("envelope".equals(MediaTray.ENVELOPE.toString()), "ENVELOPE toString");
        check(MediaTray.MANUAL.getValue() == 4, "MANUAL value == 4");
        check("manual".equals(MediaTray.MANUAL.toString()), "MANUAL toString");
        check(MediaTray.LARGE_CAPACITY.getValue() == 5, "LARGE_CAPACITY value == 5");
        check("large-capacity".equals(MediaTray.LARGE_CAPACITY.toString()), "LARGE_CAPACITY toString");
        check(MediaTray.MAIN.getValue() == 6, "MAIN value == 6");
        check("main".equals(MediaTray.MAIN.toString()), "MAIN toString");
        check(MediaTray.SIDE.getValue() == 7, "SIDE value == 7");
        check("side".equals(MediaTray.SIDE.toString()), "SIDE toString");
        check(MediaTray.TOP.clone() == MediaTray.TOP, "clone returns singleton");
        check(MediaTray.TOP.hashCode() == 0, "hashCode == value");
        check(MediaTray.TOP.equals(MediaTray.TOP), "equals self");
        check(!MediaTray.TOP.equals(MediaTray.MIDDLE), "distinct constants unequal");
        check(!MediaTray.TOP.equals(null) && !MediaTray.TOP.equals("x"), "not equals null/foreign");

        // direct protected-method invocation through a minimal subclass (super constructor + super getStringTable/getEnumValueTable)
        MT m = new MT(0);
        String[] st = m.stringTable();
        check(st != null && st.length == 8, "getStringTable length");
        check("top".equals(st[0]), "getStringTable first entry");
        javax.print.attribute.EnumSyntax[] evt = m.enumValueTable();
        check(evt != null && evt.length == 8, "getEnumValueTable length");
        check(evt[0] == MediaTray.TOP, "getEnumValueTable first entry");
        check(m.getValue() == 0 && "top".equals(m.toString()), "subclass ctor sets value 0");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
