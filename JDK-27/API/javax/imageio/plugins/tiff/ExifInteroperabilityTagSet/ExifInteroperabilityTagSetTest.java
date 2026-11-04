/*
 * @test
 * @summary javax.imageio.plugins.tiff.ExifInteroperabilityTagSet conformance: white-box structure + black-box behavior.
 * @run main ExifInteroperabilityTagSetTest
 */

import java.lang.reflect.Modifier;
import java.util.SortedSet;
import javax.imageio.plugins.tiff.ExifInteroperabilityTagSet;
import javax.imageio.plugins.tiff.TIFFTag;

public class ExifInteroperabilityTagSetTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.imageio.plugins.tiff.ExifInteroperabilityTagSet");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(Modifier.isFinal(mods), "final modifier");
        check(c.getSuperclass().getName().equals("javax.imageio.plugins.tiff.TIFFTagSet"), "extends TIFFTagSet");
    }

    static void testBlackBox() throws Exception {
        ExifInteroperabilityTagSet s = ExifInteroperabilityTagSet.getInstance();
        check(s != null, "getInstance non-null");
        check(s == ExifInteroperabilityTagSet.getInstance(), "getInstance singleton");

        SortedSet<String> names = s.getTagNames();
        check(names.size() > 0, "has tag names");
        SortedSet<Integer> numbers = s.getTagNumbers();
        check(numbers.size() == names.size(), "names and numbers same cardinality");

        boolean allResolve = true;
        for (String name : names) {
            TIFFTag t = s.getTag(name);
            if (t == null || !t.getName().equals(name)) { allResolve = false; break; }
        }
        check(allResolve, "every tag name resolves");

        TIFFTag index = s.getTag("InteroperabilityIndex");
        check(index != null, "InteroperabilityIndex tag non-null");
        check(index.getNumber() == ExifInteroperabilityTagSet.TAG_INTEROPERABILITY_INDEX, "tag number constant matches");
        check(index.getNumber() == 1, "InteroperabilityIndex number 1");
        check(s.getTag(1) == index, "getTag(1) resolves InteroperabilityIndex");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
