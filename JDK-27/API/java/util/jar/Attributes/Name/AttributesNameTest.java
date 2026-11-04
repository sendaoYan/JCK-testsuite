/*
 * @test
 * @summary java.util.jar.Attributes.Name conformance: white-box structure + black-box behavior.
 * @run main AttributesNameTest
 */

import java.util.jar.Attributes.Name;

public class AttributesNameTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(Class<? extends Throwable> type, Runnable action) {
        try {
            action.run();
            check(false, "expected " + type.getSimpleName());
        } catch (Throwable t) {
            check(type.isInstance(t),
                  "expected " + type.getSimpleName() + ", got " + t.getClass().getSimpleName());
        }
    }

    static boolean hasField(Class<?> c, String name) {
        try { c.getField(name); return true; } catch (NoSuchFieldException e) { return false; }
    }

    static boolean hasConstructor(Class<?> c, Class<?>... params) {
        try { c.getConstructor(params); return true; } catch (NoSuchMethodException e) { return false; }
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... params) {
        try { c.getMethod(name, params); return true; } catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.jar.Attributes$Name");
        check(!c.isInterface() && java.lang.reflect.Modifier.isPublic(c.getModifiers())
              && java.lang.reflect.Modifier.isStatic(c.getModifiers()),
              "Name is public static nested class");
        check(hasConstructor(c, String.class), "Name(String)");
        check(hasMethod(c, "equals", Object.class), "equals(Object)");
        check(hasMethod(c, "hashCode"), "hashCode()");
        check(hasMethod(c, "toString"), "toString()");

        // every exported constant is a public static final Name
        String[] consts = {
            "MANIFEST_VERSION", "SIGNATURE_VERSION", "CONTENT_TYPE", "CLASS_PATH",
            "MAIN_CLASS", "SEALED", "EXTENSION_LIST", "EXTENSION_NAME",
            "EXTENSION_INSTALLATION", "IMPLEMENTATION_TITLE", "IMPLEMENTATION_VERSION",
            "IMPLEMENTATION_VENDOR", "IMPLEMENTATION_VENDOR_ID", "IMPLEMENTATION_URL",
            "SPECIFICATION_TITLE", "SPECIFICATION_VERSION", "SPECIFICATION_VENDOR",
            "MULTI_RELEASE"
        };
        for (String f : consts) {
            check(hasField(c, f), "constant " + f);
            java.lang.reflect.Field fd = c.getField(f);
            check(java.lang.reflect.Modifier.isPublic(fd.getModifiers())
                  && java.lang.reflect.Modifier.isStatic(fd.getModifiers())
                  && java.lang.reflect.Modifier.isFinal(fd.getModifiers())
                  && Name.class.isAssignableFrom(fd.getType()),
                  f + " is public static final Name");
        }
    }

    // ---- Part 2: black-box (behavioral) ----

    static void testConstants() {
        check("Manifest-Version".equals(Name.MANIFEST_VERSION.toString()), "MANIFEST_VERSION");
        check("Signature-Version".equals(Name.SIGNATURE_VERSION.toString()), "SIGNATURE_VERSION");
        check("Content-Type".equals(Name.CONTENT_TYPE.toString()), "CONTENT_TYPE");
        check("Class-Path".equals(Name.CLASS_PATH.toString()), "CLASS_PATH");
        check("Main-Class".equals(Name.MAIN_CLASS.toString()), "MAIN_CLASS");
        check("Sealed".equals(Name.SEALED.toString()), "SEALED");
        check("Extension-List".equals(Name.EXTENSION_LIST.toString()), "EXTENSION_LIST");
        check("Extension-Name".equals(Name.EXTENSION_NAME.toString()), "EXTENSION_NAME");
        check("Extension-Installation".equals(Name.EXTENSION_INSTALLATION.toString()),
              "EXTENSION_INSTALLATION");
        check("Implementation-Title".equals(Name.IMPLEMENTATION_TITLE.toString()),
              "IMPLEMENTATION_TITLE");
        check("Implementation-Version".equals(Name.IMPLEMENTATION_VERSION.toString()),
              "IMPLEMENTATION_VERSION");
        check("Implementation-Vendor".equals(Name.IMPLEMENTATION_VENDOR.toString()),
              "IMPLEMENTATION_VENDOR");
        check("Implementation-Vendor-Id".equals(Name.IMPLEMENTATION_VENDOR_ID.toString()),
              "IMPLEMENTATION_VENDOR_ID");
        check("Implementation-URL".equals(Name.IMPLEMENTATION_URL.toString()),
              "IMPLEMENTATION_URL");
        check("Specification-Title".equals(Name.SPECIFICATION_TITLE.toString()),
              "SPECIFICATION_TITLE");
        check("Specification-Version".equals(Name.SPECIFICATION_VERSION.toString()),
              "SPECIFICATION_VERSION");
        check("Specification-Vendor".equals(Name.SPECIFICATION_VENDOR.toString()),
              "SPECIFICATION_VENDOR");
        check("Multi-Release".equals(Name.MULTI_RELEASE.toString()), "MULTI_RELEASE");
    }

    static void testEquality() {
        Name custom = new Name("Custom-Header");
        check("Custom-Header".equals(custom.toString()), "custom toString");
        check(custom.equals(new Name("custom-header")), "equals case-insensitive");
        check(custom.equals(new Name("CUSTOM-HEADER")), "equals case-insensitive upper");
        check(custom.hashCode() == new Name("custom-header").hashCode(),
              "hashCode case-insensitive");
        check(custom.hashCode() == new Name("CUSTOM-HEADER").hashCode(),
              "hashCode case-insensitive upper");
        check(!custom.equals(null), "equals null is false");
        check(!custom.equals("Custom-Header"), "equals non-Name is false");

        // known constant equals a fresh Name with the same (any-case) text
        check(Name.MANIFEST_VERSION.equals(new Name("manifest-version")),
              "constant equals case-insensitive fresh Name");
        check(Name.MAIN_CLASS.hashCode() == new Name("MAIN-CLASS").hashCode(),
              "constant hashCode case-insensitive");
    }

    static void testInvalidAndBoundary() {
        expectThrows(NullPointerException.class, new Runnable() {
            public void run() { new Name(null); }
        });
        expectThrows(IllegalArgumentException.class, new Runnable() {
            public void run() { new Name(""); }
        });
        expectThrows(IllegalArgumentException.class, new Runnable() {
            public void run() { new Name("bad name"); }
        });
        expectThrows(IllegalArgumentException.class, new Runnable() {
            public void run() { new Name("bad*name"); }
        });

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 70; i++) sb.append('a');
        String seventy = sb.toString();
        check(new Name(seventy).toString().equals(seventy), "70-char name accepted");

        sb.append('a');
        final String seventyOne = sb.toString();
        expectThrows(IllegalArgumentException.class, new Runnable() {
            public void run() { new Name(seventyOne); }
        });
    }

    static void testBlackBox() {
        testConstants();
        testEquality();
        testInvalidAndBoundary();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
