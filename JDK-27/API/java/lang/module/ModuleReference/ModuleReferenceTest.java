/*
 * @test
 * @summary java.lang.module.ModuleReference conformance: white-box + black-box behavior.
 * @run main ModuleReferenceTest
 */

import java.io.InputStream;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReader;
import java.lang.module.ModuleReference;
import java.net.URI;
import java.util.Optional;
import java.util.stream.Stream;

public class ModuleReferenceTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // user-supplied ModuleReference: exercises the protected constructor and final accessors
    static final class CustomRef extends ModuleReference {
        CustomRef(ModuleDescriptor desc, URI location) { super(desc, location); }
        @Override public ModuleReader open() {
            return new ModuleReader() {
                @Override public Optional<URI> find(String name) { return Optional.empty(); }
                @Override public Stream<String> list() { return Stream.empty(); }
                @Override public void close() { }
            };
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.module.ModuleReference");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public class");
        int protCtors = 0;
        for (java.lang.reflect.Constructor<?> ct : c.getDeclaredConstructors()) {
            if (java.lang.reflect.Modifier.isProtected(ct.getModifiers())) protCtors++;
        }
        check(protCtors >= 1, "protected constructor for subclasses");
        check(java.lang.reflect.Modifier.isFinal(
                      c.getDeclaredMethod("descriptor").getModifiers()), "descriptor() is final");
        check(java.lang.reflect.Modifier.isFinal(
                      c.getDeclaredMethod("location").getModifiers()), "location() is final");
        check(java.lang.reflect.Modifier.isAbstract(
                      c.getDeclaredMethod("open").getModifiers()), "open() is abstract");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // system module reference
        ModuleReference ref = ModuleFinder.ofSystem().find("java.base").orElseThrow();
        check("java.base".equals(ref.descriptor().name()), "descriptor().name() == java.base");
        check(ref.location().isPresent(), "location() present");
        check("jrt".equals(ref.location().get().getScheme()), "location() jrt URI");
        try (ModuleReader reader = ref.open()) {
            check(reader.find("java/lang/Object.class").isPresent(), "open() usable reader");
        }

        // null location is legal: Optional.empty()
        ModuleDescriptor desc = ModuleDescriptor.newModule("custom.m")
                .exports("custom.m.api").build();
        ModuleReference noLoc = new CustomRef(desc, null);
        check(noLoc.location().equals(Optional.empty()), "null location -> empty Optional");
        check(noLoc.descriptor() == desc, "descriptor() identity round trip");
        ModuleReader r = noLoc.open();
        check(r != null, "custom open() returns our reader");
        check(r.find("anything").isEmpty(), "custom reader behavior honored");
        r.close();

        // with location: round trips
        URI loc = URI.create("file:/tmp/somemod.jar");
        ModuleReference withLoc = new CustomRef(desc, loc);
        check(withLoc.location().isPresent(), "location present");
        check(withLoc.location().get().equals(loc), "location round trip");
        check(withLoc.descriptor().exports().iterator().next().source().equals("custom.m.api"),
              "descriptor content survives");

        // descriptor()/location() are stable across calls
        check(withLoc.descriptor() == withLoc.descriptor(), "descriptor() stable");
        check(withLoc.location().equals(withLoc.location()), "location() stable by value");

        // default toString from Object: class@hash form
        check(withLoc.toString().contains("@"), "Object.toString default form");
    }

    // ---- Part 3: contract deepening (2026-11) ----

    // system reference: exact location/toString, reader multiplicity, IOException surface
    static void testSystemReferenceContract() throws Exception {
        ModuleReference ref = ModuleFinder.ofSystem().find("java.base").orElseThrow();
        check("jrt:/java.base".equals(ref.location().orElseThrow().toString()),
              "exact jrt location string");
        check("[module java.base, location=jrt:/java.base]".equals(ref.toString()),
              "system reference toString form");
        ModuleReader r1 = ref.open();
        ModuleReader r2 = ref.open();
        check(r1 != r2, "open() hands out a fresh reader each call");
        try (r1) {
            check(r1.find("java/lang/Object.class").isPresent(), "first reader usable");
        }
        try (r2) {
            check(r2.find("java/lang/Object.class").isPresent(), "second reader unaffected");
        }
        check(ref.descriptor().requires().isEmpty(), "java.base descriptor requires nothing");
        check(ref.descriptor().packages().contains("java.lang"), "descriptor packages java.lang");
        check(ref.descriptor().name().equals(ref.descriptor().name()), "descriptor stable");
        java.lang.reflect.Method open = ModuleReference.class.getDeclaredMethod("open");
        check(open.getExceptionTypes().length == 1
              && open.getExceptionTypes()[0] == java.io.IOException.class,
              "open() declares IOException");
        ModuleReference logging = ModuleFinder.ofSystem().find("java.logging").orElseThrow();
        check("jrt:/java.logging".equals(logging.location().orElseThrow().toString()),
              "java.logging exact jrt location");
        try (ModuleReader reader = logging.open()) {
            check(reader.find("java/util/logging/Logger.class").isPresent(),
                  "java.logging reader sees Logger.class");
            check(reader.find("java/lang/Object.class").isEmpty(),
                  "java.logging reader does not see java.base resources");
        }
        // reference reachable from the boot configuration agrees with the system finder
        ModuleReference resolved = ModuleLayer.boot().configuration()
                .findModule("java.base").orElseThrow().reference();
        check(resolved.descriptor().equals(ref.descriptor()),
              "boot configuration reference descriptor matches system finder");
        check(resolved.location().orElseThrow().equals(ref.location().orElseThrow()),
              "boot configuration reference location matches system finder");
        check(ref.descriptor().version().isPresent(), "java.base descriptor carries a version");
        check(ref.descriptor().toNameAndVersion().startsWith("java.base@"),
              "toNameAndVersion via reference descriptor");
        try (ModuleReader reader = ref.open()) {
            check(reader.read("module-info.class").isPresent(),
                  "module-info readable through the reference");
        }
    }

    // custom references: Object identity semantics, null vs present location
    static void testCustomReferenceContract() throws Exception {
        ModuleDescriptor desc = ModuleDescriptor.newModule("jck.ref.m").exports("jck.ref.p").build();
        URI loc = URI.create("file:/tmp/jck-ref-mod.jar");
        ModuleReference a = new CustomRef(desc, loc);
        ModuleReference b = new CustomRef(desc, loc);
        check(!a.equals(b), "ModuleReference uses identity equality");
        check(a.equals(a), "identity equality reflexive");
        check(a.hashCode() == System.identityHashCode(a), "hashCode is identity-based");
        check(a.toString().contains("@"), "custom toString falls back to Object form");
        check(b.toString().startsWith(getClassSimpleName(b)), "toString prefixes the class name");
        check(a.location().orElseThrow().equals(loc), "location round trip");
        check(a.location().equals(b.location()), "equal locations by value");
        check(a.descriptor() == b.descriptor(), "same descriptor instance shared");
        ModuleReference noLoc = new CustomRef(desc, null);
        check(noLoc.location().isEmpty(), "null location as empty Optional");
        check(!a.location().equals(noLoc.location()), "present vs empty location differ");
        check(noLoc.descriptor() == desc, "descriptor independent of location");
        // open() dispatches to the subclass implementation every time
        ModuleReader r1 = a.open();
        ModuleReader r2 = a.open();
        check(r1 != r2, "subclass open called per invocation");
        check(r1.find("anything").isEmpty() && r2.find("anything").isEmpty(),
              "both readers honor the custom behavior");
        check(a.location().equals(a.location()), "location() stable across calls");
        // descriptor/location final accessors: cannot be overridden by subclasses
        check(java.lang.reflect.Modifier.isFinal(
                  ModuleReference.class.getDeclaredMethod("descriptor").getModifiers()),
              "descriptor() final");
        check(java.lang.reflect.Modifier.isFinal(
                  ModuleReference.class.getDeclaredMethod("location").getModifiers()),
              "location() final");
        // module-info round trip through a custom reference's descriptor
        check(a.descriptor().exports().iterator().next().source().equals("jck.ref.p"),
              "descriptor content reachable through reference");
        check(new CustomRef(desc, null).descriptor().name().equals("jck.ref.m"),
              "null-location reference still exposes the descriptor");
    }

    static String getClassSimpleName(Object o) {
        return o.getClass().getName();
    }

    // ---- Part 4: white-box deepening ----
    static void testWhiteBoxDeepen() throws Exception {
        Class<?> c = ModuleReference.class;
        boolean seenProtectedCtor = false;
        for (java.lang.reflect.Constructor<?> ct : c.getDeclaredConstructors()) {
            if (java.lang.reflect.Modifier.isProtected(ct.getModifiers())
                    && ct.getParameterCount() == 2
                    && ct.getParameterTypes()[0] == ModuleDescriptor.class
                    && ct.getParameterTypes()[1] == java.net.URI.class) {
                seenProtectedCtor = true;
            }
        }
        check(seenProtectedCtor, "protected (ModuleDescriptor, URI) constructor");
        check(java.lang.reflect.Modifier.isAbstract(
                  c.getDeclaredMethod("open").getModifiers()), "open remains abstract");
        check(java.io.Serializable.class.isAssignableFrom(c) == false,
              "ModuleReference is not Serializable");
        boolean overridesToString = false;
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (m.getName().equals("toString")) overridesToString = true;
        }
        check(!overridesToString, "abstract class does not override toString");
        check(c.getPackageName().equals("java.lang.module"), "package java.lang.module");
        check(!java.lang.reflect.Modifier.isFinal(c.getModifiers()), "abstract class is not final");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testWhiteBoxDeepen();
        testBlackBox();
        testSystemReferenceContract();
        testCustomReferenceContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
