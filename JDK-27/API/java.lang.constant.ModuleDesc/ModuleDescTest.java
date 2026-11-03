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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
