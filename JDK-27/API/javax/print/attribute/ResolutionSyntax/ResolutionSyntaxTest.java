/*
 * @test
 * @summary javax.print.attribute.ResolutionSyntax conformance: white-box structure + black-box behavior.
 * @run main ResolutionSyntaxTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class ResolutionSyntaxTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.ResolutionSyntax");
        check(Modifier.isPublic(c.getModifiers()) && Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "public abstract class");
        check(java.io.Serializable.class.isAssignableFrom(c) && Cloneable.class.isAssignableFrom(c), "Serializable+Cloneable");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ResolutionSyntax r = new PrinterResolution(300, 600, ResolutionSyntax.DPI);
        check(r.getCrossFeedResolution(ResolutionSyntax.DPI) == 300 && r.getFeedResolution(ResolutionSyntax.DPI) == 600, "getCrossFeed/getFeed");
        check("30000x60000 dphi".equals(r.toString()), "toString");
        check(r.lessThanOrEquals(new PrinterResolution(600, 600, ResolutionSyntax.DPI)), "lessThanOrEquals");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
