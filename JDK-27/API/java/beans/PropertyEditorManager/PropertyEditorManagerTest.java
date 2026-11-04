/*
 * @test
 * @summary java.beans.PropertyEditorManager conformance: static-only surface, default
 *          editor matrix (8 number/boolean wrapper types + their primitives, Character
 *          and void absent, enum fallback), fresh-instance-per-find, register/override/
 *          clear lifecycle incl. primitive registration, JVM-global search path
 *          semantics (default value, defensive clone, null -> empty, empty path hides
 *          search-path defaults but not registered primitives), findEditor(null) NPE
 *          matrix. Global registry state is restored after every probing test.
 * @run main PropertyEditorManagerTest
 */

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.beans.PropertyEditorSupport;
import java.util.Arrays;

public class PropertyEditorManagerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface ThrowingRunnable { void run() throws Throwable; }
    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String m) {
        try { r.run(); }
        catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(m + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(m + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }
    static final String[] DEFAULT_PATH = {"sun.beans.editors"};

    // unique-to-this-file types: the registry is JVM-global, never re-register JDK types
    public static class StringEditor extends PropertyEditorSupport {}
    public static class Custom {}
    public static class TypeA {}
    public static class Ed1 extends PropertyEditorSupport {}
    public static class Ed2 extends PropertyEditorSupport {}
    public enum Fruit { APPLE, PEAR }

    static void resetDefaultPath() {
        PropertyEditorManager.setEditorSearchPath(DEFAULT_PATH.clone());
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.PropertyEditorManager");
        check(!c.isInterface(), "is a class");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public class");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (java.lang.reflect.Modifier.isPublic(m.getModifiers())) names.add(m.getName());
        }
        check(names.size() == 4, "exactly 4 public methods, got " + names.size() + ": " + names);
        for (String n : new String[]{"registerEditor", "findEditor", "getEditorSearchPath", "setEditorSearchPath"}) {
            check(names.contains(n), "declares public " + n);
        }
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (java.lang.reflect.Modifier.isPublic(m.getModifiers())) {
                check(java.lang.reflect.Modifier.isStatic(m.getModifiers()), m.getName() + " is static");
            }
        }
    }

    static void testDefaultEditorMatrix() throws Exception {
        check(Arrays.equals(DEFAULT_PATH, PropertyEditorManager.getEditorSearchPath()),
                "default search path is sun.beans.editors: "
                        + Arrays.toString(PropertyEditorManager.getEditorSearchPath()));
        Class<?>[] withDefault = {Integer.class, String.class, Boolean.class, Long.class,
                Double.class, Float.class, Byte.class, Short.class};
        for (final Class<?> t : withDefault) {
            check(PropertyEditorManager.findEditor(t) != null, "default editor for " + t.getName());
        }
        check(PropertyEditorManager.findEditor(Character.class) == null, "no Character editor");
        check(PropertyEditorManager.findEditor(void.class) == null, "no void editor");
        check(PropertyEditorManager.findEditor(Object.class) == null, "no Object editor");
        check(PropertyEditorManager.findEditor(Custom.class) == null, "no default editor for custom type");
        check(PropertyEditorManager.findEditor(int.class) != null, "primitive int has registered editor");
        check(PropertyEditorManager.findEditor(boolean.class) != null, "primitive boolean has registered editor");
        PropertyEditor pe = PropertyEditorManager.findEditor(int.class);
        pe.setValue(Integer.valueOf(42));
        check("42".equals(pe.getAsText()), "primitive editor fully functional");
        PropertyEditor be = PropertyEditorManager.findEditor(boolean.class);
        be.setValue(Boolean.TRUE);
        check("True".equals(be.getAsText()), "primitive boolean editor functional");
        check(PropertyEditorManager.findEditor(Integer.class) != PropertyEditorManager.findEditor(int.class),
                "wrapper and primitive lookups yield independent instances");
    }

    static void testLegacyRegistrationAndSearchPath() throws Exception {
        // legacy checks kept from earlier revisions of this test
        PropertyEditorManager.registerEditor(String.class, StringEditor.class);
        PropertyEditor e = PropertyEditorManager.findEditor(String.class);
        check(e != null && e instanceof StringEditor, "findEditor");
        check(PropertyEditorManager.getEditorSearchPath() != null, "search path");
        check(PropertyEditorManager.findEditor(String.class) != null, "String has default editor");
        PropertyEditorManager.registerEditor(Custom.class, java.beans.PropertyEditorSupport.class);
        check(PropertyEditorManager.findEditor(Custom.class) instanceof java.beans.PropertyEditorSupport,
                "registered editor found");
        PropertyEditorManager.registerEditor(Custom.class, null);
        check(PropertyEditorManager.findEditor(Custom.class) == null, "unregistered");
        String[] path = PropertyEditorManager.getEditorSearchPath();
        check(path != null, "searchPath non-null");
        String[] custom = new String[]{"pkg1", "pkg2"};
        PropertyEditorManager.setEditorSearchPath(custom);
        check(java.util.Arrays.equals(custom, PropertyEditorManager.getEditorSearchPath()),
                "setEditorSearchPath");
        // end legacy; restore JVM-global state
        PropertyEditorManager.registerEditor(String.class, null);
        PropertyEditorManager.registerEditor(Custom.class, null);
        resetDefaultPath();
        PropertyEditor def = PropertyEditorManager.findEditor(String.class);
        check(def != null && !(def instanceof StringEditor),
                "clearing registration exposes the search-path default again");
    }

    static void testFreshInstancePerFind() throws Exception {
        PropertyEditor a = PropertyEditorManager.findEditor(Integer.class);
        PropertyEditor b = PropertyEditorManager.findEditor(Integer.class);
        check(a != null && b != null, "both lookups succeed");
        check(a != b, "each find returns a new editor instance");
        check(a.getClass() == b.getClass(), "same editor class");
    }

    static void testRegisterLifecycle() throws Exception {
        check(PropertyEditorManager.findEditor(TypeA.class) == null, "unregistered custom type is null");
        PropertyEditorManager.registerEditor(TypeA.class, Ed1.class);
        PropertyEditor e1 = PropertyEditorManager.findEditor(TypeA.class);
        check(e1 instanceof Ed1, "registered editor found");
        check(e1 != PropertyEditorManager.findEditor(TypeA.class), "registered class instantiated fresh per find");
        PropertyEditorManager.registerEditor(TypeA.class, Ed2.class);
        check(PropertyEditorManager.findEditor(TypeA.class) instanceof Ed2, "re-registration overrides");
        PropertyEditorManager.registerEditor(TypeA.class, null);
        check(PropertyEditorManager.findEditor(TypeA.class) == null, "null editor clears registration");
        PropertyEditorManager.registerEditor(TypeA.class, Ed1.class);
        check(PropertyEditorManager.findEditor(TypeA.class) instanceof Ed1, "re-register after clear");
        PropertyEditorManager.registerEditor(TypeA.class, null);
        check(PropertyEditorManager.findEditor(TypeA.class) == null, "final cleanup leaves type unregistered");
    }

    static void testPrimitiveRegistration() throws Exception {
        check(PropertyEditorManager.findEditor(char.class) == null, "char primitive has no default editor");
        PropertyEditorManager.registerEditor(char.class, Ed1.class);
        check(PropertyEditorManager.findEditor(char.class) instanceof Ed1, "primitive type registrable");
        PropertyEditorManager.registerEditor(char.class, null);
        check(PropertyEditorManager.findEditor(char.class) == null, "primitive registration cleared");
    }

    static void testSearchPathSemantics() throws Exception {
        resetDefaultPath();
        String[] got = PropertyEditorManager.getEditorSearchPath();
        String[] got2 = PropertyEditorManager.getEditorSearchPath();
        check(got != got2, "getEditorSearchPath returns a fresh clone");
        got[0] = "mutated";
        check(Arrays.equals(DEFAULT_PATH, PropertyEditorManager.getEditorSearchPath()),
                "mutating the returned array has no effect");
        String[] set = {"my.pkg"};
        PropertyEditorManager.setEditorSearchPath(set);
        check(Arrays.equals(set, PropertyEditorManager.getEditorSearchPath()), "set/get round-trip");
        set[0] = "changed";
        check("my.pkg".equals(PropertyEditorManager.getEditorSearchPath()[0]),
                "setEditorSearchPath clones its input");
        PropertyEditorManager.setEditorSearchPath(null);
        check(PropertyEditorManager.getEditorSearchPath() != null, "path after null set is non-null array");
        check(PropertyEditorManager.getEditorSearchPath().length == 0, "null set clears to empty path");
        resetDefaultPath();
        check(Arrays.equals(DEFAULT_PATH, PropertyEditorManager.getEditorSearchPath()), "path restored");
    }

    static void testEmptyPathHidesSearchPathDefaults() throws Exception {
        resetDefaultPath();
        PropertyEditorManager.setEditorSearchPath(new String[0]);
        try {
            check(PropertyEditorManager.findEditor(String.class) == null,
                    "empty path: search-path default editors are gone");
            check(PropertyEditorManager.findEditor(Short.class) == null,
                    "empty path: wrapper editors gone too");
            check(PropertyEditorManager.findEditor(int.class) != null,
                    "empty path: primitive registry entries survive");
            PropertyEditorManager.registerEditor(TypeA.class, Ed1.class);
            check(PropertyEditorManager.findEditor(TypeA.class) instanceof Ed1,
                    "empty path: explicit registrations still work");
            PropertyEditorManager.registerEditor(TypeA.class, null);
        } finally {
            resetDefaultPath();
        }
        check(PropertyEditorManager.findEditor(String.class) != null,
                "restoring path brings default editors back");
    }

    static void testEnumFallback() throws Exception {
        PropertyEditor e = PropertyEditorManager.findEditor(Fruit.class);
        check(e != null, "enum type gets an editor without registration");
        String[] tags = e.getTags();
        check(tags != null && tags.length == 2 && "APPLE".equals(tags[0]) && "PEAR".equals(tags[1]),
                "enum editor tags are the constant names");
        e.setAsText("PEAR");
        check(Fruit.PEAR == e.getValue(), "enum editor round-trip");
    }

    static void testNullHandling() throws Exception {
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() throws Throwable { PropertyEditorManager.findEditor(null); }
        }, "findEditor(null)");
        try {
            PropertyEditorManager.registerEditor(null, Ed1.class);
            PropertyEditor found = PropertyEditorManager.findEditor(null);
            check(found instanceof Ed1, "null-type registration is stored (recorded impl behavior)");
        } finally {
            PropertyEditorManager.registerEditor(null, null);
        }
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() throws Throwable { PropertyEditorManager.findEditor(null); }
        }, "findEditor(null) after clearing the null-type registration");
        resetDefaultPath();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testDefaultEditorMatrix();
        testLegacyRegistrationAndSearchPath();
        testFreshInstancePerFind();
        testRegisterLifecycle();
        testPrimitiveRegistration();
        testSearchPathSemantics();
        testEmptyPathHidesSearchPathDefaults();
        testEnumFallback();
        testNullHandling();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
