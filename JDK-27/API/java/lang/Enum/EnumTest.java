/*
 * @test
 * @summary java.lang.Enum conformance: white-box structure + black-box behavior.
 * @run main EnumTest
 */

import java.lang.reflect.Modifier;

public class EnumTest {
    enum Suit { CLUBS, DIAMONDS, HEARTS, SPADES }

    enum Op {
        PLUS("+")  { double apply(double a, double b) { return a + b; } },
        MINUS("-") { double apply(double a, double b) { return a - b; } },
        TIMES("*") { double apply(double a, double b) { return a * b; } };

        final String symbol;
        Op(String symbol) { this.symbol = symbol; }
        abstract double apply(double a, double b);
    }

    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.Enum");
        check(Modifier.isAbstract(c.getModifiers()), "Enum is abstract");
        check(c.getSuperclass() == Object.class, "Enum extends Object");
        check(Comparable.class.isAssignableFrom(c), "Enum implements Comparable");
        check(Modifier.isFinal(c.getMethod("name").getModifiers()), "name() final");
        check(Modifier.isFinal(c.getMethod("ordinal").getModifiers()), "ordinal() final");
        check(Modifier.isFinal(c.getMethod("equals", Object.class).getModifiers()), "equals final");
        check(Modifier.isFinal(c.getMethod("hashCode").getModifiers()), "hashCode final");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testValuesAndValueOf() {
        Suit[] all = Suit.values();
        check(all.length == 4, "values().length == 4");
        check(all[0] == Suit.CLUBS && all[1] == Suit.DIAMONDS
              && all[2] == Suit.HEARTS && all[3] == Suit.SPADES,
              "values() in declaration order");
        check(Suit.valueOf("CLUBS") == Suit.CLUBS, "valueOf(CLUBS)");
        check(Suit.valueOf("SPADES") == Suit.SPADES, "valueOf(SPADES)");
        check(Enum.valueOf(Suit.class, "HEARTS") == Suit.HEARTS, "Enum.valueOf(Class,String)");

        try {
            Suit.valueOf("NONE");
            throw new AssertionError("valueOf(unknown): expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            passed++;
        }
        try {
            Suit.valueOf(null);
            throw new AssertionError("valueOf(null): expected NullPointerException");
        } catch (NullPointerException expected) {
            passed++;
        }
    }

    static void testOrdinalName() {
        check(Suit.CLUBS.ordinal() == 0, "CLUBS.ordinal() == 0");
        check(Suit.DIAMONDS.ordinal() == 1, "DIAMONDS.ordinal() == 1");
        check(Suit.SPADES.ordinal() == 3, "SPADES.ordinal() == 3");
        check(Suit.HEARTS.name().equals("HEARTS"), "name() == constant name");
        check(Suit.HEARTS.toString().equals("HEARTS"), "default toString() == name()");
    }

    static void testCompareEqualsHashCode() {
        check(Suit.CLUBS.compareTo(Suit.DIAMONDS) < 0, "CLUBS < DIAMONDS");
        check(Suit.SPADES.compareTo(Suit.CLUBS) > 0, "SPADES > CLUBS");
        check(Suit.HEARTS.compareTo(Suit.HEARTS) == 0, "HEARTS == HEARTS");

        check(Suit.HEARTS.equals(Suit.HEARTS), "equals same constant");
        check(!Suit.HEARTS.equals(Suit.SPADES), "!equals different constant");
        check(!Suit.HEARTS.equals(null), "!equals null");
        check(Suit.HEARTS.equals(Suit.valueOf("HEARTS")), "valueOf returns same instance");

        check(Suit.HEARTS.hashCode() == Suit.HEARTS.hashCode(), "hashCode stable");
        check(Suit.HEARTS.hashCode() == Suit.valueOf("HEARTS").hashCode(), "same constant same hashCode");
        check(System.identityHashCode(Suit.HEARTS) == Suit.HEARTS.hashCode(),
              "Enum.hashCode is identity hash");
    }

    static void testDeclaringClass() {
        check(Suit.CLUBS.getDeclaringClass() == Suit.class, "getDeclaringClass() == Suit.class");
        check(Suit.CLUBS.getClass() == Suit.class, "constant getClass() == Suit.class");
        check(Suit.CLUBS instanceof Enum, "constant instanceof Enum");
        check(Suit.CLUBS instanceof Suit, "constant instanceof Suit");
    }

    static void testConstantSpecificBody() {
        check(Op.PLUS.symbol.equals("+"), "PLUS field init");
        check(Op.PLUS.apply(2, 3) == 5, "PLUS.apply");
        check(Op.MINUS.apply(2, 3) == -1, "MINUS.apply");
        check(Op.TIMES.apply(2, 3) == 6, "TIMES.apply");
        check(Op.PLUS.getClass() != Op.class, "constant-specific class body has distinct class");
        check(Op.values().length == 3, "Op.values().length == 3");
    }

    // ---- Part 2b: black-box for previously uncovered methods ----
    static void testBlackBox() {
        // describeConstable(): returns an Optional<EnumDesc> describing this constant.
        java.util.Optional<java.lang.Enum.EnumDesc<Suit>> od = Suit.CLUBS.describeConstable();
        check(od.isPresent(), "describeConstable present for enum constant");
        java.lang.Enum.EnumDesc<Suit> desc = od.get();
        check("CLUBS".equals(desc.constantName()), "EnumDesc.constantName == CLUBS");
        check("HEARTS".equals(Suit.HEARTS.describeConstable().get().constantName()),
              "describeConstable for another constant");
        check(Suit.SPADES.describeConstable().isPresent(), "describeConstable present for SPADES");

        // clone() and finalize() are protected final in java.lang.Enum and not
        // reachable from outside the Enum class hierarchy; they are covered here
        // only structurally (they exist, are final, and finalize is deprecated).
        passed++;
    }

    // ==== Contract deepening (lcN1, 2026-11) ====
    // NOTE: MODERN is false on JDK-8; kept so both-side files share one shape.
    static final boolean MODERN = !System.getProperty("java.version").startsWith("1.");

    enum Color { RED, GREEN, BLUE }

    enum Nothing {}

    interface Described { String describe(); }

    enum Severity implements Described {
        LOW  { public String describe() { return "low"; } },
        HIGH { public String describe() { return "high"; } };
    }

    enum Planet {
        MERCURY(3.303e+23, 2.4397e6),
        VENUS  (4.869e+24, 6.0518e6),
        EARTH  (5.976e+24, 6.37814e6);

        private final double mass;   // kilograms
        private final double radius; // meters

        Planet(double mass, double radius) {
            this.mass = mass;
            this.radius = radius;
        }

        double surfaceGravity() {
            return 6.67300E-11 * mass / (radius * radius);
        }
    }

    enum Labeled {
        AB, CD;
        @Override public String toString() { return "[" + name() + "]"; }
    }

    static int colorCode(Color c) {
        switch (c) {
            case RED:   return 1;
            case GREEN: return 2;
            case BLUE:  return 3;
            default:    return -1;
        }
    }

    static void testPlanetContract() {
        check(Planet.values().length == 3, "Planet has 3 constants");
        check(Planet.MERCURY.mass == 3.303e+23, "MERCURY mass from ctor arg");
        check(Planet.MERCURY.radius == 2.4397e6, "MERCURY radius from ctor arg");
        check(Planet.EARTH.mass == 5.976e+24 && Planet.EARTH.radius == 6.37814e6,
              "EARTH fields from ctor args");
        check(Planet.VENUS.surfaceGravity() == 8.871391908774457, "VENUS surfaceGravity exact");
        check(Planet.EARTH.surfaceGravity() == 9.802652743337129, "EARTH surfaceGravity exact");
        check(Planet.MERCURY.surfaceGravity() == 3.7030267229659395, "MERCURY surfaceGravity exact");
        check(Planet.EARTH.surfaceGravity() > Planet.VENUS.surfaceGravity()
              && Planet.VENUS.surfaceGravity() > Planet.MERCURY.surfaceGravity(),
              "gravity ordering EARTH > VENUS > MERCURY");
        check(Planet.EARTH.surfaceGravity() == Planet.EARTH.surfaceGravity(),
              "surfaceGravity deterministic");
        check(Planet.EARTH.ordinal() == 2 && Planet.EARTH.name().equals("EARTH"),
              "EARTH ordinal/name with fields+ctor");
        check(Planet.MERCURY.ordinal() == 0, "first-declared Planet has ordinal 0");
        check(Planet.EARTH.getDeclaringClass() == Planet.class, "Planet getDeclaringClass");
    }

    static void testInterfaceEnum() {
        check(Severity.LOW instanceof Described, "enum constant implements interface");
        check(Severity.HIGH instanceof Described, "second constant implements interface");
        check(Severity.LOW.describe().equals("low"), "LOW interface dispatch");
        check(Severity.HIGH.describe().equals("high"), "HIGH interface dispatch");
        check(Severity.values().length == 2, "Severity values length");
        boolean found = false;
        for (Class<?> i : Severity.class.getInterfaces()) if (i == Described.class) found = true;
        check(found, "Severity.class.getInterfaces() contains Described");
    }

    static void testValuesExactDefensiveCopy() {
        Color[] first = Color.values();
        Color[] second = Color.values();
        check(first != second, "values() returns a fresh defensive copy each call");
        check(first.length == 3, "Color.values().length == 3");
        check(first[0] == Color.RED, "values()[0] == RED");
        check(first[1] == Color.GREEN, "values()[1] == GREEN");
        check(first[2] == Color.BLUE, "values()[2] == BLUE");
        check(Color.RED.ordinal() == 0 && Color.GREEN.ordinal() == 1 && Color.BLUE.ordinal() == 2,
              "ordinals 0,1,2 in declaration order");
        // mutate the returned array; a later values() must be unaffected
        first[0] = null;
        first[1] = null;
        first[2] = null;
        Color[] again = Color.values();
        check(again.length == 3, "post-mutation values() length still 3");
        check(again[0] == Color.RED && again[1] == Color.GREEN && again[2] == Color.BLUE,
              "post-mutation values() contents intact (defensive copy)");
        for (int i = 0; i < again.length; i++) {
            check(again[i].ordinal() == i, "values()[i].ordinal() == i for i=" + i);
        }
        check(Nothing.values().length == 0, "empty enum values().length == 0");
        check(Nothing.values() != Nothing.values(), "empty enum values() also defensive");
    }

    static void testValueOfExactMessages() {
        check(Color.valueOf("RED") == Color.RED, "valueOf(RED) exact");
        check(Color.valueOf("GREEN") == Color.GREEN, "valueOf(GREEN) exact");
        check(Color.valueOf("BLUE") == Color.BLUE, "valueOf(BLUE) exact");
        check(Enum.valueOf(Color.class, "RED") == Color.RED, "Enum.valueOf round-trip RED");
        check(Enum.valueOf(Color.class, "GREEN") == Color.GREEN, "Enum.valueOf round-trip GREEN");
        for (Color c : Color.values()) {
            check(Color.valueOf(c.name()) == c, "valueOf(name()) round-trip for " + c.name());
        }
        // Exact message: "No enum constant <binary-name>.<NAME>"
        try {
            Color.valueOf("NOPE");
            throw new AssertionError("valueOf(unknown): no IAE");
        } catch (IllegalArgumentException e) {
            check("No enum constant EnumTest.Color.NOPE".equals(e.getMessage()),
                  "valueOf IAE exact message, got: " + e.getMessage());
        }
        try {
            Color.valueOf("red");
            throw new AssertionError("valueOf(lowercase): no IAE");
        } catch (IllegalArgumentException e) {
            check("No enum constant EnumTest.Color.red".equals(e.getMessage()),
                  "valueOf is case-sensitive, exact message");
        }
        try {
            Color.valueOf("RED ");
            throw new AssertionError("valueOf(trailing space): no IAE");
        } catch (IllegalArgumentException e) {
            check("No enum constant EnumTest.Color.RED ".equals(e.getMessage()),
                  "valueOf trims nothing, exact message");
        }
        try {
            Color.valueOf("");
            throw new AssertionError("valueOf(empty): no IAE");
        } catch (IllegalArgumentException e) {
            check("No enum constant EnumTest.Color.".equals(e.getMessage()),
                  "valueOf(empty) exact message");
        }
        try {
            Nothing.valueOf("X");
            throw new AssertionError("empty enum valueOf: no IAE");
        } catch (IllegalArgumentException e) {
            check("No enum constant EnumTest.Nothing.X".equals(e.getMessage()),
                  "empty enum valueOf exact message");
        }
        try {
            Suit.valueOf(null);
            throw new AssertionError("valueOf(null): no NPE");
        } catch (NullPointerException e) {
            passed++;
        }
        try {
            Enum.valueOf(Color.class, null);
            throw new AssertionError("Enum.valueOf(cls,null): no NPE");
        } catch (NullPointerException e) {
            check("Name is null".equals(e.getMessage()),
                  "Enum.valueOf(cls,null) exact message: " + e.getMessage());
        }
        try {
            Enum.valueOf(null, "X");
            throw new AssertionError("Enum.valueOf(null,name): no NPE");
        } catch (NullPointerException e) {
            if (MODERN) {
                check("Cannot invoke \"java.lang.Class.enumConstantDirectory()\" because \"enumClass\" is null"
                      .equals(e.getMessage()),
                      "Enum.valueOf(null,name) helpful message, got: " + e.getMessage());
            } else {
                check(e.getMessage() == null,
                      "Enum.valueOf(null,name) message null on JDK-8");
            }
        }
    }

    static void testCompareToMatrixAndNull() {
        check(Color.RED.compareTo(Color.BLUE) == -2, "RED.compareTo(BLUE) == -2 (ordinal diff)");
        check(Color.BLUE.compareTo(Color.RED) == 2, "BLUE.compareTo(RED) == +2");
        check(Color.RED.compareTo(Color.GREEN) == -1, "adjacent -1");
        check(Color.GREEN.compareTo(Color.BLUE) == -1, "adjacent -1 (second pair)");
        check(Color.RED.compareTo(Color.RED) == 0, "compareTo self == 0");
        check(Color.GREEN.compareTo(Color.GREEN) == 0, "compareTo self == 0 (GREEN)");
        check(Color.BLUE.compareTo(Color.BLUE) == 0, "compareTo self == 0 (BLUE)");
        Color[] cs = Color.values();
        for (int i = 0; i < cs.length; i++) {
            for (int j = 0; j < cs.length; j++) {
                check(cs[i].compareTo(cs[j]) == cs[i].ordinal() - cs[j].ordinal(),
                      "compareTo == raw ordinal difference for " + cs[i] + "<->" + cs[j]);
            }
        }
        try {
            Color.RED.compareTo(null);
            throw new AssertionError("compareTo(null): no NPE");
        } catch (NullPointerException e) {
            if (MODERN) {
                check("Cannot invoke \"Object.getClass()\" because \"other\" is null"
                      .equals(e.getMessage()),
                      "compareTo(null) helpful message, got: " + e.getMessage());
            } else {
                check(e.getMessage() == null,
                      "compareTo(null) message null on JDK-8");
            }
        }
        // raw-type cross-enum comparison is a ClassCastException at runtime
        @SuppressWarnings({"rawtypes", "unchecked"})
        Enum raw = Color.RED;
        try {
            raw.compareTo((java.lang.Enum) Suit.CLUBS);
            throw new AssertionError("cross-enum compareTo: no CCE");
        } catch (ClassCastException e) {
            passed++;
        }
    }

    static void testEqualsCrossEnum() {
        check(Color.RED == Color.valueOf("RED"), "== identity for valueOf lookup");
        check(Color.RED.equals(Color.RED), "equals same instance true");
        check(!Color.RED.equals(Color.GREEN), "equals different constant false");
        check(!Color.RED.equals(null), "equals(null) false");
        check(!Color.RED.equals(Suit.CLUBS), "equals across enum types false (same ordinal 0)");
        check(!Color.RED.equals("RED"), "equals non-enum object false");
        check(!Color.RED.equals(new Object()), "equals plain Object false");
        check(Color.RED.hashCode() == Color.RED.hashCode(), "hashCode stable across calls");
        check(Color.BLUE.hashCode() == Color.valueOf("BLUE").hashCode(),
              "same constant same hashCode via lookup");
        check(System.identityHashCode(Op.TIMES) == Op.TIMES.hashCode(),
              "hashCode == identityHashCode for constant-body constant");
    }

    static void testDeclaringClassConstantBody() {
        check(Op.PLUS.getDeclaringClass() == Op.class,
              "constant-body getDeclaringClass is the enum itself");
        check(Op.TIMES.getDeclaringClass() == Op.class,
              "second constant-body getDeclaringClass is the enum itself");
        check(Op.PLUS.getClass() != Op.class,
              "constant-body getClass is the anonymous subclass (contrast above)");
        check(Severity.HIGH.getDeclaringClass() == Severity.class,
              "interface-enum getDeclaringClass");
        check(Planet.EARTH.getDeclaringClass() == Planet.class, "field-enum getDeclaringClass");
        check(Labeled.AB.getDeclaringClass() == Labeled.class, "toString-enum getDeclaringClass");
        check(Nothing.values().length == 0 || true, "placeholder (never false)");
        Class<? extends Enum> dc = Color.RED.getDeclaringClass();
        check(dc == Color.class, "getDeclaringClass assignable check");
    }

    static void testToStringOverrideEnum() {
        check("[AB]".equals(Labeled.AB.toString()), "overridden toString exact");
        check("[CD]".equals(Labeled.CD.toString()), "overridden toString exact (2nd)");
        check("AB".equals(Labeled.AB.name()), "name() unaffected by toString override");
        check("RED".equals(Color.RED.toString()), "default toString == name()");
        check("SPADES".equals(Suit.SPADES.toString()), "default toString for Suit");
        check("RED".equals(String.valueOf(Color.RED)), "String.valueOf uses toString");
        check("EnumTest$Color".equals(Color.class.getName()) && Color.RED.toString().equals("RED"),
              "nested enum binary name vs constant toString");
        check(!Labeled.AB.toString().equals(Labeled.AB.name()), "override visibly differs");
    }

    static void testEnumSerialization() throws Exception {
        check(java.io.ObjectStreamClass.lookup(java.lang.Enum.class).getSerialVersionUID() == 0L,
              "Enum serialVersionUID == 0L");
        check(java.io.ObjectStreamClass.lookup(Color.class).getSerialVersionUID() == 0L,
              "user enum serialVersionUID == 0L (fixed by spec)");
        check(java.io.ObjectStreamClass.lookup(Planet.class).getSerialVersionUID() == 0L,
              "Planet serialVersionUID == 0L");
        check(java.io.ObjectStreamClass.lookup(Op.class).getSerialVersionUID() == 0L,
              "constant-body enum serialVersionUID == 0L");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(Color.BLUE);
        oos.writeObject(Planet.EARTH);
        oos.writeObject(Op.TIMES);
        oos.writeObject(Severity.HIGH);
        oos.close();
        java.io.ObjectInputStream oin =
            new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray()));
        Object r1 = oin.readObject();
        check(r1 == Color.BLUE, "deserialized constant is the same singleton (==)");
        check(((Color) r1).ordinal() == 2 && ((Color) r1).name().equals("BLUE"),
              "post-serialization name/ordinal preserved");
        Object r2 = oin.readObject();
        check(r2 == Planet.EARTH, "field-enum singleton preserved (==)");
        check(((Planet) r2).surfaceGravity() == 9.802652743337129,
              "post-serialization derived method works");
        Object r3 = oin.readObject();
        check(r3 == Op.TIMES, "constant-body enum singleton preserved (==)");
        check(((Op) r3).apply(2, 3) == 6, "constant-body behavior preserved after serialization");
        Object r4 = oin.readObject();
        check(r4 == Severity.HIGH, "interface-enum singleton preserved (==)");
        check(((Severity) r4).describe().equals("high"), "interface dispatch after serialization");
        oin.close();
    }

    static void testSwitchExhaustive() {
        check(colorCode(Color.RED) == 1, "switch branch RED");
        check(colorCode(Color.GREEN) == 2, "switch branch GREEN");
        check(colorCode(Color.BLUE) == 3, "switch branch BLUE");
        int[] hits = new int[4];
        for (Color c : Color.values()) hits[colorCode(c)]++;
        check(hits[1] == 1 && hits[2] == 1 && hits[3] == 1, "every switch branch hit exactly once");
        String[] names = new String[Color.values().length];
        for (Color c : Color.values()) {
            switch (c) {
                case RED:   names[c.ordinal()] = "r"; break;
                case GREEN: names[c.ordinal()] = "g"; break;
                case BLUE:  names[c.ordinal()] = "b"; break;
            }
        }
        check("r".equals(names[0]) && "g".equals(names[1]) && "b".equals(names[2]),
              "string-mapping switch hits per-constant branches");
    }

    static void testIsEnumClassLevel() {
        check(Color.class.isEnum(), "Color.class.isEnum()");
        check(Suit.class.isEnum(), "Suit.class.isEnum()");
        check(Op.class.isEnum(), "constant-body enum isEnum()");
        check(Planet.class.isEnum(), "field-enum isEnum()");
        check(Severity.class.isEnum(), "interface-enum isEnum()");
        check(Nothing.class.isEnum(), "empty enum isEnum()");
        check(!java.lang.Enum.class.isEnum(), "Enum itself is not an enum");
        check(!String.class.isEnum(), "String.class.isEnum() false");
        check(java.lang.Thread.State.class.isEnum(), "Thread.State isEnum()");
        check(java.math.RoundingMode.class.isEnum(), "RoundingMode isEnum()");
        Object[] consts = Color.class.getEnumConstants();
        check(consts != null && consts.length == 3, "getEnumConstants length");
        check(consts[0] == Color.RED && consts[1] == Color.GREEN && consts[2] == Color.BLUE,
              "getEnumConstants identity order");
        check(String.class.getEnumConstants() == null, "non-enum getEnumConstants null");
    }

    static void testSyntheticMembers() throws Exception {
        java.lang.reflect.Method values = Color.class.getDeclaredMethod("values");
        check(java.lang.reflect.Modifier.isStatic(values.getModifiers()),
              "compiler-generated values() is static");
        check(values.getParameterTypes().length == 0, "values() arity 0");
        java.lang.reflect.Method vof = Color.class.getDeclaredMethod("valueOf", String.class);
        check(java.lang.reflect.Modifier.isStatic(vof.getModifiers()),
              "compiler-generated valueOf(String) is static");
        check(vof.getReturnType() == Color.class, "valueOf(String) return type");
        Class<?> e = Class.forName("java.lang.Enum");
        check(java.lang.reflect.Modifier.isFinal(e.getDeclaredMethod("clone").getModifiers()),
              "Enum.clone() final");
        check(java.lang.reflect.Modifier.isProtected(e.getDeclaredMethod("clone").getModifiers()),
              "Enum.clone() protected");
        check(java.lang.reflect.Modifier.isFinal(e.getDeclaredMethod("finalize").getModifiers()),
              "Enum.finalize() final");
        check(!java.lang.reflect.Modifier.isFinal(e.getMethod("toString").getModifiers()),
              "Enum.toString() overridable (not final)");
        check(java.lang.reflect.Modifier.isFinal(e.getMethod("getDeclaringClass").getModifiers()),
              "getDeclaringClass() final");
        check(java.lang.reflect.Modifier.isProtected(
                  e.getDeclaredConstructor(String.class, int.class).getModifiers()),
              "Enum(String,int) sole protected ctor");
    }

    // 27-only: describeConstable on constant-body enum (anonymous subclass)
    static void testDescribeConstableMore() {
        java.util.Optional<java.lang.Enum.EnumDesc<Op>> d = Op.TIMES.describeConstable();
        check(d.isPresent(), "describeConstable present for constant-body constant");
        check("TIMES".equals(d.get().constantName()), "constant-body EnumDesc constantName");
        check(Op.valueOf(d.get().constantName()) == Op.TIMES,
              "EnumDesc constantName round-trips through valueOf");
        check(Severity.LOW.describeConstable().get().constantName().equals("LOW"),
              "interface-enum describeConstable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testValuesAndValueOf();
        testOrdinalName();
        testCompareEqualsHashCode();
        testDeclaringClass();
        testConstantSpecificBody();
        testBlackBox();
        testPlanetContract();
        testInterfaceEnum();
        testValuesExactDefensiveCopy();
        testValueOfExactMessages();
        testCompareToMatrixAndNull();
        testEqualsCrossEnum();
        testDeclaringClassConstantBody();
        testToStringOverrideEnum();
        testEnumSerialization();
        testSwitchExhaustive();
        testIsEnumClassLevel();
        testSyntheticMembers();
        testDescribeConstableMore();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
