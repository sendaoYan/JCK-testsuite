/*
 * @test
 * @summary javax.print.attribute.standard.MediaSize.NA conformance: white-box structure + black-box behavior.
 * @run main NATest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class NATest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.standard.MediaSize$NA");
        check(Modifier.isPublic(c.getModifiers()) && Modifier.isFinal(c.getModifiers()) && !c.isInterface(), "public final class");
        int n = 0; for (Field f : c.getDeclaredFields()) if (Modifier.isPublic(f.getModifiers()) && Modifier.isStatic(f.getModifiers()) && f.getType() == MediaSize.class) n++;
        check(n > 0, "has MediaSize constants");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        MediaSize s = MediaSize.NA.LETTER;
        check(s != null, "constant non-null");
        check(s.getCategory() == MediaSize.class && "media-size".equals(s.getName()), "getCategory/getName");
        check(s.getMediaSizeName() != null, "getMediaSizeName non-null");
        check(s.getX(MediaSize.INCH) == 8.5f && s.getY(MediaSize.INCH) == 11.0f, "MediaSize.NA.LETTER exact size");
        check("8.5x11.0 in".equals(s.toString(MediaSize.INCH, "in")), "MediaSize.NA.LETTER toString");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
