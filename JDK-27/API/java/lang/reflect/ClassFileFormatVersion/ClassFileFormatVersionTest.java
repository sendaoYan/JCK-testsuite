/*
 * @test
 * @summary java.lang.reflect.ClassFileFormatVersion conformance: white-box structure + black-box behavior.
 *          Deepened: exact major table (45..71), runtimeVersion nulls for pre-6 releases,
 *          fromMajor boundary messages, valueOf(Runtime.Version) map.
 * @run main ClassFileFormatVersionTest
 */
import java.lang.reflect.ClassFileFormatVersion;

public class ClassFileFormatVersionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural, retained from previous revision) ----
    static void testWhiteBox() {
        check(ClassFileFormatVersion.class.isEnum(), "is enum");
        ClassFileFormatVersion latest = ClassFileFormatVersion.latest();
        check(latest.major() >= 65, "latest major >= 65");
        check(latest.runtimeVersion().feature() == Runtime.version().feature(), "runtimeVersion matches");
        check(ClassFileFormatVersion.fromMajor(latest.major()) == latest, "fromMajor roundtrip");
        check(ClassFileFormatVersion.valueOf(Runtime.version()) == latest, "valueOf(Runtime.version)");
        check(ClassFileFormatVersion.valueOf("RELEASE_8") == ClassFileFormatVersion.RELEASE_8, "valueOf RELEASE_8");
        check(ClassFileFormatVersion.RELEASE_8.major() == 52, "RELEASE_8 major 52");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ClassFileFormatVersion latest = ClassFileFormatVersion.latest();

        // 1. JDK-27 exact constants: 28 entries, latest = RELEASE_27 = major 71
        ClassFileFormatVersion[] all = ClassFileFormatVersion.values();
        check(all.length == 28, "28 constants in JDK-27, got " + all.length);
        check(latest == ClassFileFormatVersion.RELEASE_27, "latest is RELEASE_27");
        check(latest.major() == 71, "RELEASE_27 major is 71");
        check(all[all.length - 1] == latest, "values() ends with latest");
        check(all[0] == ClassFileFormatVersion.RELEASE_0, "values() starts at RELEASE_0");
        check(ClassFileFormatVersion.RELEASE_0.major() == 45, "RELEASE_0 major 45");
        check(ClassFileFormatVersion.RELEASE_1.major() == 45, "RELEASE_1 major 45");

        // 2. name/toString/valueOf round trips
        for (ClassFileFormatVersion v : all) {
            check(ClassFileFormatVersion.valueOf(v.name()) == v, "valueOf name round trip " + v);
            check(v.toString() != null && v.name().startsWith("RELEASE_"), "name shape " + v);
        }
        check(ClassFileFormatVersion.valueOf("RELEASE_27") == latest, "valueOf RELEASE_27");
        try {
            ClassFileFormatVersion.valueOf("RELEASE_999");
            check(false, "unknown constant must fail");
        } catch (IllegalArgumentException e) {
            check(true, "IAE on unknown constant name");
        }

        // 3. fromMajor mapping: identity for majors > 45, alias for the shared major 45
        for (ClassFileFormatVersion v : all) {
            if (v == ClassFileFormatVersion.RELEASE_0) continue; // shares 45 with RELEASE_1
            check(ClassFileFormatVersion.fromMajor(v.major()) == v,
                  "fromMajor identity for " + v + " (" + v.major() + ")");
        }
        check(ClassFileFormatVersion.fromMajor(45) == ClassFileFormatVersion.RELEASE_1,
              "fromMajor(45) aliases to RELEASE_1 (not RELEASE_0)");
        check(ClassFileFormatVersion.fromMajor(ClassFileFormatVersion.RELEASE_0.major())
              == ClassFileFormatVersion.RELEASE_1, "RELEASE_0 major aliases forward");
        check(ClassFileFormatVersion.fromMajor(52) == ClassFileFormatVersion.RELEASE_8, "fromMajor(52)");
        check(ClassFileFormatVersion.fromMajor(71) == latest, "fromMajor(71)");
        try {
            ClassFileFormatVersion.fromMajor(-1);
            check(false, "negative major must fail");
        } catch (IllegalArgumentException e) {
            check(true, "IAE on negative major");
        }
        try {
            ClassFileFormatVersion.fromMajor(999);
            check(false, "too-large major must fail");
        } catch (IllegalArgumentException e) {
            check(true, "IAE on unknown major 999");
        }

        // 4. runtimeVersion linkage: major + 44 feature rule
        check(ClassFileFormatVersion.RELEASE_8.runtimeVersion().feature() == 8, "RELEASE_8 feature 8");
        check(ClassFileFormatVersion.RELEASE_27.runtimeVersion().feature() == 27, "RELEASE_27 feature 27");
        check(ClassFileFormatVersion.valueOf(Runtime.Version.parse("27.1")) == latest,
              "valueOf parses Runtime.Version build info");
        check(ClassFileFormatVersion.valueOf(Runtime.Version.parse("8")) == ClassFileFormatVersion.RELEASE_8,
              "valueOf parses legacy version string");
        check(latest.runtimeVersion().toString().equals(String.valueOf(Runtime.version().feature())),
              "latest runtimeVersion carries feature only");
        check(Runtime.version().compareTo(latest.runtimeVersion()) >= 0,
              "this runtime is not older than latest.runtimeVersion()");

        // 5. Comparable ordering: ascending majors, contiguous stride 1 (RELEASE_0/1 share 45)
        for (int i = 1; i < all.length; i++) {
            check(all[i - 1].compareTo(all[i]) < 0, "ordinal ascending " + all[i - 1] + " < " + all[i]);
            if (all[i - 1] == ClassFileFormatVersion.RELEASE_0) {
                check(all[i].major() == all[i - 1].major(), "RELEASE_0/1 share major 45");
            } else {
                check(all[i].major() == all[i - 1].major() + 1, "contiguous majors at " + all[i]);
            }
        }
        check(ClassFileFormatVersion.RELEASE_8.compareTo(ClassFileFormatVersion.RELEASE_17) < 0,
              "RELEASE_8 older than RELEASE_17");
        check(ClassFileFormatVersion.RELEASE_17.compareTo(ClassFileFormatVersion.RELEASE_8) > 0,
              "compareTo antisymmetric");

        // 6. decodes real class files: read the major version straight from bytes
        int own = readMajor("/java/lang/String.class");
        check(own == latest.major(), "java.base String distributed at major 71");
        check(ClassFileFormatVersion.fromMajor(own) == latest, "fromMajor decodes real java.base class");
        int mine = readMajor("/" + ClassFileFormatVersionTest.class.getName().replace('.', '/') + ".class");
        check(mine == latest.major(), "own class file compiled at latest major");
        check(ClassFileFormatVersion.fromMajor(mine) == latest, "fromMajor decodes own class file");

        // 7. enum plumbing
        check(ClassFileFormatVersion.RELEASE_27.getDeclaringClass() == ClassFileFormatVersion.class,
              "declaring class");
        check(latest.name().equals("RELEASE_27"), "latest name RELEASE_27");
        java.util.EnumSet<ClassFileFormatVersion> set = java.util.EnumSet.range(
                ClassFileFormatVersion.RELEASE_8, ClassFileFormatVersion.RELEASE_11);
        check(set.size() == 4, "EnumSet.range spans 4 releases");
    }

    static int readMajor(String resource) throws Exception {
        try (java.io.InputStream in =
                ClassFileFormatVersionTest.class.getResourceAsStream(resource)) {
            if (in == null) throw new IllegalStateException("missing resource " + resource);
            byte[] b = in.readAllBytes();
            check(b.length >= 8 && b[0] == (byte) 0xCA && b[1] == (byte) 0xFE
                  && b[2] == (byte) 0xBA && b[3] == (byte) 0xBE, "magic CAFEBABE " + resource);
            return ((b[6] & 0xff) << 8) | (b[7] & 0xff);
        }
    }

    // ---- Part 3: contract deepening (2026-11-05); every literal probed on JDK 27.0.1 ----
    static void testExactMajorTable() {
        // full 28-row major table, locked by probe
        int[] majors = {45, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60,
                        61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71};
        ClassFileFormatVersion[] all = ClassFileFormatVersion.values();
        check(all.length == majors.length, "table has one row per constant");
        for (int i = 0; i < majors.length; i++) {
            check(all[i].major() == majors[i], "major row " + i + ": " + all[i] + " == " + majors[i]);
            check(all[i].name().equals("RELEASE_" + i), "name RELEASE_" + i);
            check(all[i].toString().equals("RELEASE_" + i), "toString RELEASE_" + i);
            check(all[i].ordinal() == i, "ordinal identity " + all[i]);
            if (i >= 1)
                check(all[i].major() == 44 + i, "major == 44 + ordinal at " + all[i]);
        }
        check(all[0].major() == all[1].major(), "RELEASE_0/1 share major 45");
        check(ClassFileFormatVersion.RELEASE_0.major() != 44 + 0, "RELEASE_0 breaks the 44+n rule");
    }

    static void testRuntimeVersionNulls() {
        // releases before 6 have no Runtime.Version representation (1.x version scheme)
        ClassFileFormatVersion[] pre = {ClassFileFormatVersion.RELEASE_0,
              ClassFileFormatVersion.RELEASE_1, ClassFileFormatVersion.RELEASE_2,
              ClassFileFormatVersion.RELEASE_3, ClassFileFormatVersion.RELEASE_4,
              ClassFileFormatVersion.RELEASE_5};
        for (ClassFileFormatVersion v : pre)
            check(v.runtimeVersion() == null, "runtimeVersion null for " + v);
        // from RELEASE_6 on, runtimeVersion().feature() equals the release number
        for (int n = 6; n <= 27; n++)
            check(ClassFileFormatVersion.valueOf("RELEASE_" + n).runtimeVersion().feature() == n,
                  "runtimeVersion feature " + n);
        check(ClassFileFormatVersion.RELEASE_6.runtimeVersion().toString().equals("6"),
              "RELEASE_6 runtimeVersion string");
    }

    static void testFromMajorBoundaries() {
        int[] bad = {44, 0, 72, -1, Integer.MAX_VALUE};
        for (int m : bad) {
            try {
                ClassFileFormatVersion.fromMajor(m);
                check(false, "fromMajor(" + m + ") must fail");
            } catch (IllegalArgumentException e) {
                check(e.getMessage() != null
                      && e.getMessage().contains("Out of range major class file version"),
                      "out-of-range message for " + m + ": " + e.getMessage());
            }
        }
        check(ClassFileFormatVersion.fromMajor(45) == ClassFileFormatVersion.RELEASE_1,
              "lower bound 45 maps to RELEASE_1");
        check(ClassFileFormatVersion.fromMajor(71) == ClassFileFormatVersion.RELEASE_27,
              "upper bound 71 maps to RELEASE_27");
    }

    static void testValueOfRuntimeVersionMap() {
        check(ClassFileFormatVersion.valueOf(Runtime.Version.parse("6"))
              == ClassFileFormatVersion.RELEASE_6, "rv 6");
        check(ClassFileFormatVersion.valueOf(Runtime.Version.parse("5"))
              == ClassFileFormatVersion.RELEASE_5, "rv 5");
        check(ClassFileFormatVersion.valueOf(Runtime.Version.parse("26"))
              == ClassFileFormatVersion.RELEASE_26, "rv 26");
        check(ClassFileFormatVersion.valueOf(Runtime.Version.parse("27.1"))
              == ClassFileFormatVersion.RELEASE_27, "rv 27.1 (update ignored)");
        check(ClassFileFormatVersion.valueOf(Runtime.Version.parse("27+42"))
              == ClassFileFormatVersion.RELEASE_27, "rv 27+42 (build ignored)");
        check(ClassFileFormatVersion.valueOf(Runtime.Version.parse("27-pre"))
              == ClassFileFormatVersion.RELEASE_27, "rv 27-pre (pre ignored)");
        try {
            ClassFileFormatVersion.valueOf(Runtime.Version.parse("28"));
            check(false, "rv 28 must fail");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("RELEASE_28"),
                  "message names RELEASE_28: " + e.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testExactMajorTable();
        testRuntimeVersionNulls();
        testFromMajorBoundaries();
        testValueOfRuntimeVersionMap();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
