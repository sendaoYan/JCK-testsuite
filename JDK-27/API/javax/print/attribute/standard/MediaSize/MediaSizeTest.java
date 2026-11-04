/*
 * @test
 * @summary javax.print.attribute.standard.MediaSize conformance: white-box structure + black-box behavior.
 * @run main MediaSizeTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class MediaSizeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.standard.MediaSize");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface(), "public class");
        check(Size2DSyntax.class.isAssignableFrom(c), "extends Size2DSyntax");
        check(Attribute.class.isAssignableFrom(c), "implements Attribute");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        MediaSize a4 = MediaSize.ISO.A4;
        check(a4.getX(MediaSize.MM) == 210.0f && a4.getY(MediaSize.MM) == 297.0f, "A4 mm");
        check("210000x297000 um".equals(a4.toString()), "A4 um toString");
        check("210.0x297.0 mm".equals(a4.toString(MediaSize.MM, "mm")), "A4 toString(MM)");
        check(a4.getCategory() == MediaSize.class && "media-size".equals(a4.getName()), "getCategory/getName");
        check(MediaSizeName.ISO_A4.equals(a4.getMediaSizeName()), "getMediaSizeName");
        check(MediaSize.getMediaSizeForName(MediaSizeName.ISO_A4).equals(a4), "getMediaSizeForName roundtrip");
        check(MediaSizeName.ISO_A4.equals(MediaSize.findMedia(210, 297, MediaSize.MM)), "findMedia exact");
        MediaSize custom = new MediaSize(8.5f, 11.0f, MediaSize.INCH);
        check(custom.getX(MediaSize.INCH) == 8.5f && custom.getY(MediaSize.INCH) == 11.0f, "float ctor");
        check(custom.equals(new MediaSize(8.5f, 11.0f, MediaSize.INCH)), "float ctor equals");
        check(new MediaSize(210, 297, MediaSize.MM).equals(MediaSize.ISO.A4), "int mm ctor equals ISO.A4");
        check(!custom.equals(null) && !custom.equals("x"), "not equals null/foreign");
        boolean threw = false; try { new MediaSize(-1f, 11f, MediaSize.INCH); } catch (IllegalArgumentException e) { threw = true; }
        check(threw, "negative size IAE");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
