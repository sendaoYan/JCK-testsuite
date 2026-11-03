/*
 * @test
 * @summary javax.swing.text.TabStop conformance: white-box structure + black-box behavior.
 * @run main TabStopTest
 */

import java.lang.reflect.Modifier;
import javax.swing.text.TabStop;

public class TabStopTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.TabStop");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.io.Serializable"), "interface 0");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // Alignment constants.
        check(TabStop.ALIGN_LEFT == 0, "ALIGN_LEFT");
        check(TabStop.ALIGN_RIGHT == 1, "ALIGN_RIGHT");
        check(TabStop.ALIGN_CENTER == 2, "ALIGN_CENTER");
        check(TabStop.ALIGN_DECIMAL == 4, "ALIGN_DECIMAL");
        check(TabStop.ALIGN_BAR == 5, "ALIGN_BAR");
        // Leader constants.
        check(TabStop.LEAD_NONE == 0, "LEAD_NONE");
        check(TabStop.LEAD_DOTS == 1, "LEAD_DOTS");
        check(TabStop.LEAD_HYPHENS == 2, "LEAD_HYPHENS");
        check(TabStop.LEAD_UNDERLINE == 3, "LEAD_UNDERLINE");
        check(TabStop.LEAD_THICKLINE == 4, "LEAD_THICKLINE");
        check(TabStop.LEAD_EQUALS == 5, "LEAD_EQUALS");

        // Single-arg constructor defaults to ALIGN_LEFT / LEAD_NONE.
        TabStop left = new TabStop(10f);
        check(left.getPosition() == 10f, "default position");
        check(left.getAlignment() == TabStop.ALIGN_LEFT, "default alignment left");
        check(left.getLeader() == TabStop.LEAD_NONE, "default leader none");

        // Full constructor roundtrip.
        TabStop center = new TabStop(20f, TabStop.ALIGN_CENTER, TabStop.LEAD_DOTS);
        check(center.getPosition() == 20f, "center position");
        check(center.getAlignment() == TabStop.ALIGN_CENTER, "center alignment");
        check(center.getLeader() == TabStop.LEAD_DOTS, "center leader dots");

        TabStop decimal = new TabStop(5.5f, TabStop.ALIGN_DECIMAL, TabStop.LEAD_UNDERLINE);
        check(decimal.getPosition() == 5.5f, "decimal position");
        check(decimal.getAlignment() == TabStop.ALIGN_DECIMAL, "decimal alignment");
        check(decimal.getLeader() == TabStop.LEAD_UNDERLINE, "decimal leader underline");

        // equals: value-based, reflexive, symmetric, discriminates each field.
        check(left.equals(left), "equals reflexive");
        check(left.equals(new TabStop(10f)), "equals same value");
        check(new TabStop(10f).equals(left), "equals symmetric");
        check(!left.equals(new TabStop(10.5f)), "equals different position");
        check(!center.equals(new TabStop(20f, TabStop.ALIGN_LEFT, TabStop.LEAD_DOTS)), "equals different alignment");
        check(!center.equals(new TabStop(20f, TabStop.ALIGN_CENTER, TabStop.LEAD_NONE)), "equals different leader");
        check(!left.equals(null), "equals null false");
        check(!left.equals("tab @10.0"), "equals non-TabStop false");

        // hashCode: equal objects have equal hash codes; matches documented formula.
        check(left.hashCode() == new TabStop(10f).hashCode(), "equal objects equal hash");
        check(left.hashCode() == 10, "hashCode == align^leader^round(pos)");

        // toString.
        check(left.toString().equals("tab @10.0"), "toString left");
        check(center.toString().equals("center tab @20.0 (w/leaders)"), "toString center+dots");
        check(new TabStop(30f, TabStop.ALIGN_RIGHT, TabStop.LEAD_NONE).toString().equals("right tab @30.0"), "toString right");
        check(new TabStop(40f, TabStop.ALIGN_DECIMAL, TabStop.LEAD_HYPHENS).toString().equals("decimal tab @40.0 (w/leaders)"), "toString decimal");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
