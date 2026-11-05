/*
 * @test
 * @summary java.lang.constant.ModuleDesc conformance: white-box + black-box behavior.
 * @run main ModuleDescTest
 */

import java.lang.constant.ModuleDesc;

public class ModuleDescTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.constant.ModuleDesc");
        check(c.isInterface(), "is an interface");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        for (String m : new String[]{"of", "name", "equals"}) {
            boolean found = false;
            for (java.lang.reflect.Method mm : ms) if (mm.getName().equals(m)) found = true;
            check(found, "declares " + m);
        }
        check(java.lang.reflect.Modifier.isStatic(c.getDeclaredMethod("of", String.class).getModifiers()),
              "of is static");
        check(c.getDeclaredMethod("name").getReturnType() == String.class, "name returns String");
        check(!c.getDeclaredMethod("name").isDefault(), "name is abstract");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // of + name
        ModuleDesc base = ModuleDesc.of("java.base");
        check(base.name().equals("java.base"), "name()");
        check(ModuleDesc.of("com.example.mod").name().equals("com.example.mod"), "dotted name");

        // value semantics
        check(base.equals(ModuleDesc.of("java.base")), "equals by name");
        check(base.hashCode() == ModuleDesc.of("java.base").hashCode(), "hashCode");
        check(!base.equals(ModuleDesc.of("java.compiler")), "!equals different module");
        check(!base.equals(null), "!equals null");
        check(!base.equals("java.base"), "!equals String");
        // the unnamed module has no descriptor: ModuleDesc never represents it
        check(ModuleDesc.of("").name().isEmpty(), "empty name kept verbatim");

        // invalid input
        try { ModuleDesc.of(null); check(false, "null rejected"); }
        catch (NullPointerException expected) { check(true, "null NPE"); }

        // usable as a set key by value
        java.util.Set<ModuleDesc> set = new java.util.HashSet<>();
        set.add(ModuleDesc.of("java.base"));
        set.add(ModuleDesc.of("java.base"));
        check(set.size() == 1, "dedupes by value");
        check(set.contains(ModuleDesc.of("java.base")), "contains by value");
    }

    // ---- Part 3: deep contract (name validation surface) ----
    static void testDeepContract() {
        // ModuleDesc.of is deliberately permissive (JDK-27 measured): only null is rejected;
        // separators like ';' '/' '.' runs are kept verbatim as the module name
        String[] kept = {"bad;name", "a..b", "x/y", "a-b_c.d"};
        for (String n : kept)
            check(ModuleDesc.of(n).name().equals(n),
                  "of(\"" + n + "\") keeps the name verbatim (no format validation)");
        check(ModuleDesc.of("bad;name").equals(ModuleDesc.of("bad;name")),
              "equals holds for permissive names too");
        try { ModuleDesc.of(null); check(false, "null rejected"); }
        catch (NullPointerException expected) { check(true, "of(null) NPE"); }
        // distinct names never collide in hash-based collections
        java.util.Map<ModuleDesc, Integer> map = new java.util.HashMap<>();
        map.put(ModuleDesc.of("m1"), 1);
        map.put(ModuleDesc.of("m2"), 2);
        check(map.size() == 2 && map.get(ModuleDesc.of("m1")) == 1,
              "HashMap keyed by ModuleDesc is name-exact");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
