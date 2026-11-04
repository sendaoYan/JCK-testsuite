/*
 * @test
 * @summary java.lang.reflect.AccessFlag conformance: white-box structure + black-box behavior.
 *          Deepened: full mask/locations matrix, shared-bit resolution, Modifier cross-check,
 *          accessFlags() end-to-end on real reflected members.
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

    // ---- Part 3: contract deepening (2026-11-05); every literal probed on JDK 27.0.1 ----
    public interface Ifc { }
    public enum Color { RED }
    public @interface Meta { }
    public record Pair(int a, int b) { }
    static class Constants { public static final int ANSWER = 42; }
    interface Sup<T> { T get(); }
    static class GenImpl implements Sup<String> { public String get() { return "g"; } }
    static class VarargsHost { public int total(int... xs) { return xs.length; } }

    static void testFlagCatalog() {
        // exact mask of all 23 constants (shared bit positions per the JVMS flag tables)
        check(AccessFlag.PUBLIC.mask() == 0x0001, "PUBLIC 0x1");
        check(AccessFlag.PRIVATE.mask() == 0x0002, "PRIVATE 0x2");
        check(AccessFlag.PROTECTED.mask() == 0x0004, "PROTECTED 0x4");
        check(AccessFlag.STATIC.mask() == 0x0008, "STATIC 0x8");
        check(AccessFlag.FINAL.mask() == 0x0010, "FINAL 0x10");
        check(AccessFlag.SUPER.mask() == 0x0020 && AccessFlag.OPEN.mask() == 0x0020
              && AccessFlag.TRANSITIVE.mask() == 0x0020 && AccessFlag.SYNCHRONIZED.mask() == 0x0020,
              "0x20 shared by SUPER/OPEN/TRANSITIVE/SYNCHRONIZED");
        check(AccessFlag.STATIC_PHASE.mask() == 0x0040 && AccessFlag.VOLATILE.mask() == 0x0040
              && AccessFlag.BRIDGE.mask() == 0x0040, "0x40 shared by STATIC_PHASE/VOLATILE/BRIDGE");
        check(AccessFlag.TRANSIENT.mask() == 0x0080 && AccessFlag.VARARGS.mask() == 0x0080,
              "0x80 shared by TRANSIENT/VARARGS");
        check(AccessFlag.NATIVE.mask() == 0x0100, "NATIVE 0x100");
        check(AccessFlag.INTERFACE.mask() == 0x0200, "INTERFACE 0x200");
        check(AccessFlag.ABSTRACT.mask() == 0x0400, "ABSTRACT 0x400");
        check(AccessFlag.STRICT.mask() == 0x0800, "STRICT 0x800");
        check(AccessFlag.SYNTHETIC.mask() == 0x1000, "SYNTHETIC 0x1000");
        check(AccessFlag.ANNOTATION.mask() == 0x2000, "ANNOTATION 0x2000");
        check(AccessFlag.ENUM.mask() == 0x4000, "ENUM 0x4000");
        check(AccessFlag.MANDATED.mask() == 0x8000 && AccessFlag.MODULE.mask() == 0x8000,
              "0x8000 shared by MANDATED/MODULE");
        // toString is the constant name itself (NOT lower-cased)
        for (AccessFlag f : AccessFlag.values())
            check(f.toString().equals(f.name()), "toString == name for " + f);
        // contrast: Modifier renders the very same bits as lower-case source modifiers
        check(Modifier.toString(AccessFlag.PUBLIC.mask()).equals("public"), "Modifier renders 0x1 as public");
        check(Modifier.toString(AccessFlag.STRICT.mask()).equals("strictfp"), "Modifier renders 0x800 as strictfp");
        check(Modifier.toString(AccessFlag.VOLATILE.mask()).equals("volatile"), "Modifier renders 0x40 as volatile");
        check(Modifier.toString(AccessFlag.TRANSIENT.mask()).equals("transient"), "Modifier renders 0x80 as transient");
    }

    static void testLocationsMatrix() {
        // exact locations() set for every constant at the latest class-file version
        check(AccessFlag.PUBLIC.locations().equals(EnumSet.of(AccessFlag.Location.CLASS,
              AccessFlag.Location.FIELD, AccessFlag.Location.METHOD, AccessFlag.Location.INNER_CLASS)),
              "PUBLIC locations exact");
        check(AccessFlag.PRIVATE.locations().equals(EnumSet.of(AccessFlag.Location.FIELD,
              AccessFlag.Location.METHOD, AccessFlag.Location.INNER_CLASS)), "PRIVATE locations exact");
        check(AccessFlag.PROTECTED.locations().equals(AccessFlag.PRIVATE.locations()), "PROTECTED mirrors PRIVATE");
        check(AccessFlag.STATIC.locations().equals(AccessFlag.PRIVATE.locations()), "STATIC mirrors PRIVATE");
        check(AccessFlag.FINAL.locations().equals(EnumSet.of(AccessFlag.Location.CLASS,
              AccessFlag.Location.FIELD, AccessFlag.Location.METHOD, AccessFlag.Location.INNER_CLASS,
              AccessFlag.Location.METHOD_PARAMETER)), "FINAL locations exact (all 5 member locations)");
        check(AccessFlag.SUPER.locations().equals(EnumSet.of(AccessFlag.Location.CLASS)), "SUPER exact");
        check(AccessFlag.OPEN.locations().equals(EnumSet.of(AccessFlag.Location.MODULE)), "OPEN exact");
        check(AccessFlag.TRANSITIVE.locations().equals(EnumSet.of(AccessFlag.Location.MODULE_REQUIRES)),
              "TRANSITIVE exact");
        check(AccessFlag.SYNCHRONIZED.locations().equals(EnumSet.of(AccessFlag.Location.METHOD)),
              "SYNCHRONIZED exact");
        check(AccessFlag.STATIC_PHASE.locations().equals(EnumSet.of(AccessFlag.Location.MODULE_REQUIRES)),
              "STATIC_PHASE exact");
        check(AccessFlag.VOLATILE.locations().equals(EnumSet.of(AccessFlag.Location.FIELD)), "VOLATILE exact");
        check(AccessFlag.BRIDGE.locations().equals(EnumSet.of(AccessFlag.Location.METHOD)), "BRIDGE exact");
        check(AccessFlag.TRANSIENT.locations().equals(EnumSet.of(AccessFlag.Location.FIELD)), "TRANSIENT exact");
        check(AccessFlag.VARARGS.locations().equals(EnumSet.of(AccessFlag.Location.METHOD)), "VARARGS exact");
        check(AccessFlag.NATIVE.locations().equals(EnumSet.of(AccessFlag.Location.METHOD)), "NATIVE exact");
        check(AccessFlag.INTERFACE.locations().equals(EnumSet.of(AccessFlag.Location.CLASS,
              AccessFlag.Location.INNER_CLASS)), "INTERFACE exact");
        check(AccessFlag.ABSTRACT.locations().equals(EnumSet.of(AccessFlag.Location.CLASS,
              AccessFlag.Location.METHOD, AccessFlag.Location.INNER_CLASS)), "ABSTRACT exact");
        check(AccessFlag.STRICT.locations().isEmpty(), "STRICT empty at latest");
        check(AccessFlag.SYNTHETIC.locations().equals(EnumSet.allOf(AccessFlag.Location.class)),
              "SYNTHETIC legal at all 9 locations");
        check(AccessFlag.ANNOTATION.locations().equals(AccessFlag.INTERFACE.locations()),
              "ANNOTATION mirrors INTERFACE");
        check(AccessFlag.ENUM.locations().equals(EnumSet.of(AccessFlag.Location.CLASS,
              AccessFlag.Location.FIELD, AccessFlag.Location.INNER_CLASS)), "ENUM exact");
        check(AccessFlag.MANDATED.locations().equals(EnumSet.of(AccessFlag.Location.METHOD_PARAMETER,
              AccessFlag.Location.MODULE, AccessFlag.Location.MODULE_REQUIRES,
              AccessFlag.Location.MODULE_EXPORTS, AccessFlag.Location.MODULE_OPENS)), "MANDATED exact (5)");
        check(AccessFlag.MODULE.locations().equals(EnumSet.of(AccessFlag.Location.CLASS)), "MODULE exact");
        for (AccessFlag f : AccessFlag.values())
            if (f != AccessFlag.STRICT)
                check(!f.locations().isEmpty(), "locations non-empty " + f);
    }

    static void testVersionedLocations() {
        // the METHOD_PARAMETER location does not exist before RELEASE_8
        check(!AccessFlag.FINAL.locations(ClassFileFormatVersion.RELEASE_5)
              .contains(AccessFlag.Location.METHOD_PARAMETER), "FINAL has no METHOD_PARAMETER at RELEASE_5");
        check(AccessFlag.FINAL.locations(ClassFileFormatVersion.RELEASE_8)
              .contains(AccessFlag.Location.METHOD_PARAMETER), "FINAL has METHOD_PARAMETER at RELEASE_8");
        // module-related flags only exist once the module system does (RELEASE_9+)
        AccessFlag[] moduleFlags = {AccessFlag.OPEN, AccessFlag.TRANSITIVE,
              AccessFlag.STATIC_PHASE, AccessFlag.MODULE};
        for (AccessFlag f : moduleFlags) {
            check(f.locations(ClassFileFormatVersion.RELEASE_5).isEmpty(), f + " empty at RELEASE_5");
            check(f.locations(ClassFileFormatVersion.RELEASE_8).isEmpty(), f + " empty at RELEASE_8");
        }
        check(AccessFlag.MANDATED.locations(ClassFileFormatVersion.RELEASE_5).isEmpty(),
              "MANDATED empty at RELEASE_5");
        check(AccessFlag.MANDATED.locations(ClassFileFormatVersion.RELEASE_8)
              .equals(EnumSet.of(AccessFlag.Location.METHOD_PARAMETER)),
              "MANDATED only METHOD_PARAMETER at RELEASE_8");
        check(AccessFlag.SYNTHETIC.locations(ClassFileFormatVersion.RELEASE_8)
              .equals(EnumSet.of(AccessFlag.Location.CLASS, AccessFlag.Location.FIELD,
              AccessFlag.Location.METHOD, AccessFlag.Location.INNER_CLASS,
              AccessFlag.Location.METHOD_PARAMETER)),
              "SYNTHETIC at RELEASE_8: 5 class-file locations, none module-level");
        check(AccessFlag.STRICT.locations(ClassFileFormatVersion.RELEASE_8)
              .equals(EnumSet.of(AccessFlag.Location.METHOD)), "STRICT only METHOD at RELEASE_8");
        // RELEASE_0 (45.0) predates the InnerClasses attribute: no INNER_CLASS location yet
        check(AccessFlag.PUBLIC.locations(ClassFileFormatVersion.RELEASE_0)
              .equals(EnumSet.of(AccessFlag.Location.CLASS, AccessFlag.Location.FIELD,
              AccessFlag.Location.METHOD)), "PUBLIC at RELEASE_0: no INNER_CLASS");
        check(AccessFlag.PUBLIC.locations(ClassFileFormatVersion.RELEASE_1)
              .equals(AccessFlag.PUBLIC.locations()), "PUBLIC stable since RELEASE_1");
        // METHOD_PARAMETER arrives with RELEASE_8: FINAL loses it, SYNTHETIC loses it too
        check(!AccessFlag.SYNTHETIC.locations(ClassFileFormatVersion.RELEASE_7)
              .contains(AccessFlag.Location.METHOD_PARAMETER),
              "SYNTHETIC has no METHOD_PARAMETER at RELEASE_7");
        check(!AccessFlag.FINAL.locations(ClassFileFormatVersion.RELEASE_7)
              .contains(AccessFlag.Location.METHOD_PARAMETER),
              "FINAL has no METHOD_PARAMETER at RELEASE_7");
    }

    static void expectUnmatched(int mask, AccessFlag.Location loc) {
        try {
            AccessFlag.maskToAccessFlags(mask, loc);
            check(false, "mask 0x" + Integer.toHexString(mask) + " must be unmatched at " + loc);
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("Unmatched bit"),
                  "unmatched-bit IAE at " + loc + ": " + e.getMessage());
        }
    }

    static void testSharedBitResolution() {
        // one bit position resolves to different flags depending on the location
        check(AccessFlag.maskToAccessFlags(0x20, AccessFlag.Location.CLASS)
              .equals(EnumSet.of(AccessFlag.SUPER)), "0x20 at CLASS -> SUPER");
        check(AccessFlag.maskToAccessFlags(0x20, AccessFlag.Location.MODULE)
              .equals(EnumSet.of(AccessFlag.OPEN)), "0x20 at MODULE -> OPEN");
        check(AccessFlag.maskToAccessFlags(0x20, AccessFlag.Location.MODULE_REQUIRES)
              .equals(EnumSet.of(AccessFlag.TRANSITIVE)), "0x20 at MODULE_REQUIRES -> TRANSITIVE");
        check(AccessFlag.maskToAccessFlags(0x40, AccessFlag.Location.MODULE_REQUIRES)
              .equals(EnumSet.of(AccessFlag.STATIC_PHASE)), "0x40 at MODULE_REQUIRES -> STATIC_PHASE");
        check(AccessFlag.maskToAccessFlags(0x8000, AccessFlag.Location.CLASS)
              .equals(EnumSet.of(AccessFlag.MODULE)), "0x8000 at CLASS -> MODULE");
        AccessFlag.Location[] mandatedSpots = {AccessFlag.Location.MODULE,
              AccessFlag.Location.METHOD_PARAMETER, AccessFlag.Location.MODULE_REQUIRES,
              AccessFlag.Location.MODULE_EXPORTS, AccessFlag.Location.MODULE_OPENS};
        for (AccessFlag.Location loc : mandatedSpots)
            check(AccessFlag.maskToAccessFlags(0x8000, loc).equals(EnumSet.of(AccessFlag.MANDATED)),
                  "0x8000 at " + loc + " -> MANDATED");
        // combinations decode to the union
        check(AccessFlag.maskToAccessFlags(0x9020, AccessFlag.Location.MODULE)
              .equals(EnumSet.of(AccessFlag.OPEN, AccessFlag.SYNTHETIC, AccessFlag.MANDATED)),
              "module_info 0x9020 decodes OPEN|SYNTHETIC|MANDATED");
        // bits meaningless at the location are rejected
        expectUnmatched(0x8, AccessFlag.Location.CLASS);        // STATIC never a top-level CLASS flag
        expectUnmatched(0x20, AccessFlag.Location.FIELD);       // no 0x20 flag at FIELD
        expectUnmatched(0x40, AccessFlag.Location.INNER_CLASS); // no 0x40 flag at INNER_CLASS
        expectUnmatched(0x4000, AccessFlag.Location.METHOD);    // ENUM not a method flag
        expectUnmatched(0x1, AccessFlag.Location.MODULE);       // PUBLIC not a module flag
        expectUnmatched(0x8000, AccessFlag.Location.METHOD);    // MANDATED not for methods
        expectUnmatched(0x8000, AccessFlag.Location.FIELD);
        expectUnmatched(0x10000, AccessFlag.Location.CLASS);    // above the defined bit range
        expectUnmatched(-1, AccessFlag.Location.CLASS);
    }

    static void testModifierCrossCheck() {
        // Modifier source-modifier masks vs AccessFlag location masks (literals probed)
        check(Modifier.fieldModifiers() == 0x000000df, "fieldModifiers 0xdf");
        check(Modifier.methodModifiers() == 0x00000d3f, "methodModifiers 0xd3f (strictfp still a source modifier)");
        check(Modifier.classModifiers() == 0x00000c1f, "classModifiers 0xc1f");
        check(Modifier.interfaceModifiers() == 0x00000c0f, "interfaceModifiers 0xc0f (no FINAL)");
        check(Modifier.constructorModifiers() == 0x00000007, "constructorModifiers 0x7");
        check((Modifier.fieldModifiers() & ~AccessFlag.Location.FIELD.flagsMask()) == 0,
              "fieldModifiers fully decodable at FIELD (0x50df)");
        check((Modifier.methodModifiers() & ~AccessFlag.Location.METHOD.flagsMask())
              == AccessFlag.STRICT.mask(),
              "methodModifiers exceeds latest METHOD mask exactly by STRICT (0x800)");
        check((Modifier.methodModifiers() & ~AccessFlag.Location.METHOD.flagsMask(
              ClassFileFormatVersion.RELEASE_8)) == 0,
              "methodModifiers fully decodable at METHOD of RELEASE_8 (0x1dff)");
        check((Modifier.classModifiers() & ~AccessFlag.Location.CLASS.flagsMask())
              == (Modifier.STATIC | Modifier.PRIVATE | Modifier.PROTECTED | Modifier.STRICT),
              "classModifiers exceeds CLASS mask by nested-class source modifiers only");
        check((Modifier.interfaceModifiers() & ~AccessFlag.Location.INNER_CLASS.flagsMask())
              == Modifier.STRICT, "interfaceModifiers exceeds INNER_CLASS mask exactly by STRICT");
    }

    static void testAccessFlagsEndToEnd() throws Exception {
        // Class.accessFlags() on real classes
        check(AccessFlagTest.class.accessFlags().equals(EnumSet.of(AccessFlag.PUBLIC, AccessFlag.SUPER)),
              "top-level class PUBLIC+SUPER");
        check(Ifc.class.accessFlags().equals(EnumSet.of(AccessFlag.ABSTRACT, AccessFlag.INTERFACE,
              AccessFlag.PUBLIC, AccessFlag.STATIC)), "nested interface flags");
        check(Color.class.accessFlags().equals(EnumSet.of(AccessFlag.ENUM, AccessFlag.FINAL,
              AccessFlag.PUBLIC, AccessFlag.STATIC)), "nested enum flags");
        check(Meta.class.accessFlags().equals(EnumSet.of(AccessFlag.ABSTRACT, AccessFlag.ANNOTATION,
              AccessFlag.INTERFACE, AccessFlag.PUBLIC, AccessFlag.STATIC)), "nested @interface flags");
        check(Pair.class.accessFlags().equals(EnumSet.of(AccessFlag.FINAL, AccessFlag.PUBLIC,
              AccessFlag.STATIC)), "nested record flags (no dedicated RECORD flag exists)");
        check(String.class.accessFlags().equals(EnumSet.of(AccessFlag.FINAL, AccessFlag.PUBLIC,
              AccessFlag.SUPER)), "java.lang.String flags");
        check(java.util.List.class.accessFlags().equals(EnumSet.of(AccessFlag.ABSTRACT,
              AccessFlag.INTERFACE, AccessFlag.PUBLIC)), "java.util.List flags");
        check(Enum.class.accessFlags().equals(EnumSet.of(AccessFlag.ABSTRACT, AccessFlag.PUBLIC,
              AccessFlag.SUPER)), "java.lang.Enum flags");
        // Field.accessFlags()
        check(Constants.class.getDeclaredField("ANSWER").accessFlags().equals(
              EnumSet.of(AccessFlag.PUBLIC, AccessFlag.STATIC, AccessFlag.FINAL)),
              "public static final field flags exact");
        check(Color.class.getDeclaredField("RED").accessFlags().equals(
              EnumSet.of(AccessFlag.PUBLIC, AccessFlag.STATIC, AccessFlag.FINAL, AccessFlag.ENUM)),
              "enum-constant field carries ENUM");
        // Method.accessFlags(): varargs and compiler-generated bridge
        check(VarargsHost.class.getMethod("total", int[].class).accessFlags().equals(
              EnumSet.of(AccessFlag.PUBLIC, AccessFlag.VARARGS)), "varargs method flags");
        Method bridge = null;
        for (Method m : GenImpl.class.getDeclaredMethods()) if (m.isBridge()) bridge = m;
        check(bridge != null && bridge.accessFlags().equals(
              EnumSet.of(AccessFlag.PUBLIC, AccessFlag.BRIDGE, AccessFlag.SYNTHETIC)),
              "generic-override bridge flags BRIDGE|SYNTHETIC");
        // record accessor: implicit accessor is public
        check(Pair.class.getRecordComponents()[0].getAccessor().accessFlags()
              .equals(EnumSet.of(AccessFlag.PUBLIC)), "record accessor flags");
        // Constructor.accessFlags(): canonical ctor of a public record is public
        check(Pair.class.getDeclaredConstructor(int.class, int.class).accessFlags()
              .equals(EnumSet.of(AccessFlag.PUBLIC)), "public canonical ctor flags");
        // getModifiers() is source-view: the nested-class STATIC bit is NOT a top-level
        // CLASS flag, and maskToAccessFlags refuses it at CLASS but accepts it at INNER_CLASS
        try {
            AccessFlag.maskToAccessFlags(Ifc.class.getModifiers(), AccessFlag.Location.CLASS);
            check(false, "nested-class STATIC bit must be rejected at CLASS");
        } catch (IllegalArgumentException e) {
            check(e.getMessage().contains("Unmatched bit"), "source-view vs CLASS-location mismatch");
        }
        check(AccessFlag.maskToAccessFlags(Ifc.class.getModifiers(),
              AccessFlag.Location.INNER_CLASS).equals(EnumSet.of(AccessFlag.ABSTRACT,
              AccessFlag.INTERFACE, AccessFlag.PUBLIC, AccessFlag.STATIC)),
              "same mask decodes fully at INNER_CLASS");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testFlagCatalog();
        testLocationsMatrix();
        testVersionedLocations();
        testSharedBitResolution();
        testModifierCrossCheck();
        testAccessFlagsEndToEnd();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
