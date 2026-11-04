/*
 * @test
 * @summary javax.swing.GrayFilter conformance: white-box structure + black-box behavior.
 * @run main GrayFilterTest
 */

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.lang.reflect.Modifier;

import javax.swing.GrayFilter;

public class GrayFilterTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.GrayFilter");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.awt.image.RGBImageFilter"), "superclass");
        check(!Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        int opaquePixel = 0xFF112233;

        // percent=100, brightness=false -> fully dark (alpha preserved)
        int dark = new GrayFilter(false, 100).filterRGB(0, 0, opaquePixel);
        check(dark == 0xFF000000, "dark filter -> opaque black");

        // percent=100, brightness=true -> fully bright (alpha preserved)
        int bright = new GrayFilter(true, 100).filterRGB(0, 0, opaquePixel);
        check(bright == 0xFFFFFFFF, "bright filter -> opaque white");

        // percent=0 -> grayscale (r==g==b) with alpha preserved
        int gray = new GrayFilter(false, 0).filterRGB(0, 0, opaquePixel);
        check((gray & 0xFF000000) == 0xFF000000, "gray filter preserves alpha");
        int r = (gray >> 16) & 0xFF, g = (gray >> 8) & 0xFF, b = gray & 0xFF;
        check(r == g && g == b, "gray filter produces grayscale");

        // createDisabledImage produces a filtered image
        BufferedImage src = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);
        Image disabled = GrayFilter.createDisabledImage(src);
        check(disabled != null, "createDisabledImage non-null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
