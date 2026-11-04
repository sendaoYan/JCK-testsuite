/*
 * @test
 * @summary javax.imageio.plugins.tiff.TIFFImageReadParam conformance: white-box structure + black-box behavior.
 * @run main TIFFImageReadParamTest
 */

import java.lang.reflect.Modifier;
import java.util.List;
import javax.imageio.plugins.tiff.BaselineTIFFTagSet;
import javax.imageio.plugins.tiff.ExifParentTIFFTagSet;
import javax.imageio.plugins.tiff.FaxTIFFTagSet;
import javax.imageio.plugins.tiff.GeoTIFFTagSet;
import javax.imageio.plugins.tiff.TIFFImageReadParam;
import javax.imageio.plugins.tiff.TIFFTagSet;

public class TIFFImageReadParamTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
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
        Class<?> c = Class.forName("javax.imageio.plugins.tiff.TIFFImageReadParam");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(Modifier.isFinal(mods), "final modifier");
        check(c.getSuperclass().getName().equals("javax.imageio.ImageReadParam"), "extends ImageReadParam");
        check(c.getConstructors().length == 1, "one public constructor");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        TIFFImageReadParam p = new TIFFImageReadParam();

        // default allowed tag sets: Baseline, Fax, ExifParent, GeoTIFF
        List<TIFFTagSet> allowed = p.getAllowedTagSets();
        check(allowed != null && allowed.size() == 4, "default 4 allowed tag sets");
        check(allowed.contains(BaselineTIFFTagSet.getInstance()), "contains Baseline");
        check(allowed.contains(FaxTIFFTagSet.getInstance()), "contains Fax");
        check(allowed.contains(ExifParentTIFFTagSet.getInstance()), "contains ExifParent");
        check(allowed.contains(GeoTIFFTagSet.getInstance()), "contains GeoTIFF");

        // default unknown tags false
        check(!p.getReadUnknownTags(), "default readUnknownTags false");

        // readUnknownTags round-trip
        p.setReadUnknownTags(true);
        check(p.getReadUnknownTags(), "setReadUnknownTags true");
        p.setReadUnknownTags(false);
        check(!p.getReadUnknownTags(), "setReadUnknownTags false");

        // add a tag set
        TIFFTagSet custom = ExifParentTIFFTagSet.getInstance(); // already present -> no-op
        int before = p.getAllowedTagSets().size();
        p.addAllowedTagSet(custom);
        check(p.getAllowedTagSets().size() == before, "duplicate addAllowedTagSet is no-op");

        TIFFTagSet fresh = new javax.imageio.plugins.tiff.TIFFTagSet(
            new java.util.ArrayList<javax.imageio.plugins.tiff.TIFFTag>());
        p.addAllowedTagSet(fresh);
        check(p.getAllowedTagSets().size() == before + 1, "addAllowedTagSet increases size");
        check(p.getAllowedTagSets().contains(fresh), "added tag set present");

        // remove a tag set
        p.removeAllowedTagSet(fresh);
        check(p.getAllowedTagSets().size() == before, "removeAllowedTagSet decreases size");
        check(!p.getAllowedTagSets().contains(fresh), "removed tag set absent");

        // remove a default tag set
        p.removeAllowedTagSet(BaselineTIFFTagSet.getInstance());
        check(p.getAllowedTagSets().size() == before - 1, "remove default tag set");

        // invalid arguments
        expectThrows(IllegalArgumentException.class, () -> p.addAllowedTagSet(null), "addAllowedTagSet null");
        expectThrows(IllegalArgumentException.class, () -> p.removeAllowedTagSet(null), "removeAllowedTagSet null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
