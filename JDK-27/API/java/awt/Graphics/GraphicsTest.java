/*
 * @test
 * @summary java.awt.Graphics conformance: white-box structure + black-box behavior.
 * @run main GraphicsTest
 */

import java.lang.reflect.*;

public class GraphicsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.Graphics", false, GraphicsTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.Graphics");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public class");
        check(Modifier.isAbstract(m), "abstract class");
        check(!c.isInterface(), "not interface");
        check(c.getEnumConstants() == null, "non-enum");

        check(c.getMethod("create").getReturnType() == java.awt.Graphics.class, "create() returns Graphics");
        check(c.getMethod("getColor").getReturnType() == java.awt.Color.class, "getColor returns Color");
        check(c.getMethod("setColor", java.awt.Color.class).getReturnType() == void.class, "setColor returns void");
        check(c.getMethod("getFont").getReturnType() == java.awt.Font.class, "getFont returns Font");
        check(c.getMethod("getClipBounds").getReturnType() == java.awt.Rectangle.class,
              "getClipBounds returns Rectangle");
        for (String name : new String[] {"create", "translate", "setPaintMode", "setXORMode",
                "getFontMetrics", "clipRect", "setClip", "getClip", "copyArea", "drawLine",
                "fillRect", "drawRect", "clearRect", "drawRoundRect", "fillRoundRect",
                "draw3DRect", "fill3DRect", "drawOval", "fillOval", "drawArc", "fillArc",
                "drawPolyline", "drawPolygon", "fillPolygon", "drawString", "drawChars",
                "drawBytes", "drawImage", "dispose", "toString", "getClipRect", "hitClip",
                "getClipBounds"})
            check(java.util.Arrays.stream(c.getMethods()).anyMatch(mm -> mm.getName().equals(name)), "method present: " + name);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        java.awt.image.BufferedImage img =
            new java.awt.image.BufferedImage(16, 16, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics g = img.createGraphics();

        // color
        g.setColor(java.awt.Color.RED);
        check(g.getColor().equals(java.awt.Color.RED), "setColor/getColor round-trip");
        check(g.getColor() == java.awt.Color.RED, "getColor returns the identical constant instance");

        // font
        java.awt.Font f = new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12);
        g.setFont(f);
        check(g.getFont().equals(f), "setFont/getFont round-trip");
        check(g.getFontMetrics(f).getFont().equals(f), "getFontMetrics(Font) uses that font");

        // clip
        g.setClip(2, 3, 5, 6);
        check(g.getClipBounds().equals(new java.awt.Rectangle(2, 3, 5, 6)), "setClip(int,int,int,int)");
        check(g.getClipRect().equals(new java.awt.Rectangle(2, 3, 5, 6)), "getClipRect matches");
        java.awt.Shape clipShape = g.getClip();
        check(clipShape != null && clipShape.getBounds().equals(new java.awt.Rectangle(2, 3, 5, 6)),
              "getClip Shape bounds");
        check(g.hitClip(3, 4, 2, 2), "hitClip inside");
        check(!g.hitClip(20, 20, 1, 1), "hitClip outside");
        java.awt.Rectangle into = new java.awt.Rectangle();
        java.awt.Rectangle ret = g.getClipBounds(into);
        check(ret == into && into.equals(new java.awt.Rectangle(2, 3, 5, 6)),
              "getClipBounds(Rectangle) fills and returns argument");
        g.clipRect(2, 3, 3, 3);
        check(g.getClipBounds().equals(new java.awt.Rectangle(2, 3, 3, 3)), "clipRect intersects clip");
        g.setClip(0, 0, 16, 16);

        // translate via a child graphics
        java.awt.Graphics tg = img.createGraphics();
        tg.setColor(java.awt.Color.RED);
        tg.fillRect(0, 0, 1, 1);
        check(img.getRGB(0, 0) == 0xffff0000, "fillRect at origin without translate");
        tg.translate(5, 5);
        tg.fillRect(0, 0, 1, 1);
        check(img.getRGB(5, 5) == 0xffff0000, "translate shifts subsequent drawing");
        check(img.getRGB(0, 0) == 0xffff0000, "original pixel still drawn");
        tg.dispose();

        // fillRect + pixel verification
        g.setColor(java.awt.Color.BLUE);
        g.fillRect(1, 1, 4, 4);
        check(img.getRGB(2, 2) == 0xff0000ff, "fillRect fills interior pixel");
        check(img.getRGB(0, 0) == 0xffff0000, "fillRect leaves unrelated pixel");
        check(img.getRGB(6, 6) == 0, "pixel beyond fillRect untouched");

        // clearRect (fills with the graphics background color)
        java.awt.Graphics2D g2 = (java.awt.Graphics2D) g;
        g2.setBackground(new java.awt.Color(0, 0, 0, 0));
        g.clearRect(1, 1, 4, 4);
        check(img.getRGB(2, 2) == 0, "clearRect clears with transparent background");

        // drawRect outline
        g.setColor(java.awt.Color.GREEN);
        g.drawRect(1, 1, 4, 4);
        check(img.getRGB(1, 1) == 0xff00ff00, "drawRect draws border pixel");
        check(img.getRGB(3, 3) == 0, "drawRect does not fill interior");

        // drawLine
        g.clearRect(0, 0, 16, 16);
        g.drawLine(0, 0, 3, 0);
        check(img.getRGB(1, 0) == 0xff00ff00, "drawLine draws along the line");

        // shape drawing primitives (callable, no exceptions)
        g.setColor(java.awt.Color.RED);
        g.drawOval(0, 0, 8, 8);
        g.fillOval(0, 0, 8, 8);
        g.drawRoundRect(0, 0, 8, 8, 4, 4);
        g.fillRoundRect(0, 0, 8, 8, 4, 4);
        g.draw3DRect(0, 0, 8, 8, true);
        g.fill3DRect(0, 0, 8, 8, false);
        g.drawArc(0, 0, 8, 8, 0, 90);
        g.fillArc(0, 0, 8, 8, 0, 90);
        g.drawPolyline(new int[] {0, 4, 8}, new int[] {0, 8, 0}, 3);
        g.drawPolygon(new int[] {0, 4, 8}, new int[] {0, 8, 0}, 3);
        g.fillPolygon(new int[] {0, 4, 8}, new int[] {0, 8, 0}, 3);
        java.awt.Polygon poly = new java.awt.Polygon(new int[] {0, 4, 8}, new int[] {0, 8, 0}, 3);
        g.drawPolygon(poly);
        g.fillPolygon(poly);
        check(true, "shape drawing primitives callable");

        // text drawing primitives
        g.drawString("Hi", 2, 8);
        g.drawChars(new char[] {'A', 'B'}, 0, 2, 2, 12);
        g.drawBytes(new byte[] {'C', 'D'}, 0, 2, 2, 14);
        check(true, "text drawing primitives callable");

        // paint modes
        g.setXORMode(java.awt.Color.RED);
        g.setPaintMode();
        check(g.getColor().equals(java.awt.Color.RED), "color retained through XOR->paint modes");

        // create() copies state
        g.setColor(java.awt.Color.BLUE);
        java.awt.Graphics child = g.create();
        check(child != null, "create() non-null");
        check(child.getColor().equals(java.awt.Color.BLUE), "create() inherits color");
        child.setColor(java.awt.Color.RED);
        check(g.getColor().equals(java.awt.Color.BLUE), "child color change does not affect parent");
        java.awt.Graphics child2 = g.create(0, 0, 8, 8);
        check(child2 != null, "create(x,y,w,h) non-null");
        child.dispose();
        child2.dispose();

        // dispose
        g.dispose();
        check(true, "dispose callable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
