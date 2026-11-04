/*
 * @test
 * @summary java.awt.Graphics conformance: abstract class reflection.
 * @run main GraphicsTest
 */

public class GraphicsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.awt.Graphics");
        check(!c.isInterface() && !c.isEnum(), "not interface/enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "create"), "method create");
        check(hasMethod(c, "translate"), "method translate");
        check(hasMethod(c, "getColor"), "method getColor");
        check(hasMethod(c, "setColor"), "method setColor");
        check(hasMethod(c, "setPaintMode"), "method setPaintMode");
        check(hasMethod(c, "setXORMode"), "method setXORMode");
        check(hasMethod(c, "getFont"), "method getFont");
        check(hasMethod(c, "setFont"), "method setFont");
        check(hasMethod(c, "getFontMetrics"), "method getFontMetrics");
        check(hasMethod(c, "getClipBounds"), "method getClipBounds");
        check(hasMethod(c, "clipRect"), "method clipRect");
        check(hasMethod(c, "setClip"), "method setClip");
        check(hasMethod(c, "getClip"), "method getClip");
        check(hasMethod(c, "copyArea"), "method copyArea");
        check(hasMethod(c, "drawLine"), "method drawLine");
        check(hasMethod(c, "fillRect"), "method fillRect");
        check(hasMethod(c, "drawRect"), "method drawRect");
        check(hasMethod(c, "clearRect"), "method clearRect");
        check(hasMethod(c, "drawRoundRect"), "method drawRoundRect");
        check(hasMethod(c, "fillRoundRect"), "method fillRoundRect");
        check(hasMethod(c, "draw3DRect"), "method draw3DRect");
        check(hasMethod(c, "fill3DRect"), "method fill3DRect");
        check(hasMethod(c, "drawOval"), "method drawOval");
        check(hasMethod(c, "fillOval"), "method fillOval");
        check(hasMethod(c, "drawArc"), "method drawArc");
        check(hasMethod(c, "fillArc"), "method fillArc");
        check(hasMethod(c, "drawPolyline"), "method drawPolyline");
        check(hasMethod(c, "drawPolygon"), "method drawPolygon");
        check(hasMethod(c, "fillPolygon"), "method fillPolygon");
        check(hasMethod(c, "drawString"), "method drawString");
        check(hasMethod(c, "drawChars"), "method drawChars");
        check(hasMethod(c, "drawBytes"), "method drawBytes");
        check(hasMethod(c, "drawImage"), "method drawImage");
        check(hasMethod(c, "dispose"), "method dispose");
        check(hasMethod(c, "finalize"), "method finalize");
        check(hasMethod(c, "toString"), "method toString");
        check(hasMethod(c, "getClipRect"), "method getClipRect");
        check(hasMethod(c, "hitClip"), "method hitClip");
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
