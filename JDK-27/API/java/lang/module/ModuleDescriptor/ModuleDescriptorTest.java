/*
 * @test
 * @summary java.lang.module.ModuleDescriptor conformance: white-box structure + black-box behavior.
 * @run main ModuleDescriptorTest
 */

import java.io.InputStream;
import java.lang.module.InvalidModuleDescriptorException;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReader;
import java.lang.module.ModuleReference;
import java.lang.reflect.Modifier;
import java.util.Set;

public class ModuleDescriptorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.module.ModuleDescriptor");
        check(Modifier.isFinal(c.getModifiers()), "ModuleDescriptor is final");
        check(Comparable.class.isAssignableFrom(c), "implements Comparable");
        int pubCtors = 0;
        for (java.lang.reflect.Constructor<?> ct : c.getDeclaredConstructors()) {
            if (Modifier.isPublic(ct.getModifiers())) pubCtors++;
        }
        check(pubCtors == 0, "no public constructor: builders/read only");
        String[] factories = {"newModule", "newOpenModule", "newAutomaticModule", "read"};
        for (String f : factories) {
            boolean found = false;
            for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
                if (m.getName().equals(f) && Modifier.isStatic(m.getModifiers())) found = true;
            }
            check(found, "static factory " + f + " present");
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // read(InputStream) of a real module's module-info.class
        ModuleReference ref = ModuleFinder.ofSystem().find("java.base").orElseThrow();
        ModuleDescriptor base;
        try (ModuleReader reader = ref.open()) {
            try (InputStream in = reader.open("module-info.class").orElseThrow()) {
                base = ModuleDescriptor.read(in);
            }
        }
        check("java.base".equals(base.name()), "read(InputStream).name() == java.base");
        check(!base.isAutomatic(), "java.base not automatic");
        check(!base.isOpen(), "java.base not open");
        check(base.mainClass().isEmpty(), "java.base has no main class");
        check(base.requires().isEmpty(), "java.base requires nothing (it is the root)");
        check(base.exports().stream().anyMatch(e -> e.source().equals("java.lang")),
              "java.base exports java.lang");
        check(base.packages().contains("java.lang"), "java.base packages contains java.lang");
        check(base.modifiers() != null, "modifiers() non-null set");

        // build a full-featured descriptor and read every getter back
        ModuleDescriptor full = ModuleDescriptor.newModule("com.example.full")
                .requires(Set.of(ModuleDescriptor.Requires.Modifier.TRANSITIVE), "java.base")
                .requires(Set.of(ModuleDescriptor.Requires.Modifier.STATIC), "java.compiler")
                .exports("com.example.full.api", Set.of("com.example.client"))
                .exports("com.example.full.spi")
                .opens("com.example.full.impl", Set.of("com.example.framework"))
                .uses("com.example.full.api.Service")
                .provides("com.example.full.api.Service",
                          java.util.List.of("com.example.full.spi.Impl1", "com.example.full.spi.Impl2"))
                .packages(Set.of("com.example.full.api", "com.example.full.spi"))
                .mainClass("com.example.full.Main")
                .version(ModuleDescriptor.Version.parse("2.1.0"))
                .build();

        check("com.example.full".equals(full.name()), "name");
        check(!full.isOpen(), "not open");
        check(!full.isAutomatic(), "not automatic");
        check(full.requires().size() == 2, "two requires (java.base implicit overridden)");
        ModuleDescriptor.Requires req = full.requires().stream()
                .filter(r -> r.name().equals("java.compiler")).findFirst().orElseThrow();
        check(req.modifiers().equals(Set.of(ModuleDescriptor.Requires.Modifier.STATIC)),
              "STATIC modifier read back");
        check("2.1.0".equals(full.version().orElseThrow().toString()), "version read back");
        check("2.1.0".equals(full.rawVersion().orElseThrow()), "rawVersion read back");
        check("com.example.full@2.1.0".equals(full.toNameAndVersion()), "toNameAndVersion");
        check("com.example.full.Main".equals(full.mainClass().orElseThrow()), "mainClass");
        ModuleDescriptor.Exports qualified = full.exports().stream()
                .filter(e -> e.source().equals("com.example.full.api")).findFirst().orElseThrow();
        check(qualified.isQualified(), "qualified export");
        check(qualified.targets().equals(Set.of("com.example.client")), "export targets");
        ModuleDescriptor.Opens open = full.opens().iterator().next();
        check(open.source().equals("com.example.full.impl"), "opens source");
        check(open.targets().equals(Set.of("com.example.framework")), "opens targets");
        check(full.uses().equals(Set.of("com.example.full.api.Service")), "uses");
        ModuleDescriptor.Provides provides = full.provides().iterator().next();
        check(provides.service().equals("com.example.full.api.Service"), "provides service");
        check(provides.providers().equals(java.util.List.of(
                "com.example.full.spi.Impl1", "com.example.full.spi.Impl2")), "providers order");
        check(full.packages().size() == 4, "packages = declared + opens source + mainClass package");
        check(full.packages().containsAll(Set.of(
                "com.example.full", "com.example.full.api", "com.example.full.spi",
                "com.example.full.impl")),
              "packages content");

        // newModule injects an implicit java.base requirement with MANDATED
        ModuleDescriptor m = ModuleDescriptor.newModule("m").build();
        check(m.requires().size() == 1, "implicit java.base require");
        ModuleDescriptor.Requires implicit = m.requires().iterator().next();
        check("java.base".equals(implicit.name()), "implicit require is java.base");
        check(implicit.modifiers().equals(Set.of(ModuleDescriptor.Requires.Modifier.MANDATED)),
              "implicit require is MANDATED");

        // open/automatic variants
        ModuleDescriptor om = ModuleDescriptor.newOpenModule("om").build();
        check(om.isOpen(), "newOpenModule isOpen");
        check(!om.isAutomatic(), "newOpenModule not automatic");
        ModuleDescriptor am = ModuleDescriptor.newAutomaticModule("am").build();
        check(am.isAutomatic(), "newAutomaticModule isAutomatic");
        check(am.requires().equals(Set.of()) == false
              && am.requires().size() == 1
              && "java.base".equals(am.requires().iterator().next().name())
              && am.requires().iterator().next().modifiers()
                      .equals(Set.of(ModuleDescriptor.Requires.Modifier.MANDATED)),
              "automatic module only has mandated java.base");
        check(am.modifiers().equals(Set.of(ModuleDescriptor.Modifier.AUTOMATIC)),
              "automatic modifier set");
        try {
            ModuleDescriptor.newAutomaticModule("am2").requires("java.logging");
            check(false, "automatic module cannot declare requires");
        } catch (IllegalStateException expected) {
            check(true, "automatic requires throws ISE");
        }

        // version(String) overload equals version(Version)
        ModuleDescriptor vs = ModuleDescriptor.newModule("vs").version("9.9").build();
        check("9.9".equals(vs.version().orElseThrow().toString()), "version(String)");

        // equals/hashCode/compareTo are name-based
        check(m.equals(ModuleDescriptor.newModule("m").build()), "equals same name");
        check(m.hashCode() == ModuleDescriptor.newModule("m").build().hashCode(), "hashCode");
        check(m.compareTo(ModuleDescriptor.newModule("m").build()) == 0, "compareTo equal");
        check(ModuleDescriptor.newModule("a").build()
                .compareTo(ModuleDescriptor.newModule("b").build()) < 0, "compareTo by name");
        check(!m.equals(om), "different names not equal");

        // read(ByteBuffer) variant reads the same descriptor
        ModuleDescriptor fromBuf;
        try (ModuleReader reader = ref.open()) {
            java.nio.ByteBuffer buf = reader.read("module-info.class").orElseThrow();
            fromBuf = ModuleDescriptor.read(buf);
            reader.release(buf);
        }
        check("java.base".equals(fromBuf.name()), "read(ByteBuffer).name()");
        check(fromBuf.equals(base), "both read paths equal");

        // null/blank name rejected (IllegalArgumentException per Checks.requireModuleName)
        try {
            ModuleDescriptor.newModule(null);
            check(false, "null name rejected");
        } catch (IllegalArgumentException expected) {
            check(true, "null name IAE");
        }
        try {
            ModuleDescriptor.newModule("");
            check(false, "empty name rejected");
        } catch (IllegalArgumentException expected) {
            check(true, "empty name IAE");
        }
    }

    // ---- Part 3: contract deepening (2026-11) ----

    // read() of the real java.base module-info.class through Module.getResourceAsStream
    static void testReadRealJavaBase() throws Exception {
        ModuleDescriptor base;
        try (InputStream in = String.class.getModule().getResourceAsStream("module-info.class")) {
            check(in != null, "java.base module-info.class stream non-null");
            base = ModuleDescriptor.read(in);
        }
        check("java.base".equals(base.name()), "read name java.base");
        check(base.requires().isEmpty(), "java.base requires nothing");
        check(base.isOpen() == false && base.isAutomatic() == false, "neither open nor automatic");
        check(base.mainClass().isEmpty(), "no main class");
        check(base.version().isPresent(), "runtime image stamps java.base with a version");
        check(base.rawVersion().isPresent()
              && base.rawVersion().get().equals(base.version().get().toString()),
              "rawVersion string equals version().toString()");
        check(base.toNameAndVersion().startsWith("java.base@"), "toNameAndVersion 'java.base@<v>'");
        check(base.modifiers().isEmpty(), "java.base declares no modifiers");
        ModuleDescriptor.Exports jl = base.exports().stream()
                .filter(e -> e.source().equals("java.lang")).findFirst().orElseThrow();
        check(!jl.isQualified(), "java.lang exported unqualified");
        check(jl.targets().isEmpty(), "unqualified export has no targets");
        check(jl.modifiers().isEmpty(), "java.lang export carries no modifiers");
        check(base.exports().stream().anyMatch(e -> e.source().equals("java.util")),
              "java.util exported");
        check(base.packages().contains("java.lang") && base.packages().contains("java.util")
              && base.packages().contains("java.io"), "packages contains lang/util/io");
        check(base.packages().size() > 150, "java.base packages well over 150");
        check(base.opens().isEmpty(), "java.base opens nothing");
        check(base.uses() != null, "uses() never null");
        ModuleDescriptor again;
        try (InputStream in = String.class.getModule().getResourceAsStream("module-info.class")) {
            again = ModuleDescriptor.read(in, () -> null);
        }
        check(again.equals(base), "read(InputStream,Supplier) equals read(InputStream)");
        check(again.hashCode() == base.hashCode(), "hashCode equal for equal descriptors");
        check(again.compareTo(base) == 0, "compareTo 0 for equal descriptors");
        check(base.equals(String.class.getModule().getDescriptor()),
              "read() result equals Module.getDescriptor()");
    }

    // canonical builder round-trip: every field read back exactly
    static void testBuilderRoundTrip() {
        ModuleDescriptor d = ModuleDescriptor.newModule("t.m")
                .requires(Set.of(ModuleDescriptor.Requires.Modifier.MANDATED), "java.base")
                .exports("pkg")
                .build();
        check("t.m".equals(d.name()), "name");
        check(d.requires().size() == 1, "explicit java.base require replaces the implicit one");
        ModuleDescriptor.Requires r = d.requires().iterator().next();
        check("java.base".equals(r.name()) && r.modifiers().equals(
                Set.of(ModuleDescriptor.Requires.Modifier.MANDATED)), "require content");
        check(d.exports().size() == 1 && d.exports().iterator().next().source().equals("pkg"),
              "export content");
        check(!d.exports().iterator().next().isQualified(), "export unqualified");
        check(d.packages().equals(Set.of("pkg")), "packages exactly [pkg]");
        check(d.opens().isEmpty(), "no opens");
        check(d.uses().isEmpty(), "no uses");
        check(d.provides().isEmpty(), "no provides");
        check(d.mainClass().isEmpty(), "no main class");
        check(d.version().isEmpty() && d.rawVersion().isEmpty(), "no version");
        check(!d.isAutomatic() && !d.isOpen(), "plain explicit module");
        check("t.m".equals(d.toNameAndVersion()), "toNameAndVersion without version is bare name");
        // two identical builds are fully interchangeable value objects
        ModuleDescriptor d2 = ModuleDescriptor.newModule("t.m")
                .requires(Set.of(ModuleDescriptor.Requires.Modifier.MANDATED), "java.base")
                .exports("pkg").build();
        check(d.equals(d2), "equals same content");
        check(d.hashCode() == d2.hashCode(), "hashCode same content");
        check(d.compareTo(d2) == 0, "compareTo 0 same content");
        check(!d.equals(ModuleDescriptor.newModule("t.n").build()), "different name not equal");
        check(d.compareTo(ModuleDescriptor.newModule("t.n").build()) < 0, "compareTo by name");
        check(!d.equals(null) && !d.equals("t.m"), "equals null/foreign type false");
    }

    // Requires: modifier combinations, compiled versions, value semantics
    static void testRequiresContract() {
        ModuleDescriptor d = ModuleDescriptor.newModule("req.m")
                .requires(Set.of(ModuleDescriptor.Requires.Modifier.TRANSITIVE,
                                 ModuleDescriptor.Requires.Modifier.STATIC), "m.two.mod")
                .requires(Set.of(ModuleDescriptor.Requires.Modifier.SYNTHETIC), "m.sy")
                .requires(Set.of(ModuleDescriptor.Requires.Modifier.MANDATED,
                                 ModuleDescriptor.Requires.Modifier.TRANSITIVE), "m.tr")
                .requires(Set.of(), "m.plain")
                .requires(Set.of(), "m.cv", ModuleDescriptor.Version.parse("3.1"))
                .build();
        java.util.Map<String, ModuleDescriptor.Requires> byName = new java.util.HashMap<>();
        for (ModuleDescriptor.Requires rq : d.requires()) byName.put(rq.name(), rq);
        check(byName.size() == 6, "five explicit requires plus implicit java.base");
        // implicit java.base is present too
        check(byName.containsKey("java.base"), "implicit java.base still injected");
        check(byName.get("m.two.mod").modifiers().equals(
                Set.of(ModuleDescriptor.Requires.Modifier.TRANSITIVE,
                       ModuleDescriptor.Requires.Modifier.STATIC)), "TRANSITIVE+STATIC combo");
        String ts = byName.get("m.two.mod").toString();
        check(ts.endsWith(" m.two.mod") && ts.contains("static") && ts.contains("transitive"),
              "toString lists both modifiers in lowercase before the name");
        check("synthetic m.sy".equals(byName.get("m.sy").toString()), "toString synthetic");
        String tr = byName.get("m.tr").toString();
        check(tr.endsWith(" m.tr") && tr.contains("mandated") && tr.contains("transitive"),
              "toString mandated + transitive lowercase");
        check(byName.get("m.plain").modifiers().isEmpty(), "empty modifier set");
        check(byName.get("m.plain").toString().equals("m.plain"), "plain toString bare name");
        ModuleDescriptor.Requires cv = byName.get("m.cv");
        check(cv.compiledVersion().orElseThrow().toString().equals("3.1"), "compiledVersion 3.1");
        check("3.1".equals(cv.rawCompiledVersion().orElseThrow()), "rawCompiledVersion 3.1");
        check(byName.get("m.sy").compiledVersion().isEmpty(), "no compiled version by default");
        try {
            byName.get("m.sy").modifiers().add(ModuleDescriptor.Requires.Modifier.STATIC);
            check(false, "Requires.modifiers() immutable");
        } catch (UnsupportedOperationException e) {
            check(true, "Requires.modifiers() immutable");
        }
        // value semantics across two identical builds
        ModuleDescriptor d2 = ModuleDescriptor.newModule("req.m")
                .requires(Set.of(), "m.cv", ModuleDescriptor.Version.parse("3.1")).build();
        ModuleDescriptor.Requires cv2 = d2.requires().stream()
                .filter(rq -> rq.name().equals("m.cv")).findFirst().orElseThrow();
        check(cv.equals(cv2), "Requires equals same content");
        check(cv.hashCode() == cv2.hashCode(), "Requires hashCode same content");
        check(cv.compareTo(cv2) == 0, "Requires compareTo 0");
        check(!cv.equals(byName.get("m.plain")), "Requires different name not equal");
        check(byName.get("m.plain").compareTo(cv) != 0, "Requires compareTo nonzero across names");
    }

    // Exports/Opens/Provides/Uses value types and their contracts
    static void testExportsOpensProvidesUses() {
        ModuleDescriptor d = ModuleDescriptor.newModule("val.m")
                .exports(Set.of(ModuleDescriptor.Exports.Modifier.SYNTHETIC), "val.api", Set.of("val.client"))
                .exports("val.spi")
                .opens(Set.of(ModuleDescriptor.Opens.Modifier.SYNTHETIC), "val.impl", Set.of("val.fw"))
                .opens("val.open")
                .uses("val.api.S1").uses("val.api.S2")
                .provides("val.api.S1", java.util.List.of("val.spi.A", "val.spi.B"))
                .build();
        ModuleDescriptor.Exports q = d.exports().stream()
                .filter(e -> e.source().equals("val.api")).findFirst().orElseThrow();
        check(q.isQualified(), "qualified export");
        check(q.targets().equals(Set.of("val.client")), "export targets");
        check("synthetic val.api to [val.client]".equals(q.toString()), "export toString form");
        check(q.modifiers().equals(Set.of(ModuleDescriptor.Exports.Modifier.SYNTHETIC)),
              "export SYNTHETIC modifier");
        try {
            q.targets().add("x");
            check(false, "exports targets immutable");
        } catch (UnsupportedOperationException e) {
            check(true, "exports targets immutable");
        }
        ModuleDescriptor.Exports u = d.exports().stream()
                .filter(e -> e.source().equals("val.spi")).findFirst().orElseThrow();
        check(!u.isQualified() && u.targets().isEmpty(), "unqualified export no targets");
        // compareTo orders exports by source
        java.util.List<ModuleDescriptor.Exports> sorted = new java.util.ArrayList<>(d.exports());
        java.util.Collections.sort(sorted);
        check(sorted.get(0).source().equals("val.api") && sorted.get(1).source().equals("val.spi"),
              "Exports compareTo sorts by source");
        ModuleDescriptor.Opens oq = d.opens().stream()
                .filter(op -> op.source().equals("val.impl")).findFirst().orElseThrow();
        check(oq.isQualified() && oq.targets().equals(Set.of("val.fw")), "qualified open");
        check("synthetic val.impl to [val.fw]".equals(oq.toString()), "opens toString form");
        check(d.opens().stream().filter(op -> op.source().equals("val.open"))
                .allMatch(op -> !op.isQualified()), "unqualified open");
        ModuleDescriptor.Provides p = d.provides().iterator().next();
        check("val.api.S1".equals(p.service()), "provides service");
        check(p.providers().equals(java.util.List.of("val.spi.A", "val.spi.B")),
              "providers order preserved");
        check("val.api.S1 with [val.spi.A, val.spi.B]".equals(p.toString()), "provides toString form");
        try {
            p.providers().add("x");
            check(false, "providers list immutable");
        } catch (UnsupportedOperationException e) {
            check(true, "providers list immutable");
        }
        check(d.uses().equals(Set.of("val.api.S1", "val.api.S2")), "uses set");
        // value-object identity across identical builds
        ModuleDescriptor d2 = ModuleDescriptor.newModule("val.m")
                .exports(Set.of(ModuleDescriptor.Exports.Modifier.SYNTHETIC), "val.api", Set.of("val.client"))
                .exports("val.spi")
                .opens(Set.of(ModuleDescriptor.Opens.Modifier.SYNTHETIC), "val.impl", Set.of("val.fw"))
                .opens("val.open")
                .uses("val.api.S1").uses("val.api.S2")
                .provides("val.api.S1", java.util.List.of("val.spi.A", "val.spi.B"))
                .build();
        check(d.equals(d2), "full builder equals twin");
        check(d.hashCode() == d2.hashCode(), "full builder hashCode twin");
        check(d.exports().equals(d2.exports()), "exports sets equal");
        check(d.opens().equals(d2.opens()), "opens sets equal");
        check(d.provides().equals(d2.provides()), "provides sets equal");
        check(d.uses().equals(d2.uses()), "uses sets equal");
    }

    // copy-in overloads accept prebuilt Exports/Opens/Requires/Provides objects
    static void testCopyInOverloads() {
        ModuleDescriptor src = ModuleDescriptor.newModule("copy.src")
                .requires(Set.of(ModuleDescriptor.Requires.Modifier.TRANSITIVE), "java.logging")
                .exports("copy.src.api")
                .opens("copy.src.impl")
                .provides("copy.S", java.util.List.of("copy.Impl"))
                .build();
        ModuleDescriptor.Requires rq = src.requires().stream()
                .filter(r -> r.name().equals("java.logging")).findFirst().orElseThrow();
        ModuleDescriptor.Exports ex = src.exports().iterator().next();
        ModuleDescriptor.Opens op = src.opens().iterator().next();
        ModuleDescriptor.Provides pr = src.provides().iterator().next();
        ModuleDescriptor dst = ModuleDescriptor.newModule("copy.dst")
                .requires(rq).exports(ex).opens(op).provides(pr).build();
        check(dst.requires().contains(rq), "requires(Requires) round trip");
        check(dst.exports().contains(ex), "exports(Exports) round trip");
        check(dst.opens().contains(op), "opens(Opens) round trip");
        check(dst.provides().contains(pr), "provides(Provides) round trip");
        check(dst.exports().iterator().next() == ex, "value objects reused by identity");
    }

    // Version: parse/toString/compareTo/equals plus precise error messages
    static void testVersionContract() {
        ModuleDescriptor.Version v = ModuleDescriptor.Version.parse("11.0.1");
        check("11.0.1".equals(v.toString()), "toString round trip");
        check(v.equals(ModuleDescriptor.Version.parse("11.0.1")), "equals same parse");
        check(v.hashCode() == ModuleDescriptor.Version.parse("11.0.1").hashCode(), "hashCode stable");
        // strict ordering chain: 1.0 < 1.0.1 < 1.1 < 2.0
        ModuleDescriptor.Version v10 = ModuleDescriptor.Version.parse("1.0");
        ModuleDescriptor.Version v101 = ModuleDescriptor.Version.parse("1.0.1");
        ModuleDescriptor.Version v11 = ModuleDescriptor.Version.parse("1.1");
        ModuleDescriptor.Version v20 = ModuleDescriptor.Version.parse("2.0");
        check(v10.compareTo(v101) < 0, "1.0 < 1.0.1");
        check(v101.compareTo(v11) < 0, "1.0.1 < 1.1");
        check(v11.compareTo(v20) < 0, "1.1 < 2.0");
        check(v20.compareTo(v11) > 0, "2.0 > 1.1 (antisymmetric)");
        check(v10.compareTo(ModuleDescriptor.Version.parse("1.0")) == 0, "compareTo 0 same value");
        check(ModuleDescriptor.Version.parse("1.0-ea").compareTo(v10) < 0,
              "pre-release compares less than release");
        check(ModuleDescriptor.Version.parse("1.2.3.4.5").compareTo(v11) > 0,
              "longer number sequence compares greater at first difference");
        check(ModuleDescriptor.Version.parse("10").compareTo(ModuleDescriptor.Version.parse("9")) > 0,
              "numeric (not lexicographic) comparison");
        try {
            ModuleDescriptor.Version.parse("x");
            check(false, "parse(x) IAE");
        } catch (IllegalArgumentException e) {
            check("x: Version string does not start with a number".equals(e.getMessage()),
                  "parse(x) exact message");
        }
        try {
            ModuleDescriptor.Version.parse(null);
            check(false, "parse(null) IAE");
        } catch (IllegalArgumentException e) {
            check("Null version string".equals(e.getMessage()), "parse(null) exact message");
        }
        try {
            ModuleDescriptor.Version.parse("");
            check(false, "parse(empty) IAE");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null, "parse(empty) IAE with message");
        }
        // builder version(Version) vs version(String)
        ModuleDescriptor a = ModuleDescriptor.newModule("ver.a").version(v).build();
        ModuleDescriptor b = ModuleDescriptor.newModule("ver.a").version("11.0.1").build();
        check(a.version().orElseThrow().equals(b.version().orElseThrow()),
              "version(Version) and version(String) agree");
        check("11.0.1".equals(b.rawVersion().orElseThrow()), "rawVersion keeps the literal");
    }

    // all returned collections are immutable
    static void testImmutabilityMatrix() {
        ModuleDescriptor d = ModuleDescriptor.newModule("imm.m")
                .requires("java.logging").exports("imm.api").opens("imm.impl")
                .uses("imm.S").provides("imm.S", java.util.List.of("imm.Impl"))
                .build();
        int n = 0;
        try { d.requires().add(null); } catch (UnsupportedOperationException e) { n++; }
        try { d.exports().add(null); } catch (UnsupportedOperationException e) { n++; }
        try { d.opens().add(null); } catch (UnsupportedOperationException e) { n++; }
        try { d.uses().add(null); } catch (UnsupportedOperationException e) { n++; }
        try { d.provides().add(null); } catch (UnsupportedOperationException e) { n++; }
        try { d.packages().add(null); } catch (UnsupportedOperationException e) { n++; }
        try { d.modifiers().add(null); } catch (UnsupportedOperationException e) { n++; }
        check(n == 7, "all seven collections immutable");
        ModuleDescriptor.Requires rq = d.requires().iterator().next();
        try { rq.modifiers().add(null); check(false, "require modifiers immutable"); }
        catch (UnsupportedOperationException e) { check(true, "require modifiers immutable"); }
        ModuleDescriptor.Exports ex = d.exports().iterator().next();
        try { ex.targets().add(null); check(false, "export targets immutable"); }
        catch (UnsupportedOperationException e) { check(true, "export targets immutable"); }
        ModuleDescriptor.Opens op = d.opens().iterator().next();
        try { op.targets().add(null); check(false, "open targets immutable"); }
        catch (UnsupportedOperationException e) { check(true, "open targets immutable"); }
        ModuleDescriptor.Provides pr = d.provides().iterator().next();
        try { pr.providers().add(null); check(false, "provider list immutable"); }
        catch (UnsupportedOperationException e) { check(true, "provider list immutable"); }
    }

    // negative inputs: exact IllegalArgumentException messages from the name checks
    static void testNegativeInputs() {
        try {
            ModuleDescriptor.newModule("bad..name");
            check(false, "bad..name rejected");
        } catch (IllegalArgumentException e) {
            check("bad..name: Invalid module name: '' is not a Java identifier".equals(e.getMessage()),
                  "bad..name exact message");
        }
        try {
            ModuleDescriptor.newModule("1x");
            check(false, "1x rejected");
        } catch (IllegalArgumentException e) {
            check("1x: Invalid module name: '1x' is not a Java identifier".equals(e.getMessage()),
                  "1x exact message");
        }
        try {
            ModuleDescriptor.newModule("ok").exports("bad..pkg");
            check(false, "bad package rejected");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("Invalid package name"),
                  "bad package message prefix");
        }
        try {
            ModuleDescriptor.newModule("ok").requires("bad..name");
            check(false, "bad require name rejected");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("Invalid module name"),
                  "bad require message prefix");
        }
        try {
            ModuleDescriptor.newModule("ok").opens("bad..pkg");
            check(false, "bad open package rejected");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("Invalid package name"),
                  "bad open message prefix");
        }
        try {
            ModuleDescriptor.newModule("ok").provides(null, java.util.List.of("x"));
            check(false, "null service rejected");
        } catch (IllegalArgumentException e) {
            check(true, "null service IAE");
        }
        try {
            ModuleDescriptor.newModule("ok").provides("Svc", java.util.List.of());
            check(false, "empty providers rejected");
        } catch (IllegalArgumentException e) {
            check(true, "empty providers IAE");
        }
        try {
            ModuleDescriptor.newModule("ok").uses("bad..name");
            check(false, "bad service name rejected");
        } catch (IllegalArgumentException e) {
            check("bad..name: Invalid service type name: '' is not a Java identifier"
                      .equals(e.getMessage()), "bad uses exact message");
        }
        // open and automatic modules cannot declare opens
        try {
            ModuleDescriptor.newOpenModule("om").opens("p.q").build();
            check(false, "open module opens rejected");
        } catch (IllegalStateException e) {
            check("Open or automatic modules cannot declare open packages".equals(e.getMessage()),
                  "open module opens exact message");
        }
        try {
            ModuleDescriptor.newAutomaticModule("am").opens("p.q").build();
            check(false, "automatic module opens rejected");
        } catch (IllegalStateException e) {
            check("Open or automatic modules cannot declare open packages".equals(e.getMessage()),
                  "automatic module opens exact message");
        }
        // building twice is legal: fresh equal descriptors
        ModuleDescriptor.Builder b = ModuleDescriptor.newModule("twice");
        ModuleDescriptor first = b.build();
        ModuleDescriptor second = b.build();
        check(first != second && first.equals(second), "build() twice yields equal twins");
    }

    // broken module-info.class bytes: precise InvalidModuleDescriptorException messages
    static void testReadBrokenModuleInfo() throws Exception {
        byte[] badVersion = new byte[] {
            (byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE,
            0x00, 0x03, 0x00, 0x00, 0x00, 0x00, 0x00, 0x42, 0x01, 0x02, 0x03 };
        try {
            ModuleDescriptor.read(new java.io.ByteArrayInputStream(badVersion));
            check(false, "unsupported class version rejected");
        } catch (InvalidModuleDescriptorException e) {
            check("Unsupported major.minor version 0.3".equals(e.getMessage()),
                  "unsupported class version exact message");
        }
        try {
            ModuleDescriptor.read(new java.io.ByteArrayInputStream(new byte[0]));
            check(false, "empty input rejected");
        } catch (InvalidModuleDescriptorException e) {
            check("Truncated module-info.class".equals(e.getMessage()),
                  "truncated input exact message");
        }
        try {
            ModuleDescriptor.read((InputStream) null);
            check(false, "read(null) NPE");
        } catch (NullPointerException e) {
            check(true, "read(null) NPE");
        }
        byte[] notModuleInfo = new byte[] {
            (byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE,
            0x00, 0x00, 0x00, 0x43 };
        try {
            ModuleDescriptor.read(new java.io.ByteArrayInputStream(notModuleInfo));
            check(false, "truncated constant pool rejected");
        } catch (InvalidModuleDescriptorException e) {
            check(true, "truncated tail still InvalidModuleDescriptorException");
        }
    }

    // modifier enums: exact value sets and enum mechanics
    static void testModifierEnums() {
        check(java.util.List.of(ModuleDescriptor.Requires.Modifier.values()).toString()
              .equals("[TRANSITIVE, STATIC, SYNTHETIC, MANDATED]"), "Requires.Modifier values");
        check(java.util.List.of(ModuleDescriptor.Exports.Modifier.values()).toString()
              .equals("[SYNTHETIC, MANDATED]"), "Exports.Modifier values");
        check(java.util.List.of(ModuleDescriptor.Opens.Modifier.values()).toString()
              .equals("[SYNTHETIC, MANDATED]"), "Opens.Modifier values");
        check(java.util.List.of(ModuleDescriptor.Modifier.values()).toString()
              .equals("[OPEN, AUTOMATIC, SYNTHETIC, MANDATED]"), "ModuleDescriptor.Modifier values");
        check(ModuleDescriptor.Requires.Modifier.valueOf("TRANSITIVE")
              == ModuleDescriptor.Requires.Modifier.TRANSITIVE, "Requires valueOf");
        check(ModuleDescriptor.Exports.Modifier.valueOf("MANDATED")
              == ModuleDescriptor.Exports.Modifier.MANDATED, "Exports valueOf");
        check(ModuleDescriptor.Opens.Modifier.valueOf("SYNTHETIC")
              == ModuleDescriptor.Opens.Modifier.SYNTHETIC, "Opens valueOf");
        check(ModuleDescriptor.Modifier.valueOf("OPEN") == ModuleDescriptor.Modifier.OPEN,
              "ModuleDescriptor.Modifier valueOf");
        check(ModuleDescriptor.Requires.Modifier.TRANSITIVE.ordinal() == 0, "TRANSITIVE ordinal 0");
        check(ModuleDescriptor.Modifier.OPEN.name().equals("OPEN"), "OPEN name");
        // newModule with an explicit modifier set
        ModuleDescriptor syn = ModuleDescriptor.newModule("syn.m",
                Set.of(ModuleDescriptor.Modifier.SYNTHETIC)).build();
        check(syn.modifiers().equals(Set.of(ModuleDescriptor.Modifier.SYNTHETIC)),
              "SYNTHETIC modifier read back");
        ModuleDescriptor open = ModuleDescriptor.newOpenModule("open.m").build();
        check(open.modifiers().equals(Set.of(ModuleDescriptor.Modifier.OPEN)), "open modifiers [OPEN]");
        check(open.isOpen(), "open module isOpen");
        check(open.opens().isEmpty(), "open module declares no opens entries");
    }

    // derived packages, content-sensitive equals, ByteBuffer negative path
    static void testDescriptorExtras() {
        ModuleDescriptor d = ModuleDescriptor.newModule("p.m")
                .exports("p.a").opens("p.b").build();
        check(d.packages().equals(Set.of("p.a", "p.b")),
              "packages() derived from exports+opens sources");
        ModuleDescriptor mc = ModuleDescriptor.newModule("m").mainClass("m.Main").build();
        check(mc.packages().equals(Set.of("m")), "main class contributes its package");
        check(mc.mainClass().orElseThrow().equals("m.Main"), "mainClass round trip");
        // equals is content-sensitive, not just name-based
        ModuleDescriptor withExport = ModuleDescriptor.newModule("x").exports("p").build();
        ModuleDescriptor bare = ModuleDescriptor.newModule("x").build();
        check(!withExport.equals(bare), "same name but different content not equal");
        check(withExport.compareTo(ModuleDescriptor.newModule("y").build()) < 0,
              "compareTo orders by name first");
        check(!withExport.equals(ModuleDescriptor.newModule("x").exports("q").build()),
              "different export source not equal");
        ModuleDescriptor v1 = ModuleDescriptor.newModule("v").version("1").build();
        ModuleDescriptor v2 = ModuleDescriptor.newModule("v").version("2").build();
        check(!v1.equals(v2), "different versions not equal");
        check("1.0+1".equals(ModuleDescriptor.Version.parse("1.0+1").toString()),
              "version with build number round trips");
        try {
            ModuleDescriptor.read(java.nio.ByteBuffer.allocate(0));
            check(false, "empty ByteBuffer rejected");
        } catch (InvalidModuleDescriptorException e) {
            check("Truncated module-info.class".equals(e.getMessage()),
                  "empty ByteBuffer exact message");
        }
    }

    // non-module classfile, trailing-zero versions, automatic-module restrictions
    static void testFinalExtras() throws Exception {
        try (InputStream is = String.class.getResourceAsStream("/java/lang/String.class")) {
            check(is != null, "String.class bytes readable");
            try {
                ModuleDescriptor.read(is);
                check(false, "non-module-info classfile rejected");
            } catch (InvalidModuleDescriptorException e) {
                check("access_flags should be ACC_MODULE".equals(e.getMessage()),
                      "ACC_MODULE exact message");
            }
        }
        check(ModuleDescriptor.Version.parse("1.0").compareTo(
                  ModuleDescriptor.Version.parse("1.0.0")) == 0,
              "trailing zeros ignored in version comparison");
        check(ModuleDescriptor.newAutomaticModule("am.pkgs").packages(Set.of("p.q"))
                  .build().packages().equals(Set.of("p.q")), "automatic packages round trip");
        try {
            ModuleDescriptor.newAutomaticModule("am.ex").exports("p.q").build();
            check(false, "automatic module exports rejected");
        } catch (IllegalStateException e) {
            check("Automatic modules cannot declare exported packages".equals(e.getMessage()),
                  "automatic exports exact message");
        }
        check(ModuleDescriptor.newAutomaticModule("am.name")
                  .build().toNameAndVersion().equals("am.name"), "automatic toNameAndVersion");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testReadRealJavaBase();
        testBuilderRoundTrip();
        testRequiresContract();
        testExportsOpensProvidesUses();
        testCopyInOverloads();
        testVersionContract();
        testImmutabilityMatrix();
        testNegativeInputs();
        testReadBrokenModuleInfo();
        testModifierEnums();
        testDescriptorExtras();
        testFinalExtras();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
