/*
 * @test
 * @summary java.awt.Graphics2D conformance: white-box structure + black-box behavior.
 * @run main Graphics2DTest
 */

import java.awt.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.awt.font.*;
import java.lang.reflect.*;
import java.util.*;

public class Graphics2DTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean approx(double a, double b) {
        return Math.abs(a - b) < 1e-6;
    }

    // count pixels whose RGB (ignoring alpha) is non-zero
    static int nonBlack(BufferedImage bi, int x0, int y0, int w, int h) {
        int c = 0;
        for (int y = y0; y < y0 + h; y++)
            for (int x = x0; x < x0 + w; x++)
                if ((bi.getRGB(x, y) & 0xffffff) != 0) c++;
        return c;
    }

    static int rgb(int r, int g, int b) { return (0xff << 24) | (r << 16) | (g << 8) | b; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.Graphics2D", false, Graphics2DTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.Graphics2D");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public type");
        check(Modifier.isAbstract(m), "abstract class");
        check(!c.isInterface(), "not interface");
        check(c.getEnumConstants() == null, "non-enum");
        check(c.getSuperclass() == java.awt.Graphics.class, "extends java.awt.Graphics");

        for (String name : new String[] {
                "draw", "fill", "drawString", "drawImage", "drawRenderedImage", "drawRenderableImage",
                "drawGlyphVector", "hit", "getDeviceConfiguration", "setComposite", "setPaint",
                "setStroke", "setRenderingHint", "getRenderingHint", "setRenderingHints",
                "addRenderingHints", "getRenderingHints", "translate", "rotate", "scale", "shear",
                "transform", "setTransform", "getTransform", "getPaint", "getComposite",
                "setBackground", "getBackground", "getStroke", "clip", "getFontRenderContext"})
            check(Arrays.stream(c.getMethods()).anyMatch(mm -> mm.getName().equals(name)),
                  "method present: " + name);

        check(Modifier.isAbstract(c.getMethod("draw", Shape.class).getModifiers()),
              "draw(Shape) is abstract");
        check(Modifier.isAbstract(c.getMethod("fill", Shape.class).getModifiers()),
              "fill(Shape) is abstract");
        check(Modifier.isAbstract(c.getMethod("setTransform", AffineTransform.class).getModifiers()),
              "setTransform is abstract");
        check(Modifier.isAbstract(c.getMethod("getTransform").getModifiers()),
              "getTransform is abstract");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // ===== initial state of a BufferedImage Graphics2D =====
        BufferedImage img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        check(g != null, "createGraphics returns Graphics2D");
        check(g.getTransform().isIdentity(), "initial transform is identity");
        check(g.getFontRenderContext() != null, "getFontRenderContext non-null");
        check(g.getDeviceConfiguration() != null, "getDeviceConfiguration non-null");
        check(g.getClipBounds() == null, "initial clip is null");
        check(g.getBackground().equals(Color.black), "initial background is black");
        check(g.getPaint().equals(Color.white), "initial paint is white");
        check(g.getComposite() != null, "initial composite non-null");
        check(g.getStroke() != null, "initial stroke non-null");

        // ===== setPaint / getPaint =====
        g.setPaint(Color.RED);
        check(g.getPaint().equals(Color.RED), "setPaint/getPaint round-trip (red)");
        g.setPaint(Color.BLUE);
        check(g.getPaint().equals(Color.BLUE), "setPaint/getPaint round-trip (blue)");

        // ===== fill(Shape) + pixel verification =====
        g.setComposite(AlphaComposite.SrcOver);
        g.setTransform(new AffineTransform());
        g.setClip(null);
        g.setPaint(Color.RED);
        g.fill(new Rectangle2D.Float(10, 10, 10, 10));
        check(img.getRGB(15, 15) == 0xffff0000, "fill rectangle paints center pixel red");
        check(img.getRGB(5, 5) == 0, "outside filled rectangle stays transparent");

        // ===== draw(Shape) with a thick stroke =====
        img.setRGB(15, 10, 0); // ensure clean
        g.setStroke(new BasicStroke(3.0f));
        check(g.getStroke() instanceof BasicStroke, "setStroke installs BasicStroke");
        check(((BasicStroke) g.getStroke()).getLineWidth() == 3.0f, "stroke width preserved");
        g.setPaint(Color.GREEN);
        g.setComposite(AlphaComposite.SrcOver);
        g.draw(new Line2D.Double(20, 20, 40, 20));
        check((img.getRGB(30, 20) & 0x00ff00) != 0, "draw(Line2D) paints line pixel green");

        // ===== setComposite: AlphaComposite.Clear clears pixels =====
        img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        g = img.createGraphics();
        g.setColor(Color.RED);
        g.fillRect(0, 0, 64, 64);
        check(img.getRGB(10, 10) == 0xffff0000, "baseline filled red");
        g.setComposite(AlphaComposite.Clear);
        check(g.getComposite() == AlphaComposite.Clear, "getComposite returns Clear instance");
        g.fill(new Rectangle2D.Double(0, 0, 32, 64));
        check(img.getRGB(10, 10) == 0, "Clear composite erases filled region");
        check(img.getRGB(50, 10) == 0xffff0000, "Clear composite leaves non-clipped region red");

        // ===== rendering hints (single + bulk) =====
        img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        check(g.getRenderingHint(RenderingHints.KEY_ANTIALIASING) == RenderingHints.VALUE_ANTIALIAS_ON,
              "setRenderingHint/getRenderingHint round-trip");
        Map<RenderingHints.Key, Object> hints = new HashMap<>();
        hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.addRenderingHints(hints);
        check(g.getRenderingHint(RenderingHints.KEY_RENDERING) == RenderingHints.VALUE_RENDER_QUALITY,
              "addRenderingHints adds rendering hint");
        check(g.getRenderingHint(RenderingHints.KEY_ANTIALIASING) == RenderingHints.VALUE_ANTIALIAS_OFF,
              "addRenderingHints overrides antialiasing hint");
        check(g.getRenderingHints().get(RenderingHints.KEY_RENDERING) == RenderingHints.VALUE_RENDER_QUALITY,
              "getRenderingHints exposes added hint");
        g.setRenderingHints(hints);
        check(g.getRenderingHints().get(RenderingHints.KEY_RENDERING) == RenderingHints.VALUE_RENDER_QUALITY,
              "setRenderingHints bulk install");

        // ===== affine transforms =====
        img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        g = img.createGraphics();
        g.translate(5, 10);
        check(approx(g.getTransform().getTranslateX(), 5) && approx(g.getTransform().getTranslateY(), 10),
              "translate(int,int) updates transform");
        g.setTransform(new AffineTransform());
        g.translate(1.5, 2.5);
        check(approx(g.getTransform().getTranslateX(), 1.5) && approx(g.getTransform().getTranslateY(), 2.5),
              "translate(double,double) updates transform");

        g.setTransform(new AffineTransform());
        g.rotate(Math.PI / 2);
        Point2D p = g.getTransform().deltaTransform(new Point2D.Double(1, 0), null);
        check(approx(p.getX(), 0) && approx(p.getY(), 1), "rotate(90) maps (1,0)->(0,1)");

        g.setTransform(new AffineTransform());
        g.scale(2, 3);
        check(approx(g.getTransform().getScaleX(), 2) && approx(g.getTransform().getScaleY(), 3),
              "scale(2,3) updates transform");

        g.setTransform(new AffineTransform());
        g.shear(1, 0);
        Point2D q = g.getTransform().deltaTransform(new Point2D.Double(0, 1), null);
        check(approx(q.getX(), 1) && approx(q.getY(), 1), "shear(1,0) maps (0,1)->(1,1)");

        g.setTransform(new AffineTransform());
        g.transform(AffineTransform.getTranslateInstance(7, 8));
        check(approx(g.getTransform().getTranslateX(), 7) && approx(g.getTransform().getTranslateY(), 8),
              "transform(AffineTransform) concatenates translation");

        g.setTransform(new AffineTransform());
        check(g.getTransform().isIdentity(), "setTransform(identity) resets transform");

        // ===== clip(Shape) =====
        img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        g = img.createGraphics();
        g.clip(new Rectangle2D.Double(10, 10, 20, 20));
        check(g.getClipBounds().equals(new Rectangle(10, 10, 20, 20)), "clip installs rectangle bounds");
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 64, 64);
        check(img.getRGB(15, 15) == 0xffffffff, "fill respects clip: inside painted");
        check(img.getRGB(5, 5) == 0, "fill respects clip: outside not painted");

        // ===== hit() =====
        img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        g = img.createGraphics();
        check(g.hit(new Rectangle(5, 5, 10, 10), new Rectangle2D.Double(0, 0, 10, 10), false),
              "hit true for intersecting rectangle");
        check(!g.hit(new Rectangle(80, 80, 10, 10), new Rectangle2D.Double(0, 0, 10, 10), false),
              "hit false for disjoint rectangle");

        // ===== drawString =====
        img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        g = img.createGraphics();
        g.setColor(Color.WHITE);
        g.setFont(new Font("Dialog", Font.PLAIN, 24));
        g.drawString("ABC", 5, 30);
        check(nonBlack(img, 0, 0, 100, 40) > 0, "drawString paints glyph pixels");

        // ===== drawGlyphVector =====
        img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        g = img.createGraphics();
        Font f = new Font("Dialog", Font.PLAIN, 24);
        GlyphVector gv = f.createGlyphVector(g.getFontRenderContext(), "Hi");
        g.setColor(Color.WHITE);
        g.drawGlyphVector(gv, 5, 30);
        check(nonBlack(img, 0, 0, 100, 40) > 0, "drawGlyphVector paints glyph pixels");

        // ===== image drawing =====
        BufferedImage src = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < 8; y++) for (int x = 0; x < 8; x++) src.setRGB(x, y, 0xffff0000);

        img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        g = img.createGraphics();
        boolean complete = g.drawImage(src, new AffineTransform(), null);
        check(complete, "drawImage(BufferedImage,AffineTransform,null) completes");
        check(img.getRGB(4, 4) == 0xffff0000, "drawImage(AffineTransform) paints pixel red");

        img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        g = img.createGraphics();
        g.drawImage(src, AffineTransform.getScaleInstance(2, 2), null);
        check(img.getRGB(12, 12) == 0xffff0000, "drawImage scaled paints red at (12,12)");

        img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        g = img.createGraphics();
        g.drawImage(src, null, 0, 0);
        check(img.getRGB(4, 4) == 0xffff0000, "drawImage(BufferedImage,null,x,y) paints red");

        img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        g = img.createGraphics();
        g.drawRenderedImage(src, new AffineTransform());
        check(img.getRGB(4, 4) == 0xffff0000, "drawRenderedImage paints red");

        // ===== draw3DRect / fill3DRect =====
        img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        g = img.createGraphics();
        g.setColor(Color.YELLOW);
        g.fill3DRect(5, 5, 20, 20, true);
        check((img.getRGB(10, 10) & 0xffffff) != 0, "fill3DRect paints interior");
        g.draw3DRect(30, 30, 10, 10, false);
        check((img.getRGB(35, 30) & 0xffffff) != 0 || (img.getRGB(31, 31) & 0xffffff) != 0,
              "draw3DRect paints outline");

        g.dispose();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
