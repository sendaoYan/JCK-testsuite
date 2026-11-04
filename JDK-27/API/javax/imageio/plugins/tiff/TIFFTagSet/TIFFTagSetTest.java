/*
 * @test
 * @summary javax.imageio.plugins.tiff.TIFFTagSet conformance: white-box structure + black-box behavior.
 * @run main TIFFTagSetTest
 */

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import javax.imageio.plugins.tiff.TIFFTag;
import javax.imageio.plugins.tiff.TIFFTagSet;

public class TIFFTagSetTest {
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
        Class<?> c = Class.forName("javax.imageio.plugins.tiff.TIFFTagSet");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(Modifier.isAbstract(mods) == false, "not abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        check(c.getConstructors().length == 1, "one public constructor (List)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        TIFFTag w = new TIFFTag("ImageWidth", 256, (1 << TIFFTag.TIFF_SHORT) | (1 << TIFFTag.TIFF_LONG), 1);
        TIFFTag h = new TIFFTag("ImageLength", 257, (1 << TIFFTag.TIFF_SHORT) | (1 << TIFFTag.TIFF_LONG), 1);
        TIFFTag bps = new TIFFTag("BitsPerSample", 258, 1 << TIFFTag.TIFF_SHORT);
        List<TIFFTag> tags = new ArrayList<TIFFTag>();
        tags.add(w);
        tags.add(h);
        tags.add(bps);
        TIFFTagSet set = new TIFFTagSet(tags);

        // name/number lookup
        check(set.getTag("ImageWidth") == w, "getTag(name) ImageWidth");
        check(set.getTag("ImageLength") == h, "getTag(name) ImageLength");
        check(set.getTag("BitsPerSample") == bps, "getTag(name) BitsPerSample");
        check(set.getTag(256) == w, "getTag(number) 256");
        check(set.getTag(257) == h, "getTag(number) 257");
        check(set.getTag(258) == bps, "getTag(number) 258");
        check(set.getTag(999) == null, "unknown number null");
        check(set.getTag("Nonexistent") == null, "unknown name null");

        // tag names/numbers sorted sets
        SortedSet<String> names = set.getTagNames();
        SortedSet<Integer> numbers = set.getTagNumbers();
        check(names.size() == 3, "three names");
        check(numbers.size() == 3, "three numbers");
        check(names.contains("ImageWidth") && names.contains("ImageLength") && names.contains("BitsPerSample"),
              "names contain all three");
        check(numbers.contains(Integer.valueOf(256)) && numbers.contains(Integer.valueOf(257))
              && numbers.contains(Integer.valueOf(258)), "numbers contain 256/257/258");

        // ascending order of numbers
        Integer[] nums = numbers.toArray(new Integer[0]);
        check(nums[0] == 256 && nums[1] == 257 && nums[2] == 258, "numbers ascending");

        // duplicate tag numbers: later one wins (TreeMap put semantics)
        TIFFTag dup = new TIFFTag("Other", 256, 1 << TIFFTag.TIFF_LONG, 1);
        List<TIFFTag> dupTags = new ArrayList<TIFFTag>();
        dupTags.add(w);
        dupTags.add(dup);
        TIFFTagSet set2 = new TIFFTagSet(dupTags);
        check(set2.getTag(256) == dup, "duplicate number last-wins");
        check(set2.getTagNames().size() == 2, "duplicate name set keeps both by name");

        // invalid construction
        expectThrows(IllegalArgumentException.class, () -> new TIFFTagSet(null), "null list");
        List<Object> bad = new ArrayList<Object>();
        bad.add("not-a-tag");
        expectThrows(IllegalArgumentException.class, () -> new TIFFTagSet((List) bad), "list with non-TIFFTag");
        expectThrows(IllegalArgumentException.class, () -> set.getTag((String) null), "getTag(null)");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
