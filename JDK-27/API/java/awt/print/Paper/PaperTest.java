/*
 * @test
 * @summary java.awt.print.Paper conformance: paper geometry (headless-safe):
 *          Letter defaults, setSize/setImageableArea with independent axes,
 *          negative/beyond-paper values stored verbatim, clone independence,
 *          non-serializability despite Cloneable.
 * @run main PaperTest
 */

import java.awt.print.Paper;

public class PaperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static String str(Paper p) {
        return p.getWidth() + "x" + p.getHeight() + " img " + p.getImageableX() + "," + p.getImageableY()
                + " " + p.getImageableWidth() + "x" + p.getImageableHeight();
    }

    public static void main(String[] args) throws Exception {
        // US Letter defaults.
        Paper p = new Paper();
        check(p.getWidth() == 612.0, "default width 612 (letter)");
        check(p.getHeight() == 792.0, "default height 792");
        check(p.getImageableX() == 72.0 && p.getImageableY() == 72.0, "default imageable origin 72,72 (1 inch)");
        check(p.getImageableWidth() == 468.0, "default imageable width 468");
        check(p.getImageableHeight() == 648.0, "default imageable height 648");

        // setSize changes extent only; imageable area is untouched.
        p.setSize(100, 200);
        check(p.getWidth() == 100.0 && p.getHeight() == 200.0, "setSize round-trip");
        check(p.getImageableX() == 72.0 && p.getImageableWidth() == 468.0,
                "setSize leaves imageable area alone");
        // Fractional sizes are accepted (points are doubles).
        p.setSize(210.5, 297.25);
        check(p.getWidth() == 210.5 && p.getHeight() == 297.25, "fractional sizes preserved");
        p.setSize(100, 200);

        // setImageableArea is independent of the paper size.
        p.setImageableArea(10, 20, 30, 40);
        check(p.getImageableX() == 10.0 && p.getImageableY() == 20.0, "imageable origin");
        check(p.getImageableWidth() == 30.0 && p.getImageableHeight() == 40.0, "imageable extent");
        check(p.getWidth() == 100.0 && p.getHeight() == 200.0, "imageable change leaves size alone");

        // Negative sizes are stored verbatim: no clamping anywhere.
        Paper n = new Paper();
        n.setSize(-5, -7);
        check(n.getWidth() == -5.0 && n.getHeight() == -7.0, "negative size stored verbatim");
        n.setImageableArea(-10, -20, -30, -40);
        check(n.getImageableX() == -10.0 && n.getImageableWidth() == -30.0,
                "negative imageable stored verbatim");
        // Imageable area larger than the page is accepted.
        Paper big = new Paper();
        big.setImageableArea(0, 0, 5000, 5000);
        check(big.getImageableWidth() == 5000.0 && big.getImageableHeight() == 5000.0,
                "imageable beyond page accepted");

        // Zero-extent degenerate paper.
        Paper z = new Paper();
        z.setSize(0, 0);
        check(z.getWidth() == 0.0 && z.getHeight() == 0.0, "zero size accepted");

        // clone(): a deep, independent copy.
        Paper c = new Paper();
        c.setSize(123, 456);
        c.setImageableArea(1, 2, 3, 4);
        Paper cl = (Paper) c.clone();
        check(cl.getClass() == Paper.class, "clone is a Paper");
        check(str(cl).equals(str(c)), "clone copies geometry: " + str(cl));
        cl.setSize(9, 9);
        cl.setImageableArea(8, 8, 8, 8);
        check(c.getWidth() == 123.0 && c.getImageableX() == 1.0, "clone mutation does not leak back");

        // equals/hashCode are identity-based (not value-based).
        Paper e1 = new Paper();
        Paper e2 = new Paper();
        e2.setSize(e1.getWidth(), e1.getHeight());
        e2.setImageableArea(e1.getImageableX(), e1.getImageableY(), e1.getImageableWidth(), e1.getImageableHeight());
        check(!e1.equals(e2), "equal-valued papers are not equals()");
        check(e1.equals(e1), "identity equals");

        // Not serializable despite being Cloneable.
        boolean notSerializable = false;
        try {
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
            oos.writeObject(new Paper());
            oos.close();
        } catch (java.io.NotSerializableException expected) {
            notSerializable = true;
        }
        check(notSerializable, "Paper is not serializable");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
