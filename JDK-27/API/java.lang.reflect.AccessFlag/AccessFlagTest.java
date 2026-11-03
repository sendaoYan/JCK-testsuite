/*
 * @test
 * @summary java.lang.reflect.AccessFlag conformance: white-box structure + black-box behavior.
 * @run main AccessFlagTest
 */
import java.lang.reflect.AccessFlag;
import java.lang.reflect.ClassFileFormatVersion;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.EnumSet;
import java.util.Set;

public class AccessFlagTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static class Targets {
        private static final int LOCK = 1;
        public volatile long shared;
        public transient int tmp;
        public synchronized int compute() { return 1; }
        public int varargsSum(int... xs) { return 0; }
    }

    // ---- Part 1: white-box (structural, retained from previous revision) ----
    static void testWhiteBox() {
        check(AccessFlag.class.isEnum(), "is enum");
        check(AccessFlag.PUBLIC.mask() == Modifier.PUBLIC, "PUBLIC mask");
        check(AccessFlag.STATIC.mask() == Modifier.STATIC, "STATIC mask");
        check(AccessFlag.STATIC.sourceModifier(), "STATIC sourceModifier");
        check(!AccessFlag.SYNTHETIC.sourceModifier(), "SYNTHETIC not source");
        check(AccessFlag.PUBLIC.locations().contains(AccessFlag.Location.CLASS), "PUBLIC locations CLASS");
        check(AccessFlag.maskToAccessFlags(Modifier.PUBLIC, AccessFlag.Location.CLASS).contains(AccessFlag.PUBLIC),
              "maskToAccessFlags");
        check(AccessFlag.values().length == 23, "23 constants in JDK-27");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. enum mechanics: values/valueOf/name round trips
        AccessFlag[] all = AccessFlag.values();
        check(EnumSet.allOf(AccessFlag.class).size() == all.length, "EnumSet agrees with values");
        for (AccessFlag f : all) {
            check(AccessFlag.valueOf(f.name()) == f, "valueOf round trip " + f);
            check(f.toString() != null && f.getDeclaringClass() == AccessFlag.class,
                  "declared in AccessFlag " + f);
        }
        check(all[0] == AccessFlag.PUBLIC, "first constant PUBLIC");
        check(AccessFlag.valueOf("MODULE") == AccessFlag.MODULE, "valueOf MODULE");
        try {
            AccessFlag.valueOf("NO_SUCH_FLAG");
            check(false, "bogus valueOf must fail");
        } catch (IllegalArgumentException e) {
            check(true, "IAE on unknown name");
        }
        check(AccessFlag.PUBLIC.compareTo(AccessFlag.PRIVATE) < 0, "ordinal ordering");
        check(all[all.length - 1] == AccessFlag.MODULE, "last constant MODULE");

        // 2. masks align with Modifier constants
        check(AccessFlag.PRIVATE.mask() == Modifier.PRIVATE, "PRIVATE mask");
        check(AccessFlag.PROTECTED.mask() == Modifier.PROTECTED, "PROTECTED mask");
        check(AccessFlag.FINAL.mask() == Modifier.FINAL, "FINAL mask");
        check(AccessFlag.SYNCHRONIZED.mask() == Modifier.SYNCHRONIZED, "SYNCHRONIZED mask");
        check(AccessFlag.VOLATILE.mask() == Modifier.VOLATILE, "VOLATILE mask");
        check(AccessFlag.TRANSIENT.mask() == Modifier.TRANSIENT, "TRANSIENT mask");
        check(AccessFlag.NATIVE.mask() == Modifier.NATIVE, "NATIVE mask");
        check(AccessFlag.INTERFACE.mask() == Modifier.INTERFACE, "INTERFACE mask");
        check(AccessFlag.ABSTRACT.mask() == Modifier.ABSTRACT, "ABSTRACT mask");
        check(AccessFlag.STRICT.mask() == Modifier.STRICT, "STRICT mask");
        for (AccessFlag f : all) check(f.mask() != 0, "nonzero mask " + f);

        // 3. sourceModifier partition
        check(AccessFlag.PRIVATE.sourceModifier() && AccessFlag.PROTECTED.sourceModifier()
              && AccessFlag.FINAL.sourceModifier() && AccessFlag.SYNCHRONIZED.sourceModifier()
              && AccessFlag.VOLATILE.sourceModifier() && AccessFlag.TRANSIENT.sourceModifier()
              && AccessFlag.NATIVE.sourceModifier() && AccessFlag.ABSTRACT.sourceModifier()
              && AccessFlag.STRICT.sourceModifier(), "all source modifiers true");
        check(!AccessFlag.SUPER.sourceModifier() && !AccessFlag.BRIDGE.sourceModifier()
              && !AccessFlag.VARARGS.sourceModifier() && !AccessFlag.SYNTHETIC.sourceModifier()
              && !AccessFlag.MANDATED.sourceModifier() && !AccessFlag.MODULE.sourceModifier()
              && !AccessFlag.OPEN.sourceModifier() && !AccessFlag.TRANSITIVE.sourceModifier()
              && !AccessFlag.STATIC_PHASE.sourceModifier(), "non-source flags false");
        check(!AccessFlag.ANNOTATION.sourceModifier() && !AccessFlag.ENUM.sourceModifier(),
              "ANNOTATION/ENUM not source modifiers");

        // 4. maskToAccessFlags round-trips real reflected members
        Field lock = Targets.class.getDeclaredField("LOCK");
        Set<AccessFlag> lockFlags = AccessFlag.maskToAccessFlags(lock.getModifiers(), AccessFlag.Location.FIELD);
        check(lockFlags.equals(EnumSet.of(AccessFlag.PRIVATE, AccessFlag.STATIC, AccessFlag.FINAL)),
              "private static final field flags exact set");
        check(lock.accessFlags().equals(lockFlags), "Field.accessFlags agrees with mask");
        Field vol = Targets.class.getDeclaredField("shared");
        check(AccessFlag.maskToAccessFlags(vol.getModifiers(), AccessFlag.Location.FIELD)
              .equals(EnumSet.of(AccessFlag.PUBLIC, AccessFlag.VOLATILE)), "public volatile exact set");
        Method comp = Targets.class.getMethod("compute");
        check(AccessFlag.maskToAccessFlags(comp.getModifiers(), AccessFlag.Location.METHOD)
              .equals(EnumSet.of(AccessFlag.PUBLIC, AccessFlag.SYNCHRONIZED)),
              "public synchronized exact set");
        Constructor<?> ctor = Targets.class.getDeclaredConstructor();
        check(AccessFlag.maskToAccessFlags(ctor.getModifiers(), AccessFlag.Location.METHOD)
              .isEmpty(), "package-private ctor empty set");
        check(ctor.accessFlags().isEmpty(), "Constructor.accessFlags empty for package-private");
        check(AccessFlag.maskToAccessFlags(0, AccessFlag.Location.CLASS).isEmpty(), "zero mask empty set");

        // 5. location sensitivity: shared bits resolve by location, unmatched bits throw IAE
        check(AccessFlag.maskToAccessFlags(Modifier.VOLATILE, AccessFlag.Location.METHOD)
              .equals(EnumSet.of(AccessFlag.BRIDGE)),
              "0x40 is BRIDGE at METHOD (shares bit with VOLATILE)");
        check(AccessFlag.maskToAccessFlags(Modifier.TRANSIENT, AccessFlag.Location.METHOD)
              .equals(EnumSet.of(AccessFlag.VARARGS)),
              "0x80 is VARARGS at METHOD (shares bit with TRANSIENT)");
        check(AccessFlag.maskToAccessFlags(Modifier.VOLATILE, AccessFlag.Location.FIELD)
              .equals(EnumSet.of(AccessFlag.VOLATILE)), "0x40 is VOLATILE at FIELD");
        check(AccessFlag.maskToAccessFlags(0x20, AccessFlag.Location.CLASS)
              .equals(EnumSet.of(AccessFlag.SUPER)), "0x20 is SUPER at CLASS");
        try {
            AccessFlag.maskToAccessFlags(Modifier.SYNCHRONIZED, AccessFlag.Location.FIELD);
            check(false, "SYNCHRONIZED bit at FIELD must be rejected");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("Unmatched bit"),
                  "unmatched bit IAE message: " + e.getMessage());
        }
        try {
            AccessFlag.maskToAccessFlags(Modifier.NATIVE, AccessFlag.Location.FIELD);
            check(false, "NATIVE at FIELD must be rejected");
        } catch (IllegalArgumentException e) {
            check(true, "NATIVE at FIELD IAE");
        }
        check(AccessFlag.maskToAccessFlags(Modifier.PUBLIC, AccessFlag.Location.METHOD)
              .equals(EnumSet.of(AccessFlag.PUBLIC)), "PUBLIC valid for METHOD");
        check(AccessFlag.maskToAccessFlags(Modifier.PUBLIC | Modifier.STATIC, AccessFlag.Location.METHOD)
              .equals(EnumSet.of(AccessFlag.PUBLIC, AccessFlag.STATIC)), "PUBLIC|STATIC at METHOD");
        // STRICT is obsolete in latest class files: rejected now, valid at RELEASE_8
        try {
            AccessFlag.maskToAccessFlags(Modifier.STRICT, AccessFlag.Location.METHOD);
            check(false, "STRICT at METHOD (latest) must be rejected");
        } catch (IllegalArgumentException e) {
            check(true, "STRICT obsolete at latest");
        }
        check(AccessFlag.maskToAccessFlags(Modifier.STRICT, AccessFlag.Location.METHOD,
              ClassFileFormatVersion.RELEASE_8).equals(EnumSet.of(AccessFlag.STRICT)),
              "STRICT valid at RELEASE_8 METHOD");

        // 6. locations: current and versioned
        check(AccessFlag.PUBLIC.locations().containsAll(
                java.util.Arrays.asList(AccessFlag.Location.CLASS, AccessFlag.Location.METHOD,
                        AccessFlag.Location.FIELD)), "PUBLIC across CLASS/METHOD/FIELD");
        check(AccessFlag.OPEN.locations().equals(EnumSet.of(AccessFlag.Location.MODULE)),
              "OPEN only at MODULE");
        check(AccessFlag.MODULE.locations().equals(EnumSet.of(AccessFlag.Location.CLASS)),
              "MODULE flag lives on module-info CLASS");
        check(AccessFlag.TRANSITIVE.locations()
              .equals(EnumSet.of(AccessFlag.Location.MODULE_REQUIRES)),
              "TRANSITIVE only at MODULE_REQUIRES");
        check(AccessFlag.STRICT.locations().isEmpty(),
              "STRICT has no location in latest class file (obsolete)");
        check(!AccessFlag.STRICT.locations(ClassFileFormatVersion.RELEASE_8).isEmpty(),
              "STRICT valid at RELEASE_8");
        check(AccessFlag.SUPER.locations().equals(EnumSet.of(AccessFlag.Location.CLASS)),
              "SUPER only at CLASS");
        check(AccessFlag.VARARGS.locations().contains(AccessFlag.Location.METHOD),
              "VARARGS at METHOD");
        check(AccessFlag.ANNOTATION.locations().containsAll(
                java.util.Arrays.asList(AccessFlag.Location.CLASS, AccessFlag.Location.INNER_CLASS)),
                "ANNOTATION at CLASS+INNER_CLASS");
        check(AccessFlag.ENUM.locations().contains(AccessFlag.Location.FIELD),
                "ENUM also valid at FIELD");
        // versioned lookup from the Location side: flags() vs flags(RELEASE_8)
        check(AccessFlag.Location.METHOD.flags(ClassFileFormatVersion.RELEASE_8)
              .contains(AccessFlag.NATIVE), "versioned flags lookup");
        check(AccessFlag.Location.METHOD.flags(ClassFileFormatVersion.RELEASE_8)
              .contains(AccessFlag.STRICT), "STRICT in RELEASE_8 METHOD flags");
        check(AccessFlag.Location.METHOD.flags().contains(AccessFlag.STRICT) == false,
              "STRICT absent from latest METHOD flags");
        check(AccessFlag.Location.METHOD.flags(ClassFileFormatVersion.RELEASE_8).size()
              > AccessFlag.Location.METHOD.flags().size(), "RELEASE_8 METHOD set larger (has STRICT)");
        check(AccessFlag.Location.METHOD_PARAMETER.flags()
              .equals(EnumSet.of(AccessFlag.FINAL, AccessFlag.SYNTHETIC, AccessFlag.MANDATED)),
              "METHOD_PARAMETER flag set exact");
        check(AccessFlag.Location.INNER_CLASS.flags().contains(AccessFlag.PRIVATE),
              "INNER_CLASS allows PRIVATE");
        check(AccessFlag.Location.INNER_CLASS.flags().contains(AccessFlag.SUPER) == false,
              "SUPER not an INNER_CLASS flag");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
