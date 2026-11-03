/*
 * @test
 * @summary java.lang.reflect.AccessFlag.Location conformance: white-box structure + black-box behavior.
 * @run main LocationTest
 */
import java.lang.reflect.AccessFlag;
import java.lang.reflect.ClassFileFormatVersion;
import java.util.EnumSet;

public class LocationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural, retained from previous revision) ----
    static void testWhiteBox() {
        check(AccessFlag.Location.class.isEnum(), "is enum");
        check(AccessFlag.Location.CLASS.flags().contains(AccessFlag.PUBLIC), "CLASS.flags has PUBLIC");
        check(AccessFlag.Location.METHOD.flags().contains(AccessFlag.STATIC), "METHOD.flags has STATIC");
        check(AccessFlag.Location.FIELD.flags().contains(AccessFlag.VOLATILE), "FIELD.flags has VOLATILE");
        check(AccessFlag.Location.CLASS.flagsMask() != 0, "CLASS flagsMask non-zero");
        check(AccessFlag.Location.valueOf("MODULE") == AccessFlag.Location.MODULE, "valueOf");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // 1. exact constant set for JDK-27: 9 locations
        AccessFlag.Location[] all = AccessFlag.Location.values();
        check(all.length == 9, "9 locations, got " + all.length);
        check(all[0] == AccessFlag.Location.CLASS && all[1] == AccessFlag.Location.FIELD
              && all[2] == AccessFlag.Location.METHOD, "declaration order CLASS, FIELD, METHOD");
        try {
            AccessFlag.Location.valueOf("NO_SUCH");
            check(false, "unknown location name must fail");
        } catch (IllegalArgumentException e) {
            check(true, "IAE on unknown location");
        }

        // 2. exact flag sets per location (JVMS table, JDK-27)
        check(AccessFlag.Location.CLASS.flags().equals(EnumSet.of(
                AccessFlag.PUBLIC, AccessFlag.FINAL, AccessFlag.SUPER, AccessFlag.INTERFACE,
                AccessFlag.ABSTRACT, AccessFlag.SYNTHETIC, AccessFlag.ANNOTATION,
                AccessFlag.ENUM, AccessFlag.MODULE)),
              "CLASS exact (9: top-level classes take no PRIVATE/PROTECTED/STATIC)");
        check(AccessFlag.Location.FIELD.flags().equals(EnumSet.of(
                AccessFlag.PUBLIC, AccessFlag.PRIVATE, AccessFlag.PROTECTED, AccessFlag.STATIC,
                AccessFlag.FINAL, AccessFlag.VOLATILE, AccessFlag.TRANSIENT, AccessFlag.SYNTHETIC,
                AccessFlag.ENUM)), "FIELD exact (9, ENUM for enum constants)");
        check(AccessFlag.Location.METHOD.flags().equals(EnumSet.of(
                AccessFlag.PUBLIC, AccessFlag.PRIVATE, AccessFlag.PROTECTED, AccessFlag.STATIC,
                AccessFlag.FINAL, AccessFlag.SYNCHRONIZED, AccessFlag.BRIDGE, AccessFlag.VARARGS,
                AccessFlag.NATIVE, AccessFlag.ABSTRACT, AccessFlag.SYNTHETIC)),
              "METHOD exact (11, no STRICT at latest)");
        check(AccessFlag.Location.INNER_CLASS.flags().equals(EnumSet.of(
                AccessFlag.PUBLIC, AccessFlag.PRIVATE, AccessFlag.PROTECTED, AccessFlag.STATIC,
                AccessFlag.FINAL, AccessFlag.INTERFACE, AccessFlag.ABSTRACT, AccessFlag.SYNTHETIC,
                AccessFlag.ANNOTATION, AccessFlag.ENUM)), "INNER_CLASS exact (10, no SUPER/MODULE)");
        check(AccessFlag.Location.METHOD_PARAMETER.flags().equals(EnumSet.of(
                AccessFlag.FINAL, AccessFlag.SYNTHETIC, AccessFlag.MANDATED)),
              "METHOD_PARAMETER exact (3)");
        check(AccessFlag.Location.MODULE.flags().equals(EnumSet.of(
                AccessFlag.OPEN, AccessFlag.SYNTHETIC, AccessFlag.MANDATED)),
              "MODULE exact (OPEN/SYNTHETIC/MANDATED; MODULE flag itself sits on CLASS)");
        check(AccessFlag.Location.MODULE_REQUIRES.flags().equals(EnumSet.of(
                AccessFlag.TRANSITIVE, AccessFlag.STATIC_PHASE, AccessFlag.SYNTHETIC,
                AccessFlag.MANDATED)), "MODULE_REQUIRES exact");
        check(AccessFlag.Location.MODULE_EXPORTS.flags().equals(EnumSet.of(
                AccessFlag.SYNTHETIC, AccessFlag.MANDATED)), "MODULE_EXPORTS exact");
        check(AccessFlag.Location.MODULE_OPENS.flags().equals(
                AccessFlag.Location.MODULE_EXPORTS.flags()), "MODULE_OPENS mirrors EXPORTS");

        // 3. flagsMask is the union of member masks, and flags() decodes it exactly
        for (AccessFlag.Location loc : all) {
            int mask = loc.flagsMask();
            check(mask != 0, "nonzero mask " + loc);
            check(AccessFlag.maskToAccessFlags(mask, loc).equals(loc.flags()),
                  "mask decodes back to flag set " + loc);
            int sum = 0;
            for (AccessFlag f : loc.flags()) sum |= f.mask();
            check(sum == mask, "flagsMask equals OR of member masks " + loc);
        }

        // 4. version sensitivity: STRICT leaves METHOD at latest, present at RELEASE_8
        check(!AccessFlag.Location.METHOD.flags().contains(AccessFlag.STRICT),
              "STRICT gone from latest METHOD");
        check(AccessFlag.Location.METHOD.flags(ClassFileFormatVersion.RELEASE_8)
              .contains(AccessFlag.STRICT), "STRICT back at RELEASE_8 METHOD");
        check(AccessFlag.Location.METHOD.flagsMask(ClassFileFormatVersion.RELEASE_8)
              == (AccessFlag.Location.METHOD.flagsMask() | AccessFlag.STRICT.mask()),
              "RELEASE_8 METHOD mask adds STRICT bit (0x15ff -> 0x1dff)");
        check(AccessFlag.Location.METHOD.flagsMask(ClassFileFormatVersion.RELEASE_8)
              == 0x1dff, "RELEASE_8 METHOD mask literal");
        check(AccessFlag.Location.METHOD.flagsMask() == 0x15ff, "latest METHOD mask literal");
        check(AccessFlag.Location.CLASS.flags(ClassFileFormatVersion.RELEASE_5)
              .contains(AccessFlag.ANNOTATION), "ANNOTATION valid in RELEASE_5 CLASS (49.0+)");
        check(AccessFlag.Location.CLASS.flags(ClassFileFormatVersion.RELEASE_5)
              .contains(AccessFlag.MODULE) == false, "MODULE flag not in RELEASE_5 CLASS");
        check(AccessFlag.Location.METHOD.flags(ClassFileFormatVersion.RELEASE_8)
              .contains(AccessFlag.PUBLIC), "PUBLIC valid at METHOD in RELEASE_8 too");
        // every location keeps its set identical across recent versions except METHOD/STRICT
        check(AccessFlag.Location.FIELD.flags(ClassFileFormatVersion.RELEASE_8)
              .equals(AccessFlag.Location.FIELD.flags()), "FIELD stable across versions");

        // 5. ordering and enum plumbing
        check(AccessFlag.Location.CLASS.compareTo(AccessFlag.Location.FIELD) < 0, "ordinal order");
        check(AccessFlag.Location.MODULE_OPENS.getDeclaringClass() == AccessFlag.Location.class,
              "nested in AccessFlag");
    }

    public static void main(String[] args) {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
