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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
