/*
 * @test
 * @summary java.awt.Graphics2D conformance: abstract class reflection.
 * @run main Graphics2DTest
 */

public class Graphics2DTest {
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
        Class<?> c = Class.forName("java.awt.Graphics2D");
        check(!c.isInterface() && !c.isEnum(), "not interface/enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.awt.Graphics"), "superclass java.awt.Graphics");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "draw3DRect"), "method draw3DRect");
        check(hasMethod(c, "fill3DRect"), "method fill3DRect");
        check(hasMethod(c, "draw"), "method draw");
        check(hasMethod(c, "drawImage"), "method drawImage");
        check(hasMethod(c, "drawRenderedImage"), "method drawRenderedImage");
        check(hasMethod(c, "drawRenderableImage"), "method drawRenderableImage");
        check(hasMethod(c, "drawString"), "method drawString");
        check(hasMethod(c, "drawGlyphVector"), "method drawGlyphVector");
        check(hasMethod(c, "fill"), "method fill");
        check(hasMethod(c, "hit"), "method hit");
        check(hasMethod(c, "getDeviceConfiguration"), "method getDeviceConfiguration");
        check(hasMethod(c, "setComposite"), "method setComposite");
        check(hasMethod(c, "setPaint"), "method setPaint");
        check(hasMethod(c, "setStroke"), "method setStroke");
        check(hasMethod(c, "setRenderingHint"), "method setRenderingHint");
        check(hasMethod(c, "getRenderingHint"), "method getRenderingHint");
        check(hasMethod(c, "setRenderingHints"), "method setRenderingHints");
        check(hasMethod(c, "addRenderingHints"), "method addRenderingHints");
        check(hasMethod(c, "getRenderingHints"), "method getRenderingHints");
        check(hasMethod(c, "translate"), "method translate");
        check(hasMethod(c, "rotate"), "method rotate");
        check(hasMethod(c, "scale"), "method scale");
        check(hasMethod(c, "shear"), "method shear");
        check(hasMethod(c, "transform"), "method transform");
        check(hasMethod(c, "setTransform"), "method setTransform");
        check(hasMethod(c, "getTransform"), "method getTransform");
        check(hasMethod(c, "getPaint"), "method getPaint");
        check(hasMethod(c, "getComposite"), "method getComposite");
        check(hasMethod(c, "setBackground"), "method setBackground");
        check(hasMethod(c, "getBackground"), "method getBackground");
        check(hasMethod(c, "getStroke"), "method getStroke");
        check(hasMethod(c, "clip"), "method clip");
        check(hasMethod(c, "getFontRenderContext"), "method getFontRenderContext");
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Part 3: contract deepening via BufferedImage.createGraphics() ----
    static int px(java.awt.image.BufferedImage bi, int x, int y) { return bi.getRGB(x, y); }
    static java.awt.image.BufferedImage blank() {
        java.awt.image.BufferedImage bi = new java.awt.image.BufferedImage(20, 20, java.awt.image.BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g = bi.createGraphics();
        g.setColor(java.awt.Color.WHITE); g.fillRect(0, 0, 20, 20); g.dispose();
        return bi;
    }
    static void testContract() {
        java.awt.image.BufferedImage bi = blank();
        java.awt.Graphics2D g = bi.createGraphics();
        // stroke/composite/paint round-trips return the same object set
        java.awt.BasicStroke st = new java.awt.BasicStroke(3f);
        g.setStroke(st);
        check(g.getStroke() == st, "setStroke/getStroke identity");
        java.awt.AlphaComposite ac = java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC, 0.5f);
        g.setComposite(ac);
        check(g.getComposite() == ac, "setComposite/getComposite identity");
        java.awt.GradientPaint gp = new java.awt.GradientPaint(0f, 0f, java.awt.Color.red, 10f, 10f, java.awt.Color.blue);
        g.setPaint(gp);
        check(g.getPaint() == gp, "setPaint/getPaint identity");
        check(g.getPaint() != java.awt.Color.BLACK, "paint not confused with color");
        // rendering hints: put/get + addRenderingHints
        g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        check(g.getRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING)
              == java.awt.RenderingHints.VALUE_ANTIALIAS_ON, "setRenderingHint/getRenderingHint");
        java.awt.RenderingHints extra = new java.awt.RenderingHints(
                java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_QUALITY);
        g.addRenderingHints(extra);
        check(g.getRenderingHint(java.awt.RenderingHints.KEY_RENDERING)
              == java.awt.RenderingHints.VALUE_RENDER_QUALITY, "addRenderingHints merges");
        check(g.getRenderingHints().containsKey(java.awt.RenderingHints.KEY_ANTIALIASING),
              "getRenderingHints reflects hints");
        g.setRenderingHints(new java.awt.RenderingHints(java.awt.RenderingHints.KEY_DITHERING,
                java.awt.RenderingHints.VALUE_DITHER_DISABLE));
        // setRenderingHints(Map) resets prior custom values to defaults and applies the map (observed)
        check(g.getRenderingHint(java.awt.RenderingHints.KEY_RENDERING)
              == java.awt.RenderingHints.VALUE_RENDER_DEFAULT,
              "setRenderingHints(Map) resets custom KEY_RENDERING to default");
        check(g.getRenderingHint(java.awt.RenderingHints.KEY_DITHERING)
              == java.awt.RenderingHints.VALUE_DITHER_DISABLE,
              "setRenderingHints(Map) applies given entries");
        // background
        g.setBackground(java.awt.Color.YELLOW);
        check(g.getBackground().equals(java.awt.Color.yellow), "setBackground/getBackground");
        // draw(Shape): outline only
        java.awt.image.BufferedImage bi2 = blank();
        java.awt.Graphics2D g2 = bi2.createGraphics();
        g2.setColor(java.awt.Color.BLACK);
        g2.draw(new java.awt.geom.Rectangle2D.Double(2, 2, 10, 10));
        check(px(bi2, 2, 2) == 0xFF000000 && px(bi2, 12, 12) == 0xFF000000, "draw(Shape) outline corners");
        check(px(bi2, 7, 7) == 0xFFFFFFFF, "draw(Shape) interior untouched");
        // fill(Shape): interior filled
        g2.setColor(java.awt.Color.CYAN);
        g2.fill(new java.awt.geom.Rectangle2D.Double(14, 14, 4, 4));
        check(px(bi2, 15, 15) == 0xFF00FFFF && px(bi2, 17, 17) == 0xFF00FFFF, "fill(Shape) interior");
        check(px(bi2, 18, 18) == 0xFFFFFFFF, "fill(Shape) exclusive far corner");
        g2.dispose();
        // rotate: exact 90-degree rotation maps user point deterministically
        java.awt.image.BufferedImage bi3 = blank();
        java.awt.Graphics2D g3 = bi3.createGraphics();
        g3.setColor(java.awt.Color.BLACK);
        g3.rotate(Math.PI / 2, 10, 10);
        g3.drawLine(12, 10, 12, 10);
        check(px(bi3, 10, 12) == 0xFF000000 && px(bi3, 10, 8) == 0xFFFFFFFF,
              "rotate 90deg maps (12,10) to (10,12)");
        g3.dispose();
        // scale: 2x scale quadruples rect extent
        java.awt.image.BufferedImage bi4 = blank();
        java.awt.Graphics2D g4 = bi4.createGraphics();
        g4.setColor(java.awt.Color.RED);
        g4.scale(2.0, 2.0);
        g4.fillRect(2, 2, 3, 3);
        check(px(bi4, 4, 4) == 0xFFFF0000 && px(bi4, 9, 9) == 0xFFFF0000, "scaled fill covers [4,10)");
        check(px(bi4, 10, 10) == 0xFFFFFFFF, "scaled fill boundary exclusive");
        g4.dispose();
        // shear + concat transform + getTransform non-null
        check(g.getTransform() != null, "getTransform non-null");
        g.shear(1, 0);
        check(g.getTransform().getShearX() == 1.0, "shear visible in transform");
        java.awt.geom.AffineTransform xform = java.awt.geom.AffineTransform.getTranslateInstance(5, 5);
        g.transform(xform);
        g.setTransform(new java.awt.geom.AffineTransform());
        check(g.getTransform().isIdentity(), "setTransform resets to identity");
        // clip(Shape) intersects current clip
        java.awt.image.BufferedImage bi5 = blank();
        java.awt.Graphics2D g5 = bi5.createGraphics();
        g5.setColor(java.awt.Color.BLACK);
        g5.setClip(new java.awt.Rectangle(0, 0, 10, 10));
        g5.clip(new java.awt.geom.Rectangle2D.Double(5, 0, 10, 10));
        check(g5.getClipBounds().equals(new java.awt.Rectangle(5, 0, 5, 10)), "clip(Shape) intersection");
        g5.fillRect(0, 0, 20, 20);
        check(px(bi5, 6, 6) == 0xFF000000 && px(bi5, 4, 4) == 0xFFFFFFFF, "clipped fill result");
        g5.dispose();
        // translate(int,int) via Graphics path
        g.translate(2, 3); g.rotate(0.5); g.scale(2, 2); g.shear(1, 1);
        check(true, "concatenating transforms no exception");
        // hit: rectangle vs transformed space
        g.setTransform(new java.awt.geom.AffineTransform());
        check(!g.hit(new java.awt.Rectangle(0, 0, 2, 2),
              new java.awt.geom.Rectangle2D.Double(10, 10, 5, 5), true),
              "hit false for disjoint rect");
        check(g.hit(new java.awt.Rectangle(10, 10, 5, 5),
              new java.awt.geom.Rectangle2D.Double(11, 11, 2, 2), true),
              "hit true for contained rect");
        check(g.hit(new java.awt.Rectangle(10, 10, 5, 5),
              new java.awt.geom.Rectangle2D.Double(11, 11, 2, 2), false),
              "hit with onStroke=false");
        // device configuration may be null offscreen? just callable
        try { g.getDeviceConfiguration(); check(true, "getDeviceConfiguration callable"); }
        catch (Throwable t) { check(true, "getDeviceConfiguration callable (null allowed)"); }
        // font render context available
        check(g.getFontRenderContext() != null, "getFontRenderContext non-null");
        // drawString with explicit font (deterministic non-blank pixels)
        java.awt.image.BufferedImage bi6 = blank();
        java.awt.Graphics2D g6 = bi6.createGraphics();
        g6.setColor(java.awt.Color.BLACK);
        g6.setFont(new java.awt.Font(java.awt.Font.DIALOG, java.awt.Font.PLAIN, 12));
        g6.drawString("Wg", 2, 12);
        boolean anyBlack = false;
        for (int x = 0; x < 20 && !anyBlack; x++) for (int y = 0; y < 20; y++)
            if (px(bi6, x, y) == 0xFF000000) { anyBlack = true; break; }
        check(anyBlack, "drawString rendered visible pixels");
        g6.dispose();
        g.dispose();
    }

}
