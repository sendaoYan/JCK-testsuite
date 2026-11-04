/*
 * @test
 * @summary javax.swing.GrayFilter conformance: type shape + exact graying formula (headless-safe).
 * @run main GrayFilterTest
 */

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import javax.swing.GrayFilter;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;

public class GrayFilterTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    static void testShape() throws Exception {
        Class<?> c = Class.forName("javax.swing.GrayFilter", false, GrayFilterTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null
                && c.getSuperclass().getName().equals("java.awt.image.RGBImageFilter"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("createDisabledImage"), "method createDisabledImage");
        check(names.contains("filterRGB"), "method filterRGB");
    }

    static int gray(GrayFilter f, int r, int g, int b) {
        return f.filterRGB(0, 0, (0xFF << 24) | (r << 16) | (g << 8) | b) & 0xFF;
    }

    static void testDarkerFormula() {
        GrayFilter f = new GrayFilter(false, 50);
        check(gray(f, 255, 0, 0) == 12, "darker/50 red");
        check(gray(f, 0, 255, 0) == 25, "darker/50 green");
        check(gray(f, 0, 0, 255) == 4, "darker/50 blue");
        check(gray(f, 255, 255, 255) == 42, "darker/50 white");
        check(gray(f, 0, 0, 0) == 0, "darker/50 black");
        check(gray(f, 100, 150, 200) == 23, "darker/50 mixed");
        GrayFilter f20 = new GrayFilter(false, 20);
        check(gray(f20, 200, 100, 50) == 32, "darker/20 mixed");
        check(gray(f20, 0, 0, 0) == 0, "darker/20 black stays black");
        check(gray(f20, 255, 255, 255) == 68, "darker/20 white");
    }

    static void testBrighterFormula() {
        GrayFilter f = new GrayFilter(true, 50);
        check(gray(f, 255, 0, 0) == 140, "brighter/50 red");
        check(gray(f, 0, 255, 0) == 153, "brighter/50 green");
        check(gray(f, 0, 0, 255) == 132, "brighter/50 blue");
        check(gray(f, 255, 255, 255) == 170, "brighter/50 white");
        check(gray(f, 0, 0, 0) == 128, "brighter/50 black");
        check(gray(f, 100, 150, 200) == 151, "brighter/50 mixed");
        check(gray(new GrayFilter(true, 20), 200, 100, 50) == 84, "brighter/20 mixed");
    }

    static void testAlphaAndDisabledImage() {
        GrayFilter f = new GrayFilter(false, 50);
        int out = f.filterRGB(0, 0, (0x80 << 24) | (Color.RED.getRed() << 16)
                | (Color.RED.getGreen() << 8) | Color.RED.getBlue());
        check((out & 0xFF000000) == (0x80 << 24), "alpha preserved through filter");
        BufferedImage src = new BufferedImage(4, 3, BufferedImage.TYPE_INT_ARGB);
        Image disabled = GrayFilter.createDisabledImage(src);
        check(disabled != null, "createDisabledImage returns image");
        check(disabled.getWidth(null) == 4 && disabled.getHeight(null) == 3,
                "createDisabledImage preserves dimensions");
    }

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(new MetalLookAndFeel());
        testShape();
        testDarkerFormula();
        testBrighterFormula();
        testAlphaAndDisabledImage();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
