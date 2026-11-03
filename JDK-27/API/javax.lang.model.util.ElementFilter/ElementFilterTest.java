/*
 * @test
 * @summary javax.lang.model.util.ElementFilter conformance: white-box structure + black-box behavior.
 * @run main ElementFilterTest
 */

import java.util.*;
import javax.lang.model.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.*;
import javax.tools.*;
import com.sun.source.util.*;

public class ElementFilterTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    static Set<String> ifaceNames(Class<?> c) { Set<String> s = new HashSet<>(); for (Class<?> i : c.getInterfaces()) s.add(i.getName()); return s; }
    static boolean declares(Class<?> c, String name) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }

    // ---- compiler harness (headless, deterministic) ----
    static javax.lang.model.util.Elements el;
    static javax.lang.model.util.Types ty;
    static com.sun.source.util.JavacTask jt;
    static javax.tools.StandardJavaFileManager fm;
    static java.nio.file.Path tmpDir;
    static void compile(String source) throws Exception {
        javax.tools.JavaCompiler c = javax.tools.ToolProvider.getSystemJavaCompiler();
        tmpDir = java.nio.file.Files.createTempDirectory("jck27bh-langmodel");
        java.nio.file.Path src = tmpDir.resolve("sample/Sample.java");
        java.nio.file.Files.createDirectories(src.getParent());
        java.nio.file.Files.writeString(src, source, java.nio.charset.StandardCharsets.UTF_8);
        javax.tools.DiagnosticCollector<javax.tools.JavaFileObject> diags = new javax.tools.DiagnosticCollector<>();
        fm = c.getStandardFileManager(diags, java.util.Locale.ROOT, java.nio.charset.StandardCharsets.UTF_8);
        Iterable<? extends javax.tools.JavaFileObject> units = fm.getJavaFileObjectsFromFiles(java.util.List.of(src.toFile()));
        jt = (com.sun.source.util.JavacTask) c.getTask(null, fm, diags, java.util.List.of("-proc:none"), null, units);
        el = jt.getElements();
        ty = jt.getTypes();
        jt.parse();
        jt.analyze();
    }
    static void cleanup() {
        try { if (fm != null) fm.close(); } catch (java.io.IOException e) {}
        if (tmpDir != null) {
            try (java.util.stream.Stream<java.nio.file.Path> s = java.nio.file.Files.walk(tmpDir)) {
                s.sorted(java.util.Comparator.reverseOrder()).forEach(p -> { try { java.nio.file.Files.deleteIfExists(p); } catch (java.io.IOException e) {} });
            } catch (java.io.IOException e) {}
        }
    }


    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.lang.model.util.ElementFilter");
        check(c != null, "javax.lang.model.util.ElementFilter loads");
        check(c.getName().equals("javax.lang.model.util.ElementFilter"), "binary name is javax.lang.model.util.ElementFilter");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "javax.lang.model.util.ElementFilter is public");
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass is java.lang.Object");
        check(declares(c, "fieldsIn"), "declares fieldsIn()");
        check(declares(c, "recordComponentsIn"), "declares recordComponentsIn()");
        check(declares(c, "constructorsIn"), "declares constructorsIn()");
        check(declares(c, "methodsIn"), "declares methodsIn()");
        check(declares(c, "typesIn"), "declares typesIn()");
        check(declares(c, "packagesIn"), "declares packagesIn()");
        check(declares(c, "modulesIn"), "declares modulesIn()");
        check(declares(c, "exportsIn"), "declares exportsIn()");
        check(declares(c, "opensIn"), "declares opensIn()");
        check(declares(c, "providesIn"), "declares providesIn()");
        check(declares(c, "requiresIn"), "declares requiresIn()");
        check(declares(c, "usesIn"), "declares usesIn()");
    }

    static void testBlackBox() throws Exception {
        String src = """
package sample;
/** Doc for Sample. */
public class Sample implements Runnable {
    public int field;
    public java.util.List<String> list;
    public Sample() {}
    public Sample(int a) {}
    public String method(int a) { return null; }
    public void run() {}
    public <T> T id(T x) { return x; }
}
record Rec(int x) {}
@Deprecated class Old {}
@interface Marker { String value(); }
@Marker("unchecked") class W {}
enum Color { RED, GREEN }
""";
        compile(src);
        TypeElement sample = el.getTypeElement("sample.Sample");
        List<VariableElement> fields = ElementFilter.fieldsIn(sample.getEnclosedElements());
        List<String> fnames = new ArrayList<>();
        for (VariableElement f : fields) fnames.add(f.getSimpleName().toString());
        Collections.sort(fnames);
        check(fnames.equals(List.of("field", "list")), "fieldsIn sorted [field,list]");
        check(ElementFilter.constructorsIn(sample.getEnclosedElements()).size() == 2, "constructorsIn == 2");
        List<String> mnames = new ArrayList<>();
        for (ExecutableElement m : ElementFilter.methodsIn(sample.getEnclosedElements())) mnames.add(m.getSimpleName().toString());
        Collections.sort(mnames);
        check(mnames.equals(List.of("id", "method", "run")), "methodsIn sorted [id,method,run]");
        check(ElementFilter.typesIn(sample.getEnclosedElements()).isEmpty(), "typesIn empty (no nested types)");
        TypeElement rec = el.getTypeElement("sample.Rec");
        List<RecordComponentElement> rcs = ElementFilter.recordComponentsIn(rec.getRecordComponents());
        check(rcs.size() == 1 && rcs.get(0).getSimpleName().contentEquals("x"), "recordComponentsIn [x]");
        Set<Element> mixed = new HashSet<>();
        mixed.add(sample);
        mixed.add(el.getPackageElement("sample"));
        check(ElementFilter.packagesIn(mixed).size() == 1, "packagesIn == 1");
        check(ElementFilter.typesIn(mixed).size() == 1, "typesIn(mixed) == 1");
        Set<Element> mods = new HashSet<>();
        mods.add(el.getModuleElement("java.base"));
        check(ElementFilter.modulesIn(mods).size() == 1, "modulesIn == 1");
        List<? extends ModuleElement.Directive> dirs = el.getModuleElement("java.base").getDirectives();
        check(ElementFilter.requiresIn(dirs).isEmpty(), "java.base requiresIn empty");
        check(!ElementFilter.exportsIn(dirs).isEmpty(), "exportsIn finds exports");
        check(ElementFilter.exportsIn(dirs).get(0).getPackage() != null, "exports getPackage");
        check(ElementFilter.usesIn(dirs).stream().allMatch(d -> d.getKind() == ModuleElement.DirectiveKind.USES), "usesIn all USES");
        check(!ElementFilter.requiresIn(el.getModuleElement("java.sql").getDirectives()).isEmpty(), "java.sql requiresIn non-empty");
    }

    public static void main(String[] args) throws Exception {
        try {
            testWhiteBox();
            testBlackBox();
            System.out.println("PASS (" + passed + " assertions)");
        } finally {
            cleanup();
        }
    }
}
