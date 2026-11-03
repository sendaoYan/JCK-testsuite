/*
 * @test
 * @summary javax.print.attribute.standard.MediaPrintableArea conformance: white-box structure + black-box behavior.
 * @run main MediaPrintableAreaTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class MediaPrintableAreaTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.standard.MediaPrintableArea");
        check(Modifier.isPublic(c.getModifiers()) && Modifier.isFinal(c.getModifiers()) && !c.isInterface(), "public final class");
        check(DocAttribute.class.isAssignableFrom(c) && PrintRequestAttribute.class.isAssignableFrom(c) && PrintJobAttribute.class.isAssignableFrom(c), "implements interfaces");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        MediaPrintableArea p = new MediaPrintableArea(1, 1, 2, 2, MediaPrintableArea.INCH);
        check(p.getX(MediaPrintableArea.INCH) == 1.0f && p.getY(MediaPrintableArea.INCH) == 1.0f, "getX/getY");
        check(p.getWidth(MediaPrintableArea.INCH) == 2.0f && p.getHeight(MediaPrintableArea.INCH) == 2.0f, "getWidth/getHeight");
        float[] a = p.getPrintableArea(MediaPrintableArea.INCH);
        check(a.length == 4 && a[0] == 1.0f && a[1] == 1.0f && a[2] == 2.0f && a[3] == 2.0f, "getPrintableArea");
        check("(1.0,1.0)->(2.0,2.0)".equals(p.toString(MediaPrintableArea.INCH, null)), "toString(INCH,null)");
        check("(25.4,25.4)->(50.8,50.8)mm".equals(p.toString()), "toString default mm");
        check("media-printable-area".equals(p.getName()) && p.getCategory() == MediaPrintableArea.class, "getCategory/getName");
        check(p.equals(new MediaPrintableArea(1, 1, 2, 2, MediaPrintableArea.INCH)), "equals");
        check(!p.equals(new MediaPrintableArea(1, 1, 3, 2, MediaPrintableArea.INCH)), "different unequal");
        check(!p.equals(null) && !p.equals("x"), "not equals null/foreign");
        check(p.hashCode() == new MediaPrintableArea(1, 1, 2, 2, MediaPrintableArea.INCH).hashCode(), "hashCode consistent");
        boolean threw = false; try { new MediaPrintableArea(1, 1, 0, 2, MediaPrintableArea.INCH); } catch (IllegalArgumentException e) { threw = true; }
        check(threw, "zero width IAE");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
