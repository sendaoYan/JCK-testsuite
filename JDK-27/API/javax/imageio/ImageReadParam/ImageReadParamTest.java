/*
 * @test
 * @summary javax.imageio.ImageReadParam conformance: white-box structure + black-box behavior.
 * @run main ImageReadParamTest
 */

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Modifier;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.stream.ImageInputStream;

public class ImageReadParamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface ThrowingRunnable { void run() throws Exception; }
    static void expectThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        boolean thrown = false;
        try { r.run(); } catch (Throwable t) {
            thrown = true;
            check(expected.isAssignableFrom(t.getClass()),
                  msg + " expected " + expected.getSimpleName() + " but got " + t.getClass().getSimpleName());
        }
        check(thrown, msg + " should throw " + expected.getSimpleName());
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.imageio.ImageReadParam");
        int mods = c.getModifiers();
        check(!c.isInterface(), "not interface");
        check(Modifier.isPublic(mods), "public");
        check(!Modifier.isAbstract(mods), "not abstract");
        check(c.getSuperclass().getName().equals("javax.imageio.IIOParam"), "extends IIOParam");
        java.util.Set<String> dm = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("setDestinationType"), "method setDestinationType");
        check(dm.contains("setDestination"), "method setDestination");
        check(dm.contains("getDestination"), "method getDestination");
        check(dm.contains("setDestinationBands"), "method setDestinationBands");
        check(dm.contains("getDestinationBands"), "method getDestinationBands");
        check(dm.contains("canSetSourceRenderSize"), "method canSetSourceRenderSize");
        check(dm.contains("setSourceRenderSize"), "method setSourceRenderSize");
        check(dm.contains("getSourceRenderSize"), "method getSourceRenderSize");
        check(dm.contains("setSourceProgressivePasses"), "method setSourceProgressivePasses");
        check(dm.contains("getSourceMinProgressivePass"), "method getSourceMinProgressivePass");
        check(dm.contains("getSourceMaxProgressivePass"), "method getSourceMaxProgressivePass");
        check(dm.contains("getSourceNumProgressivePasses"), "method getSourceNumProgressivePasses");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ImageReadParam p = new ImageReadParam();

        // defaults
        check(p.getDestination() == null, "default destination null");
        check(!p.canSetSourceRenderSize(), "default canSetSourceRenderSize false");
        check(p.getSourceRenderSize() == null, "default sourceRenderSize null");
        check(p.getSourceMinProgressivePass() == 0, "default min progressive pass 0");
        check(p.getSourceNumProgressivePasses() == Integer.MAX_VALUE, "default num progressive passes MAX");
        check(p.getSourceMaxProgressivePass() == Integer.MAX_VALUE, "default max progressive pass MAX");
        check(p.getDestinationBands() == null, "default destinationBands null");

        // destination image stored by reference
        BufferedImage dest = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);
        p.setDestination(dest);
        check(p.getDestination() == dest, "set/get destination identity");

        // setDestinationType clears destination as a side effect
        ImageTypeSpecifier its = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_ARGB);
        p.setDestinationType(its);
        check(p.getDestination() == null, "setDestinationType clears destination");
        check(p.getDestinationType() == its, "setDestinationType sets type");

        // destination bands: copy semantics, validation
        int[] dbands = new int[]{3, 2, 1, 0};
        p.setDestinationBands(dbands);
        int[] gotBands = p.getDestinationBands();
        check(gotBands.length == 4 && gotBands[0] == 3 && gotBands[3] == 0, "destinationBands round-trip");
        check(gotBands != dbands, "getDestinationBands returns a copy");
        p.setDestinationBands(null);
        check(p.getDestinationBands() == null, "setDestinationBands null");
        expectThrows(IllegalArgumentException.class, () -> p.setDestinationBands(new int[]{1, 1}), "duplicate destination band");
        expectThrows(IllegalArgumentException.class, () -> p.setDestinationBands(new int[]{-2}), "negative destination band");

        // source render size unsupported by default
        expectThrows(UnsupportedOperationException.class, () -> p.setSourceRenderSize(new Dimension(8, 8)), "setSourceRenderSize unsupported");

        // progressive passes
        p.setSourceProgressivePasses(1, 5);
        check(p.getSourceMinProgressivePass() == 1, "min pass 1");
        check(p.getSourceNumProgressivePasses() == 5, "num passes 5");
        check(p.getSourceMaxProgressivePass() == 5, "max pass 5");
        p.setSourceProgressivePasses(0, Integer.MAX_VALUE);
        check(p.getSourceMaxProgressivePass() == Integer.MAX_VALUE, "max pass MAX");
        expectThrows(IllegalArgumentException.class, () -> p.setSourceProgressivePasses(-1, 1), "negative min pass");
        expectThrows(IllegalArgumentException.class, () -> p.setSourceProgressivePasses(0, 0), "zero num passes");

        // inherited IIOParam behavior through the concrete class
        check(p.getSourceXSubsampling() == 1, "inherited default xSubsampling 1");
        p.setSourceSubsampling(2, 2, 0, 0);
        check(p.getSourceXSubsampling() == 2 && p.getSourceYSubsampling() == 2, "inherited setSourceSubsampling");

        // end-to-end: use destination with a real PNG read
        BufferedImage src = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);
        src.setRGB(0, 0, 0xFF123456);
        src.setRGB(3, 3, 0xFF654321);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(src, "png", bos);
        ImageReader reader = ImageIO.getImageReadersByFormatName("png").next();
        ImageReadParam rp = reader.getDefaultReadParam();
        BufferedImage target = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);
        rp.setDestination(target);
        ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(bos.toByteArray()));
        reader.setInput(iis);
        BufferedImage result = reader.read(0, rp);
        check(result == target, "read returns the supplied destination");
        check(target.getRGB(0, 0) == 0xFF123456, "destination received pixel (0,0)");
        check(target.getRGB(3, 3) == 0xFF654321, "destination received pixel (3,3)");
        reader.dispose();
        iis.close();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
