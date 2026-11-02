/*
 * @test
 * @summary java.lang.module.ModuleDescriptor.Provides conformance: service(), providers(), equality.
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

    public static void main(String[] args) {
        Provides p = build();
        check("svc.Service".equals(p.service()), "service()");
        check(p.providers().equals(List.of("impl.A", "impl.B")), "providers()");

        Provides p2 = build();
        check(p.equals(p2), "equals()");
        check(p.hashCode() == p2.hashCode(), "hashCode()");
        check(p.compareTo(p2) == 0, "compareTo() equal");
        check(p.toString() != null, "toString() non-null");

        Provides other = ModuleDescriptor.newModule("n")
                .provides("other.Service", List.of("impl.C"))
                .build().provides().iterator().next();
        check(!p.equals(other), "!equals different service");
        check(p.compareTo(other) > 0, "compareTo orders by service name");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
