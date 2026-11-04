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
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Part 3: contract deepening via BufferedImage.createGraphics() (headless-safe) ----
    static final int WHITE = 0xFFFFFFFF, BLACK = 0xFF000000;
    static int px(java.awt.image.BufferedImage bi, int x, int y) { return bi.getRGB(x, y); }
    static java.awt.image.BufferedImage blank() {
        java.awt.image.BufferedImage bi = new java.awt.image.BufferedImage(20, 20, java.awt.image.BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g = bi.createGraphics();
        g.setColor(java.awt.Color.WHITE); g.fillRect(0, 0, 20, 20); g.dispose();
        return bi;
    }
    static void testContract() {
        java.awt.image.BufferedImage bi = blank();
        java.awt.Graphics g = bi.createGraphics();
        check(g != null, "createGraphics returns context");
        // color round-trip
        g.setColor(java.awt.Color.BLACK);
        check(g.getColor().equals(java.awt.Color.black), "setColor/getColor round-trip");
        // drawLine: exactly the line pixels, neighbors untouched
        g.drawLine(5, 5, 15, 5);
        check(px(bi, 5, 5) == BLACK && px(bi, 10, 5) == BLACK && px(bi, 15, 5) == BLACK, "drawLine endpoints and middle black");
        check(px(bi, 5, 4) == WHITE && px(bi, 16, 5) == WHITE, "adjacent pixels untouched");
        // drawRect: 1px outline
        java.awt.image.BufferedImage bi2 = blank();
        java.awt.Graphics g2 = bi2.createGraphics();
        g2.setColor(java.awt.Color.RED);
        g2.drawRect(2, 10, 6, 4);
        check(px(bi2, 2, 10) == 0xFFFF0000 && px(bi2, 8, 14) == 0xFFFF0000
              && px(bi2, 2, 14) == 0xFFFF0000 && px(bi2, 8, 10) == 0xFFFF0000, "drawRect outline corners");
        check(px(bi2, 3, 11) == WHITE, "drawRect interior untouched");
        // fillRect: fills width*height pixels, right/bottom exclusive
        java.awt.image.BufferedImage bi3 = blank();
        java.awt.Graphics g3 = bi3.createGraphics();
        g3.setColor(java.awt.Color.GREEN);
        g3.fillRect(10, 10, 5, 5);
        check(px(bi3, 10, 10) == 0xFF00FF00 && px(bi3, 14, 14) == 0xFF00FF00, "fillRect corners inside");
        check(px(bi3, 9, 9) == WHITE && px(bi3, 15, 15) == WHITE, "fillRect exclusive edges");
        // clearRect paints background color
        ((java.awt.Graphics2D) g3).setBackground(java.awt.Color.YELLOW);
        g3.clearRect(10, 10, 5, 5);
        check(px(bi3, 12, 12) == 0xFFFFFF00, "clearRect uses background");
        // copyArea
        java.awt.image.BufferedImage bi4 = blank();
        java.awt.Graphics g4 = bi4.createGraphics();
        g4.setColor(java.awt.Color.BLUE);
        g4.fillRect(0, 0, 2, 2);
        g4.copyArea(0, 0, 2, 2, 10, 10);
        check(px(bi4, 10, 10) == 0xFF0000FF && px(bi4, 11, 11) == 0xFF0000FF, "copyArea copies block");
        check(px(bi4, 12, 12) == WHITE, "copyArea target boundary exclusive");
        g4.dispose();
        // clip: setClip(Shape) + getClipBounds + clipRect intersection
        java.awt.image.BufferedImage bi5 = blank();
        java.awt.Graphics g5 = bi5.createGraphics();
        g5.setColor(java.awt.Color.BLACK);
        g5.setClip(new java.awt.Rectangle(5, 5, 5, 5));
        check(g5.getClipBounds() != null && g5.getClipBounds().equals(new java.awt.Rectangle(5, 5, 5, 5)),
              "setClip/getClipBounds round-trip");
        check(g5.getClip() instanceof java.awt.Rectangle, "getClip returns Shape");
        g5.fillRect(0, 0, 20, 20);
        check(px(bi5, 6, 6) == BLACK && px(bi5, 4, 4) == WHITE && px(bi5, 10, 10) == WHITE,
              "clip constrains fill to [5,10)");
        g5.clipRect(5, 5, 3, 3);
        check(g5.getClipBounds().equals(new java.awt.Rectangle(5, 5, 3, 3)), "clipRect intersects");
        g5.setClip(null);
        check(g5.getClip() == null, "setClip(null) clears");
        g5.dispose();
        // hitClip
        java.awt.image.BufferedImage bi6 = blank();
        java.awt.Graphics g6 = bi6.createGraphics();
        g6.setClip(new java.awt.Rectangle(0, 0, 5, 5));
        check(g6.hitClip(1, 1, 2, 2), "hitClip inside");
        check(!g6.hitClip(10, 10, 5, 5), "hitClip outside");
        g6.dispose();
        // translate affects subsequent drawing origin
        java.awt.image.BufferedImage bi7 = blank();
        java.awt.Graphics g7 = bi7.createGraphics();
        g7.setColor(java.awt.Color.BLACK);
        g7.translate(3, 4);
        g7.drawLine(0, 0, 0, 0);
        check(px(bi7, 3, 4) == BLACK && px(bi7, 2, 4) == WHITE, "translate shifts origin");
        g7.dispose();
        // font round-trip
        java.awt.Font f = new java.awt.Font(java.awt.Font.DIALOG, java.awt.Font.BOLD, 14);
        g.setFont(f);
        check(g.getFont().equals(f), "setFont/getFont round-trip");
        check(g.getFontMetrics() != null, "getFontMetrics() for current font");
        check(g.getFontMetrics(f) != null, "getFontMetrics(Font)");
        // create() derivation: state copied, independent afterwards
        java.awt.Graphics derived = g.create();
        derived.setColor(java.awt.Color.PINK);
        check(derived.getColor().equals(java.awt.Color.PINK), "derived color set");
        check(g.getColor().equals(java.awt.Color.BLACK), "original color unchanged after derived set");
        java.awt.Graphics derived2 = g.create(2, 3, 10, 10);
        check(derived2 != null, "create(x,y,w,h) with translate+clip");
        derived2.dispose();
        derived.dispose();
        // XOR mode: drawing twice restores original pixel
        java.awt.image.BufferedImage bi8 = blank();
        java.awt.Graphics g8 = bi8.createGraphics();
        g8.setXORMode(java.awt.Color.GREEN);
        g8.setColor(java.awt.Color.BLACK);
        g8.drawLine(0, 0, 0, 0);
        int once = px(bi8, 0, 0);
        check(once != WHITE, "XOR first draw changes pixel");
        g8.drawLine(0, 0, 0, 0);
        check(px(bi8, 0, 0) == WHITE, "XOR second identical draw restores pixel");
        g8.setPaintMode();
        g8.setColor(java.awt.Color.BLACK);
        g8.drawLine(1, 0, 1, 0);
        check(px(bi8, 1, 0) == BLACK, "setPaintMode restores overwrite");
        g8.dispose();
        // dispose twice is safe
        g.dispose();
        g.dispose();
        check(true, "dispose twice no exception");
        // toString mentions color/font
        java.awt.Graphics g9 = blank().createGraphics();
        check(g9.toString().length() > 0, "toString non-empty");
        g9.dispose();
    }

}
