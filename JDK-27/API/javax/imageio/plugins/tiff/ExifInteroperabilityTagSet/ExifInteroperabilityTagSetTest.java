/*
 * @test
 * @summary javax.imageio.plugins.tiff.ExifInteroperabilityTagSet conformance: white-box structure + black-box behavior.
 * @run main ExifInteroperabilityTagSetTest
 */

import java.lang.reflect.Modifier;
import java.util.SortedSet;
import javax.imageio.plugins.tiff.ExifInteroperabilityTagSet;
import javax.imageio.plugins.tiff.ExifTIFFTagSet;
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

    // ---- Part 3 (deepened): constants, types, boundary lookups ----
    static void testDeepened() throws Exception {
        // exact constant values (probed via javap -constants)
        check(ExifInteroperabilityTagSet.TAG_INTEROPERABILITY_INDEX == 1,
              "TAG_INTEROPERABILITY_INDEX == 1");
        check(ExifInteroperabilityTagSet.INTEROPERABILITY_INDEX_R98.equals("R98"),
              "INTEROPERABILITY_INDEX_R98 R98");
        check(ExifInteroperabilityTagSet.INTEROPERABILITY_INDEX_THM.equals("THM"),
              "INTEROPERABILITY_INDEX_THM THM");

        ExifInteroperabilityTagSet s = ExifInteroperabilityTagSet.getInstance();
        // exactly one tag, number 1, ASCII (probed)
        check(s.getTagNumbers().size() == 1, "exactly one tag");
        check(s.getTagNames().size() == 1, "exactly one name");
        TIFFTag t = s.getTag("InteroperabilityIndex");
        check(t != null, "tag resolves");
        check(t.getNumber() == 1, "tag number 1");
        check(s.getTag(1) == t, "number 1 resolves same instance");
        check(t.isDataTypeOK(TIFFTag.TIFF_ASCII), "InteroperabilityIndex ASCII");
        check(!t.isDataTypeOK(TIFFTag.TIFF_SHORT), "InteroperabilityIndex rejects SHORT");
        check(!t.isDataTypeOK(TIFFTag.TIFF_UNDEFINED), "InteroperabilityIndex rejects UNDEFINED");
        check(t.getCount() == -1, "count undefined");
        check(!t.isIFDPointer(), "not an IFD pointer");
        check(!t.hasValueNames(), "no value names");
        check(t.getTagSet() == null, "no tag set back-reference");
        // boundary lookups miss cleanly
        check(s.getTag(0) == null, "getTag(0) null");
        check(s.getTag(2) == null, "getTag(2) null");
        check(s.getTag("R98") == null, "getTag(R98) null");
        check(s.getTag("") == null, "getTag(\"\") null");
        // sorted view boundaries on the singleton set
        check(s.getTagNumbers().first() == 1 && s.getTagNumbers().last() == 1,
              "singleton first==last==1");
        check(s.getTagNames().first().equals("InteroperabilityIndex"), "single name first");
        // a field carrying the documented value round-trips through its tag
        javax.imageio.plugins.tiff.TIFFField f =
            new javax.imageio.plugins.tiff.TIFFField(t, TIFFTag.TIFF_ASCII, 1,
                                                     new String[]{ExifInteroperabilityTagSet.INTEROPERABILITY_INDEX_R98});
        check(f.getAsString(0).equals("R98"), "R98 string round-trip");
        check(f.getValueAsString(0).equals("R98"), "R98 value-as-string");
        // THM variant round-trips identically
        javax.imageio.plugins.tiff.TIFFField f2 =
            new javax.imageio.plugins.tiff.TIFFField(t, TIFFTag.TIFF_ASCII, 1,
                                                     new String[]{ExifInteroperabilityTagSet.INTEROPERABILITY_INDEX_THM});
        check(f2.getAsString(0).equals("THM"), "THM string round-trip");
        // native node round-trip through createFromMetadataNode (probed semantics)
        javax.imageio.plugins.tiff.TIFFField back =
            javax.imageio.plugins.tiff.TIFFField.createFromMetadataNode(
                s, f.getAsNativeNode());
        check(back.getType() == TIFFTag.TIFF_ASCII, "from-node ASCII type");
        check(back.getAsString(0).equals("R98"), "from-node R98 value");
        check(back.getTag() == t, "from-node resolves registered tag");
        // the set inter-operates with ExifTIFFTagSet's interop IFD pointer number
        check(ExifTIFFTagSet.TAG_INTEROPERABILITY_IFD_POINTER == 40965,
              "ExifTIFFTagSet interop pointer constant 40965");
        // sorted-set view of the single name
        check(s.getTagNames().first().equals(s.getTagNames().last()),
              "single name first == last");
        // removing and re-adding through a param round-trips membership
        javax.imageio.plugins.tiff.TIFFImageReadParam p =
            new javax.imageio.plugins.tiff.TIFFImageReadParam();
        p.addAllowedTagSet(s);
        check(p.getAllowedTagSets().contains(s), "param add interop set");
        p.removeAllowedTagSet(s);
        check(!p.getAllowedTagSets().contains(s), "param remove interop set");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepened();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
