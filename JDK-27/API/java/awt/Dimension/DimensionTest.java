/*
 * @test
 * @summary java.awt.Dimension conformance: white-box structure + black-box behavior.
 * @run main DimensionTest
 */

import java.lang.reflect.*;

public class DimensionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.Dimension", false, DimensionTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.Dimension");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) || Modifier.isProtected(m) || c.isMemberClass(),
              "public/protected/member");
        check(c.getEnumConstants() == null, "non-enum returns null constants");
        check(c.getDeclaredConstructors() != null, "constructors array non-null");
        check(c.getDeclaredMethods() != null, "methods array non-null");
        check(c.getDeclaredFields() != null, "fields array non-null");
        check(c.getSuperclass() != null, "class superclass non-null");
        check(c.getSuperclass().getName().equals("java.awt.geom.Dimension2D"),
              "extends java.awt.geom.Dimension2D");
        check(java.io.Serializable.class.isAssignableFrom(c), "implements Serializable");
        check(!Modifier.isAbstract(m), "concrete class");
        // public mutable fields
        check(c.getField("width").getType() == int.class, "public int width");
        check(c.getField("height").getType() == int.class, "public int height");
        // key API surface (overloads: setSize(int,int)/setSize(Dimension)/setSize(double,double))
        for (String name : new String[] {"getWidth", "getHeight", "setSize", "getSize",
                "equals", "hashCode", "toString"}) {
            boolean found = java.util.Arrays.stream(c.getMethods())
                    .anyMatch(mm -> mm.getName().equals(name));
            check(found, "method surface present: " + name);
        }
        check(c.getConstructor() != null, "ctor ()");
        check(c.getConstructor(int.class, int.class) != null, "ctor (int,int)");
        check(c.getConstructor(java.awt.Dimension.class) != null, "ctor (Dimension)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // constructors + defaults
        java.awt.Dimension d0 = new java.awt.Dimension();
        check(d0.width == 0 && d0.height == 0, "default is 0x0, got " + d0);
        java.awt.Dimension d = new java.awt.Dimension(3, 4);
        check(d.width == 3 && d.height == 4, "(3,4) ctor");
        java.awt.Dimension copy = new java.awt.Dimension(d);
        check(copy.width == 3 && copy.height == 4, "copy ctor");
        copy.width = 99;
        check(d.width == 3, "copy ctor is independent");

        // Dimension2D double view
        check(d.getWidth() == 3.0 && d.getHeight() == 4.0, "getWidth/getHeight doubles");

        // setSize int / Dimension / double
        d.setSize(10, 20);
        check(d.width == 10 && d.height == 20, "setSize(int,int)");
        d.setSize(new java.awt.Dimension(7, 8));
        check(d.width == 7 && d.height == 8, "setSize(Dimension)");
        java.awt.Dimension other = new java.awt.Dimension(1, 1);
        d.setSize(other);
        check(d.width == 1 && d.height == 1, "setSize copies values");
        other.width = 50;
        check(d.width == 1, "setSize does not alias the argument");
        d.setSize(10.7, -3.9);
        check(d.width == 11, "setSize(double,double) rounds up width: " + d.width);
        check(d.height == -3, "setSize(double,double) rounds up height toward zero: " + d.height);
        d.setSize(0.0, 0.0);
        check(d.width == 0 && d.height == 0, "setSize zero");

        // getSize returns a new independent instance
        d.setSize(5, 6);
        java.awt.Dimension got = d.getSize();
        check(got.width == 5 && got.height == 6, "getSize values");
        got.width = 77;
        check(d.width == 5, "getSize returns independent object");

        // equals / hashCode contract
        java.awt.Dimension a = new java.awt.Dimension(11, 22);
        java.awt.Dimension b = new java.awt.Dimension(11, 22);
        java.awt.Dimension e = new java.awt.Dimension(22, 11);
        check(a.equals(b), "equal dimensions");
        check(a.hashCode() == b.hashCode(), "equal objects have equal hashCodes");
        check(!a.equals(e), "swapped w/h not equal");
        check(!a.equals(null), "not equal to null");
        check(!a.equals("11x22"), "not equal to unrelated type");
        check(a.equals(a), "equals reflexive");

        // hashCode formula stability across equal-width/height pairs
        check(new java.awt.Dimension(0, 0).hashCode() == new java.awt.Dimension(0, 0).hashCode(),
              "0x0 hashCode stable");

        // toString format
        check(d.toString().equals("java.awt.Dimension[width=5,height=6]"),
              "toString format: " + d);

        // combination chain: construct -> resize -> read back -> compare
        java.awt.Dimension box = new java.awt.Dimension();
        box.setSize(2.5, 2.5);
        java.awt.Dimension expect = new java.awt.Dimension(3, 3);
        check(box.equals(expect), "chain resize equals expected rounded 3x3");
        check(box.toString().contains("width=3"), "toString reflects mutation");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
