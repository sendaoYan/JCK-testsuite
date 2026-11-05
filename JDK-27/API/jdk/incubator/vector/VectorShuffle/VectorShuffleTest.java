/*
 * @test
 * @summary jdk.incubator.vector.VectorShuffle conformance: white-box structure + black-box behavior.
 * @modules jdk.incubator.vector
 * @run main VectorShuffleTest
 */

import jdk.incubator.vector.*;
import java.lang.reflect.*;

public class VectorShuffleTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.incubator.vector.VectorShuffle");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(hasMethod(c, "length"), "length");
        check(hasMethod(c, "laneSource"), "laneSource");
        check(hasMethod(c, "vectorSpecies"), "vectorSpecies");
        check(hasMethod(c, "iota"), "iota");
        check(hasMethod(c, "fromValues"), "fromValues");
    }

    static void testBlackBox() {
        VectorSpecies<Integer> sp = IntVector.SPECIES_256;
        int n = sp.length();
        VectorShuffle<Integer> iota = VectorShuffle.iota(sp, 0, 1, false);
        check(iota.length() == n, "iota length");
        check(iota.laneSource(0) == 0, "iota laneSource 0");
        check(iota.laneSource(n - 1) == n - 1, "iota laneSource last");
        check(iota.vectorSpecies() == sp, "iota species");
        int[] src = new int[n];
        for (int i = 0; i < n; i++) src[i] = n - 1 - i;
        VectorShuffle<Integer> rev = VectorShuffle.fromValues(sp, src);
        check(rev.laneSource(0) == n - 1, "fromValues reverse first");
        check(rev.laneSource(n - 1) == 0, "fromValues reverse last");
        VectorShuffle<Integer> wrap = iota.rearrange(rev);
        check(wrap.laneSource(0) == n - 1, "rearrange applied");
    }


    static void testDeepen() {
        VectorSpecies<Integer> sp = IntVector.SPECIES_256;
        int n = sp.length();
        VectorShuffle<Integer> sh = VectorShuffle.fromValues(sp, 3, 1, 2, 0, 3, 1, 2, 0);
        check(sh.toString().equals("Shuffle[3, 1, 2, 0, 3, 1, 2, 0]"), "toString exact");
        int[] arr = sh.toArray();
        check(arr.length == n && arr[0] == 3 && arr[7] == 0, "toArray round-trip");
        check(((IntVector) sh.toVector()).lane(0) == 3, "toVector lanes are source indexes");
        check(sh.laneIsValid().allTrue(), "all in-range valid");
        VectorShuffle<Integer> bad = VectorShuffle.fromValues(sp, -1, 0, 0, 0, 0, 0, 0, 0);
        check(bad.laneSource(0) == -1, "negative source preserved unwrapped");
        check(bad.laneIsValid().toString().equals("Mask[.TTTTTTT]"), "laneIsValid marks -1 invalid");
        check(bad.wrapIndexes().laneSource(0) == n - 1, "wrapIndexes -1 wraps to n-1");
        boolean ioobe = false;
        try { bad.checkIndexes(); } catch (IndexOutOfBoundsException e) { ioobe = true; }
        check(ioobe, "checkIndexes throws IndexOutOfBoundsException");
        check(VectorShuffle.iota(sp, 6, 2, true).toArray()[0] == 6
                && VectorShuffle.iota(sp, 6, 2, true).toArray()[1] == 0, "iota step 2 wraps 8 to 0");
        int[] src = {5, 6, 7, 0, 1, 2, 3, 4};
        check(VectorShuffle.fromArray(sp, src, 0).laneSource(0) == 5, "fromArray");
        check(VectorShuffle.fromOp(sp, i -> (i + 1) % n).laneSource(0) == 1, "fromOp lambda");
        VectorShuffle<Integer> iota = VectorShuffle.iota(sp, 0, 1, false);
        check(iota.rearrange(sh).laneSource(0) == 3, "shuffle rearrange composes sources");
        check(sh.equals(VectorShuffle.fromValues(sp, 3, 1, 2, 0, 3, 1, 2, 0)), "equals value");
        check(sh.hashCode() == VectorShuffle.fromValues(sp, 3, 1, 2, 0, 3, 1, 2, 0).hashCode(), "hashCode consistent");
        VectorShuffle<Integer> zip = VectorShuffle.makeZip(sp, 0);
        check(zip.laneSource(0) == 0 && zip.laneSource(1) == -n, "makeZip even/odd halves");
        VectorShuffle<Integer> unzip = VectorShuffle.makeUnzip(sp, 0);
        check(unzip.laneSource(0) == 0 && unzip.laneSource(1) == 2
                && unzip.laneSource(2) == 4 && unzip.laneSource(3) == 6, "makeUnzip even indexes");
        check(sh.cast(DoubleVector.SPECIES_512).toString().equals(sh.toString()), "cast same lane count");
        boolean iae = false;
        try { sh.cast(IntVector.SPECIES_64); } catch (IllegalArgumentException e) {
            iae = e.getMessage().equals("VectorShuffle length and species length differ");
        }
        check(iae, "cast shorter species IAE exact message");
        int[] out = new int[n];
        sh.intoArray(out, 0);
        for (int i = 0; i < n; i++) check(out[i] == arr[i], "intoArray lane " + i);
        // rearrange data vector through shuffle (gather)
        IntVector data = IntVector.fromArray(sp, new int[] {10, 11, 12, 13, 14, 15, 16, 17}, 0);
        IntVector gathered = data.rearrange(sh);
        check(gathered.lane(0) == 13 && gathered.lane(1) == 11 && gathered.lane(3) == 10, "rearrange gathers data[sh[i]]");
    }

    public static void main(String[] args) throws Exception {
        testDeepen();

        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
