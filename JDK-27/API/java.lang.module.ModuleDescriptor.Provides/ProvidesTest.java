/*
 * @test
 * @summary java.lang.module.ModuleDescriptor.Provides conformance: white-box + black-box behavior.
 * @run main ProvidesTest
 */

import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleDescriptor.Provides;
import java.util.List;

public class ProvidesTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static Provides build() {
        return ModuleDescriptor.newModule("m")
                .provides("svc.Service", List.of("impl.A", "impl.B"))
                .build().provides().iterator().next();
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.module.ModuleDescriptor$Provides");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "Provides is final");
        check(Comparable.class.isAssignableFrom(c), "implements Comparable");
        int pubCtors = 0;
        for (java.lang.reflect.Constructor<?> ct : c.getDeclaredConstructors()) {
            if (java.lang.reflect.Modifier.isPublic(ct.getModifiers())) pubCtors++;
        }
        check(pubCtors == 0, "no public constructor");
        check(c.getDeclaredMethod("service").getReturnType() == String.class, "service() String");
        check(c.getDeclaredMethod("providers").getReturnType() == List.class, "providers() List");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        Provides p = build();
        check("svc.Service".equals(p.service()), "service()");
        check(p.providers().equals(List.of("impl.A", "impl.B")), "providers() in declared order");
        check(p.providers().size() == 2, "two providers");

        // value semantics
        Provides p2 = build();
        check(p.equals(p2), "equals()");
        check(p.hashCode() == p2.hashCode(), "hashCode()");
        check(p.compareTo(p2) == 0, "compareTo() equal");
        check(!p.equals(new Object()), "!equals Object");

        Provides other = ModuleDescriptor.newModule("n")
                .provides("other.Service", List.of("impl.C"))
                .build().provides().iterator().next();
        check(!p.equals(other), "!equals different service");
        check(p.compareTo(other) > 0, "compareTo orders by service name");
        check(other.compareTo(p) < 0, "compareTo reverse");

        // different providers list -> not equal (order matters, List semantics)
        Provides reversed = ModuleDescriptor.newModule("m2")
                .provides("svc.Service", List.of("impl.B", "impl.A"))
                .build().provides().iterator().next();
        check(!p.equals(reversed), "provider order participates in equals");

        Provides fewer = ModuleDescriptor.newModule("m3")
                .provides("svc.Service", List.of("impl.A"))
                .build().provides().iterator().next();
        check(!p.equals(fewer), "provider count participates in equals");

        // toString shape: "service/provider,provider"
        String s = p.toString();
        check(s.contains("svc.Service") && s.contains("impl.A"), "toString mentions both");

        // real-world: java.base provides well-known services
        ModuleDescriptor base = ModuleLayer.boot()
                .findModule("java.base").orElseThrow().getDescriptor();
        check(base.provides().stream().anyMatch(pr -> pr.service().startsWith("java.")),
              "java.base provides java.* services");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
