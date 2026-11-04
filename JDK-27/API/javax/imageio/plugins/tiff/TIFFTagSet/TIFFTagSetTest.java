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

    // ---- Part 3 (deepened): exact-exception helper ----
    static void expectExact(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
            check(false, msg + " should throw " + expected.getSimpleName() + " but nothing was thrown");
        } catch (Throwable t) {
            check(t.getClass() == expected,
                  msg + " expected exactly " + expected.getName() + " but got " + t.getClass().getName());
        }
    }

    // ---- Part 3 (deepened): immutability, sorted views, boundary lookups ----
    static void testDeepened() throws Exception {
        TIFFTag w = new TIFFTag("ImageWidth", 256, (1 << TIFFTag.TIFF_SHORT) | (1 << TIFFTag.TIFF_LONG), 1);
        TIFFTag h = new TIFFTag("ImageLength", 257, (1 << TIFFTag.TIFF_SHORT) | (1 << TIFFTag.TIFF_LONG), 1);

        // the constructor copies its input list: later mutation is invisible (probed)
        List<TIFFTag> src = new ArrayList<TIFFTag>(java.util.Arrays.asList(w, h));
        TIFFTagSet set = new TIFFTagSet(src);
        src.clear();
        check(set.getTag(256) == w && set.getTag(257) == h, "source list mutation invisible");

        // getTagNumbers/getTagNames are unmodifiable views (probed: add/remove -> UOE)
        expectExact(UnsupportedOperationException.class,
            () -> set.getTagNumbers().add(Integer.valueOf(999)), "tagNumbers.add UOE");
        expectExact(UnsupportedOperationException.class,
            () -> set.getTagNumbers().remove(Integer.valueOf(256)), "tagNumbers.remove UOE");
        expectExact(UnsupportedOperationException.class,
            () -> set.getTagNames().add("zzz"), "tagNames.add UOE");
        expectExact(UnsupportedOperationException.class,
            () -> set.getTagNames().remove("ImageWidth"), "tagNames.remove UOE");
        expectExact(UnsupportedOperationException.class,
            () -> set.getTagNumbers().clear(), "tagNumbers.clear UOE");

        // SortedSet surface: first/last/contains reflect sorted order
        SortedSet<Integer> nums = set.getTagNumbers();
        check(nums.first() == 256, "numbers first 256");
        check(nums.last() == 257, "numbers last 257");
        check(nums.size() == 2, "numbers size 2");
        SortedSet<String> names = set.getTagNames();
        check(names.first().equals("ImageLength"), "names first alphabetical ImageLength");
        check(names.last().equals("ImageWidth"), "names last alphabetical ImageWidth");
        check(nums.headSet(257).size() == 1 && nums.headSet(257).contains(256), "headSet view");

        // boundary lookups: number 0, negative numbers, empty string all miss (probed)
        check(set.getTag(0) == null, "getTag(0) null");
        check(set.getTag(-5) == null, "getTag(-5) null");
        check(set.getTag(65535) == null, "getTag(65535) null");
        check(set.getTag("") == null, "getTag(\"\") null");

        // exact-exception constructor lookups (probed)
        expectExact(IllegalArgumentException.class, () -> new TIFFTagSet(null), "null list IAE");
        expectExact(IllegalArgumentException.class,
            () -> set.getTag((String) null), "getTag(null) IAE");

        // empty tag set: legal, zero-size views, first() -> NoSuchElementException (probed)
        TIFFTagSet empty = new TIFFTagSet(new ArrayList<TIFFTag>());
        check(empty.getTagNumbers().isEmpty(), "empty set numbers empty");
        check(empty.getTagNames().isEmpty(), "empty set names empty");
        check(empty.getTag(1) == null && empty.getTag("x") == null, "empty set lookups null");
        expectExact(java.util.NoSuchElementException.class,
            () -> empty.getTagNumbers().first(), "empty first() NSEE");
        expectExact(java.util.NoSuchElementException.class,
            () -> empty.getTagNames().last(), "empty last() NSEE");

        // single-tag set sorted view boundaries
        TIFFTagSet one = new TIFFTagSet(java.util.Arrays.asList(w));
        check(one.getTagNumbers().first() == 256 && one.getTagNumbers().last() == 256,
              "single-tag first==last");

        // duplicate number with distinct name keeps both names but one number key (probed)
        TIFFTag dup = new TIFFTag("Other", 256, 1 << TIFFTag.TIFF_LONG, 1);
        TIFFTagSet set2 = new TIFFTagSet(java.util.Arrays.asList(w, dup));
        check(set2.getTag(256) == dup, "duplicate number last-wins");
        check(set2.getTagNames().size() == 2, "both names kept");
        check(set2.getTagNumbers().size() == 1, "single number key");
        check(set2.getTag("ImageWidth") == w && set2.getTag("Other") == dup,
              "name lookup not collapsed by duplicate number");

        // tag number 0 participates in sorting (Exif GPS GPSVersionID uses 0)
        TIFFTag zero = new TIFFTag("Zero", 0, 1 << TIFFTag.TIFF_BYTE, 1);
        TIFFTagSet zset = new TIFFTagSet(java.util.Arrays.asList(w, zero));
        check(zset.getTagNumbers().first() == 0, "tag 0 sorts first");

        // list with a null element is rejected (probed)
        List<TIFFTag> withNull = new ArrayList<TIFFTag>();
        withNull.add(w);
        withNull.add(null);
        expectExact(IllegalArgumentException.class,
            () -> new TIFFTagSet(withNull), "list containing null IAE");
    }

    // ---- Part 3 (deepened): connection to the shipped standard tag sets ----
    static void testStandardSets() throws Exception {
        // every shipped singleton resolves all of its own names and numbers both ways
        javax.imageio.plugins.tiff.TIFFTagSet[] sets = {
            javax.imageio.plugins.tiff.BaselineTIFFTagSet.getInstance(),
            javax.imageio.plugins.tiff.FaxTIFFTagSet.getInstance(),
            javax.imageio.plugins.tiff.ExifParentTIFFTagSet.getInstance(),
            javax.imageio.plugins.tiff.GeoTIFFTagSet.getInstance(),
        };
        int[] expectedMin = { 254, 326, 34665, 33550 };
        for (int i = 0; i < sets.length; i++) {
            javax.imageio.plugins.tiff.TIFFTagSet s = sets[i];
            check(s.getTagNumbers().size() > 0, "set " + i + " non-empty");
            check(s.getTagNumbers().size() == s.getTagNames().size(), "set " + i + " cardinality");
            check(s.getTagNumbers().first() == expectedMin[i],
                  "set " + i + " smallest tag " + expectedMin[i]);
            boolean allResolve = true;
            for (String name : s.getTagNames()) {
                javax.imageio.plugins.tiff.TIFFTag t = s.getTag(name);
                if (t == null || !t.getName().equals(name)
                    || s.getTag(t.getNumber()) != t) { allResolve = false; break; }
            }
            check(allResolve, "set " + i + " every name/number resolves bidirectionally");
        }
        // singleton identity: getInstance called twice yields the same object
        check(javax.imageio.plugins.tiff.BaselineTIFFTagSet.getInstance()
              == javax.imageio.plugins.tiff.BaselineTIFFTagSet.getInstance(), "baseline singleton identity");
        check(javax.imageio.plugins.tiff.FaxTIFFTagSet.getInstance()
              == javax.imageio.plugins.tiff.FaxTIFFTagSet.getInstance(), "fax singleton identity");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepened();
        testStandardSets();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
