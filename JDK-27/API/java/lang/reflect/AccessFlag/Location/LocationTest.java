/*
 * @test
 * @summary java.lang.reflect.AccessFlag.Location conformance: white-box structure + black-box behavior.
 *          Deepened: exact flagsMask literals, pre-module RELEASE_8 view, enum plumbing,
 *          per-location unmatched-bit rejection.
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

    // ---- Part 3: contract deepening (2026-11-05); every literal probed on JDK 27.0.1 ----
    static void testFlagsMaskLiterals() {
        check(AccessFlag.Location.CLASS.flagsMask() == 0x0000f631, "CLASS 0xf631");
        check(AccessFlag.Location.FIELD.flagsMask() == 0x000050df, "FIELD 0x50df");
        check(AccessFlag.Location.METHOD.flagsMask() == 0x000015ff, "METHOD 0x15ff");
        check(AccessFlag.Location.INNER_CLASS.flagsMask() == 0x0000761f, "INNER_CLASS 0x761f");
        check(AccessFlag.Location.METHOD_PARAMETER.flagsMask() == 0x00009010, "METHOD_PARAMETER 0x9010");
        check(AccessFlag.Location.MODULE.flagsMask() == 0x00009020, "MODULE 0x9020");
        check(AccessFlag.Location.MODULE_REQUIRES.flagsMask() == 0x00009060, "MODULE_REQUIRES 0x9060");
        check(AccessFlag.Location.MODULE_EXPORTS.flagsMask() == 0x00009000, "MODULE_EXPORTS 0x9000");
        check(AccessFlag.Location.MODULE_OPENS.flagsMask() == 0x00009000, "MODULE_OPENS 0x9000");
        // RELEASE_8 view: CLASS drops MODULE (0x7631); METHOD differs only by STRICT (0x1dff)
        check(AccessFlag.Location.CLASS.flagsMask(ClassFileFormatVersion.RELEASE_8) == 0x00007631,
              "CLASS at RELEASE_8 = 0x7631 (no MODULE)");
        check(AccessFlag.Location.INNER_CLASS.flagsMask(ClassFileFormatVersion.RELEASE_8)
              == AccessFlag.Location.INNER_CLASS.flagsMask(), "INNER_CLASS stable at RELEASE_8");
        check(AccessFlag.Location.FIELD.flagsMask(ClassFileFormatVersion.RELEASE_8)
              == AccessFlag.Location.FIELD.flagsMask(), "FIELD stable at RELEASE_8");
        check(AccessFlag.Location.METHOD_PARAMETER.flagsMask(ClassFileFormatVersion.RELEASE_8)
              == 0x00009010, "METHOD_PARAMETER stable at RELEASE_8");
    }

    static void testModuleLocationsPreJava9() {
        // the four module-info locations carry no flags at all before the module system
        AccessFlag.Location[] moduleLocs = {AccessFlag.Location.MODULE,
              AccessFlag.Location.MODULE_REQUIRES, AccessFlag.Location.MODULE_EXPORTS,
              AccessFlag.Location.MODULE_OPENS};
        for (AccessFlag.Location loc : moduleLocs) {
            check(loc.flags(ClassFileFormatVersion.RELEASE_8).isEmpty(),
                  "no flags at RELEASE_8: " + loc);
            check(loc.flagsMask(ClassFileFormatVersion.RELEASE_8) == 0,
                  "flagsMask 0 at RELEASE_8: " + loc);
            check(AccessFlag.maskToAccessFlags(0, loc, ClassFileFormatVersion.RELEASE_8).isEmpty(),
                  "zero mask decodes to empty pre-modules: " + loc);
            check(!loc.flags().isEmpty(), "populated again at latest: " + loc);
        }
    }

    static void testEnumPlumbing() {
        AccessFlag.Location[] all = AccessFlag.Location.values();
        check(EnumSet.allOf(AccessFlag.Location.class).size() == all.length,
              "EnumSet.allOf agrees with values");
        for (AccessFlag.Location loc : all) {
            check(AccessFlag.Location.valueOf(loc.name()) == loc, "valueOf round trip " + loc);
            check(loc.toString().equals(loc.name()), "toString == name " + loc);
            check(loc.getDeclaringClass() == AccessFlag.Location.class, "declaring class " + loc);
            check(!loc.flags().isEmpty(), "non-empty flag set " + loc);
            check(loc.flagsMask() != 0, "non-zero mask " + loc);
        }
        check(all[0] == AccessFlag.Location.CLASS, "first constant CLASS");
        check(all[all.length - 1] == AccessFlag.Location.MODULE_OPENS, "last constant MODULE_OPENS");
        check(AccessFlag.Location.CLASS.ordinal() == 0
              && AccessFlag.Location.MODULE_OPENS.ordinal() == 8, "ordinals 0 and 8");
    }

    static void expectUnmatched(int mask, AccessFlag.Location loc) {
        try {
            AccessFlag.maskToAccessFlags(mask, loc);
            check(false, "mask 0x" + Integer.toHexString(mask) + " must be unmatched at " + loc);
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("Unmatched bit"),
                  "unmatched-bit IAE at " + loc);
        }
    }

    static void testUnmatchedBitPerLocation() {
        // one provably-absent bit for each of the 9 locations
        expectUnmatched(0x0008, AccessFlag.Location.CLASS);            // STATIC
        expectUnmatched(0x0020, AccessFlag.Location.FIELD);            // no 0x20 flag at FIELD
        expectUnmatched(0x4000, AccessFlag.Location.METHOD);           // ENUM
        expectUnmatched(0x0020, AccessFlag.Location.INNER_CLASS);      // SUPER
        expectUnmatched(0x0001, AccessFlag.Location.METHOD_PARAMETER); // PUBLIC
        expectUnmatched(0x0001, AccessFlag.Location.MODULE);
        expectUnmatched(0x0001, AccessFlag.Location.MODULE_REQUIRES);
        expectUnmatched(0x0020, AccessFlag.Location.MODULE_EXPORTS);   // TRANSITIVE only on REQUIRES
        expectUnmatched(0x0020, AccessFlag.Location.MODULE_OPENS);
    }

    public static void main(String[] args) {
        testWhiteBox();
        testBlackBox();
        testFlagsMaskLiterals();
        testModuleLocationsPreJava9();
        testEnumPlumbing();
        testUnmatchedBitPerLocation();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
