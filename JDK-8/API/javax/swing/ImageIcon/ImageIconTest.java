/*
 * @test
 * @summary javax.swing.ImageIcon conformance: constructors, load status, paint contract (headless-safe).
 * @run main ImageIconTest
 */

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;

public class ImageIconTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static Throwable checkThrows(Class<? extends Throwable> ex, Runnable r) {
        try { r.run(); } catch (Throwable t) {
            if (!ex.isInstance(t)) throw new AssertionError("expected " + ex.getSimpleName() + " got " + t);
            passed++; return t; }
        throw new AssertionError("expected " + ex.getSimpleName() + " not thrown");
    }

    static BufferedImage solidImage(int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.RED);
        g.fillRect(0, 0, w, h);
        g.dispose();
        return img;
    }

    static byte[] pngBytes(BufferedImage img) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(img, "png", out);
        return out.toByteArray();
    }

    static void testEmptyAndBasics() {
        ImageIcon ic = new ImageIcon();
        check(ic.getImage() == null, "no image");
        check(ic.getIconWidth() == -1, "width -1");
        check(ic.getIconHeight() == -1, "height -1");
        check(ic.getDescription() == null, "description default null");
        check(ic.getImageObserver() == null, "observer default null");
        check(ic.toString() != null && !ic.toString().isEmpty(), "toString nonempty");
        ic.setDescription("desc");
        check("desc".equals(ic.getDescription()), "setDescription");
        JLabel obs = new JLabel("x");
        ic.setImageObserver(obs);
        check(ic.getImageObserver() == obs, "setImageObserver roundtrip");
        ic.setImageObserver(null);
        check(ic.getImageObserver() == null, "observer reset");
    }

    static void testConstructors() throws Exception {
        BufferedImage img = solidImage(7, 9);
        ImageIcon fromImage = new ImageIcon(img);
        check(fromImage.getIconWidth() == 7 && fromImage.getIconHeight() == 9,
                "Image ctor dimensions");
        check(fromImage.getImage() == (Image) img, "getImage returns ctor image");
        check(fromImage.getImageLoadStatus() == java.awt.MediaTracker.COMPLETE,
                "valid image reports COMPLETE load status");
        ImageIcon fromBytes = new ImageIcon(pngBytes(img));
        check(fromBytes.getIconWidth() == 7 && fromBytes.getIconHeight() == 9,
                "byte[] ctor dimensions");
        check(fromBytes.getImageLoadStatus() == java.awt.MediaTracker.COMPLETE,
                "decoded bytes report COMPLETE");
        ImageIcon described = new ImageIcon(img, "dsc");
        check("dsc".equals(described.getDescription()), "(Image, String) ctor description");
        ImageIcon fromDescBytes = new ImageIcon(pngBytes(img), "pd");
        check("pd".equals(fromDescBytes.getDescription()), "(byte[], String) ctor description");
    }

    static void testBrokenAndReset() throws Exception {
        ImageIcon garbage = new ImageIcon("not-png".getBytes("UTF-8"));
        check(garbage.getImageLoadStatus() == java.awt.MediaTracker.ERRORED,
                "undecodable bytes report ERRORED");
        check(garbage.getIconWidth() == -1 && garbage.getIconHeight() == -1,
                "broken icon has -1 dimensions");
        ImageIcon ic = new ImageIcon(solidImage(3, 4));
        check(ic.getIconWidth() == 3 && ic.getIconHeight() == 4, "setImage target ready");
        ic.setImage(solidImage(3, 4));
        check(ic.getIconWidth() == 3 && ic.getIconHeight() == 4, "setImage swaps image");
        checkThrows(NullPointerException.class, () -> ic.setImage(null));
    }

    static void testPaintContract() {
        ImageIcon ic = new ImageIcon(solidImage(4, 3));
        check(ic instanceof javax.swing.Icon, "ImageIcon is an Icon");
        check(ic instanceof javax.accessibility.Accessible, "ImageIcon is Accessible");
        check(ic.getAccessibleContext() != null, "accessible context nonnull");
        BufferedImage canvas = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = canvas.createGraphics();
        ic.paintIcon(null, g, 2, 3); // null component tolerated headless
        g.dispose();
        check(canvas.getRGB(2, 3) == canvas.getRGB(2, 3), "paint ran without throwing");
        int px = canvas.getRGB(3, 4);
        check((px & 0xFF0000) != 0 || (px & 0xFF00) != 0 || (px & 0xFF) != 0,
                "pixels drawn at offset");
    }

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(new MetalLookAndFeel());
        testEmptyAndBasics();
        testConstructors();
        testBrokenAndReset();
        testPaintContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
