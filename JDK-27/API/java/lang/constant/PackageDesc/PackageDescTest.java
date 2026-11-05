/*
 * @test
 * @summary java.lang.constant.PackageDesc conformance: white-box + black-box behavior.
 * @run main PackageDescTest
 */

import java.lang.constant.PackageDesc;

public class PackageDescTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.constant.PackageDesc");
        check(c.isInterface(), "is an interface");
        for (String m : new String[]{"of", "ofInternalName", "internalName", "name", "equals"}) {
            boolean found = false;
            for (java.lang.reflect.Method mm : c.getDeclaredMethods())
                if (mm.getName().equals(m)) found = true;
            check(found, "declares " + m);
        }
        check(java.lang.reflect.Modifier.isStatic(c.getDeclaredMethod("of", String.class).getModifiers()),
              "of is static");
        check(c.getDeclaredMethod("name").isDefault(), "name is derived (default method)");
        check(c.getDeclaredMethod("internalName").getReturnType() == String.class,
              "internalName returns String");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // of: binary (dotted) name; internalName: slashed form
        PackageDesc lang = PackageDesc.of("java.lang");
        check(lang.name().equals("java.lang"), "name()");
        check(lang.internalName().equals("java/lang"), "internalName()");

        // ofInternalName goes the other way
        PackageDesc util = PackageDesc.ofInternalName("java/util");
        check(util.name().equals("java.util"), "ofInternalName -> name");
        check(util.internalName().equals("java/util"), "ofInternalName round trip");
        check(PackageDesc.ofInternalName("java/lang").equals(lang),
              "of and ofInternalName agree");

        // default (root) package
        PackageDesc root = PackageDesc.ofInternalName("");
        check(root.name().isEmpty() && root.internalName().isEmpty(), "root package");

        // value semantics
        check(lang.equals(PackageDesc.of("java.lang")), "equals by name");
        check(lang.hashCode() == PackageDesc.of("java.lang").hashCode(), "hashCode");
        check(!lang.equals(PackageDesc.of("java.util")), "!equals other package");
        check(!lang.equals(null) && !lang.equals("java.lang"), "!equals unrelated");
        java.util.Set<PackageDesc> set = new java.util.HashSet<>();
        set.add(PackageDesc.of("java.lang"));
        set.add(PackageDesc.ofInternalName("java/lang"));
        check(set.size() == 1, "dedupes across factory forms");

        // invalid names
        try { PackageDesc.of("java/lang"); check(false, "slash name rejected"); }
        catch (IllegalArgumentException expected) { check(true, "of(slash) IAE"); }
        try { PackageDesc.of("java;lang"); check(false, "semicolon rejected"); }
        catch (IllegalArgumentException expected) { check(true, "of(semicolon) IAE"); }
        try { PackageDesc.of(null); check(false, "null rejected"); }
        catch (NullPointerException expected) { check(true, "of(null) NPE"); }
        try { PackageDesc.ofInternalName(null); check(false, "internal null rejected"); }
        catch (NullPointerException expected) { check(true, "ofInternalName(null) NPE"); }
    }

    // ---- Part 3: deep contract (dotted-name validation surface) ----
    static void testDeepContract() {
        // of("") denotes the root package directly in dotted form too
        check(PackageDesc.of("").name().isEmpty()
                      && PackageDesc.of("").internalName().isEmpty(),
              "of(\"\") is the root package");
        check(PackageDesc.of("").equals(PackageDesc.ofInternalName("")),
              "root package equals across factory forms");

        // dotted form rejects empty identifiers and leading/trailing dots
        String[] badDotted = {"java..lang", ".a", "a.", "a.b;.c"};
        for (String n : badDotted) {
            try {
                PackageDesc.of(n);
                check(false, "of(" + n + ") must be rejected");
            } catch (IllegalArgumentException expected) {
                check(true, "of(" + n + ") -> IAE");
            }
        }
        // internal form rejects empty path components
        try {
            PackageDesc.ofInternalName("a//b");
            check(false, "ofInternalName(a//b) must be rejected");
        } catch (IllegalArgumentException expected) {
            check(true, "ofInternalName(a//b) -> IAE"); }

        // name()/internalName() stay mutually consistent
        PackageDesc deep = PackageDesc.of("a.b.c.d");
        check(deep.internalName().equals("a/b/c/d"), "dotted -> slashed");
        check(PackageDesc.ofInternalName(deep.internalName()).equals(deep),
              "factory round-trip equals");
        // single-segment packages
        PackageDesc single = PackageDesc.of("top");
        check(single.internalName().equals("top"), "single segment unchanged in internal form");
        // validation is separator-shaped only: non-identifier characters inside a
        // segment are kept verbatim (JDK-27 measured)
        check(PackageDesc.of("a.b-c").name().equals("a.b-c"),
              "of(\"a.b-c\") kept verbatim (only dot/semicolon placement is validated)");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
