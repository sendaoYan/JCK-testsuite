/*
 * @test
 * @summary javax.lang.model.util.AbstractElementVisitor14 conformance: white-box structure + black-box behavior.
 * @run main AbstractElementVisitor14Test
 */

import java.util.*;
import javax.lang.model.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.*;
import javax.tools.*;
import com.sun.source.util.*;

public class AbstractElementVisitor14Test {
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
        Class<?> c = Class.forName("javax.lang.model.util.AbstractElementVisitor14");
        check(c != null, "javax.lang.model.util.AbstractElementVisitor14 loads");
        check(c.getName().equals("javax.lang.model.util.AbstractElementVisitor14"), "binary name is javax.lang.model.util.AbstractElementVisitor14");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "javax.lang.model.util.AbstractElementVisitor14 is public");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass().getName().equals("javax.lang.model.util.AbstractElementVisitor9"), "superclass is javax.lang.model.util.AbstractElementVisitor9");
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
        final String[] called = new String[1];
        AbstractElementVisitor14<String,Integer> v = new AbstractElementVisitor14<String,Integer>() {
            public String visitType(TypeElement e, Integer p) { called[0] = "type"; return "T"; }
            public String visitPackage(PackageElement e, Integer p) { return "P"; }
            public String visitVariable(VariableElement e, Integer p) { return "V"; }
            public String visitExecutable(ExecutableElement e, Integer p) { return "E"; }
            public String visitTypeParameter(TypeParameterElement e, Integer p) { return "TP"; }
            public String visitModule(ModuleElement e, Integer p) { called[0] = "module"; return "M:" + e.getSimpleName(); }
            public String visitRecordComponent(RecordComponentElement e, Integer p) { called[0] = "record"; return "R:" + e.getSimpleName(); }
            public String visitUnknown(Element e, Integer p) { called[0] = "unknown"; return "U:" + e.getSimpleName(); }
        };
        check("T".equals(v.visit(sample, 1)) && "type".equals(called[0]), "visit(TypeElement) -> visitType");
        check("M:base".equals(v.visit(el.getModuleElement("java.base"), 2)), "visit(Module) expected");
        check("R:x".equals(v.visit(el.getTypeElement("sample.Rec").getRecordComponents().get(0), 3)), "visit(RecordComponent) expected");
        AbstractElementVisitor14<String,Integer> v2 = new AbstractElementVisitor14<String,Integer>() {
            public String visitType(TypeElement e, Integer p) { return "T"; }
            public String visitPackage(PackageElement e, Integer p) { return "P"; }
            public String visitVariable(VariableElement e, Integer p) { return "V"; }
            public String visitExecutable(ExecutableElement e, Integer p) { return "E"; }
            public String visitTypeParameter(TypeParameterElement e, Integer p) { return "TP"; }
            public String visitModule(ModuleElement e, Integer p) { return "M"; }
            public String visitRecordComponent(RecordComponentElement e, Integer p) { return "R"; }
        };
        boolean threw = false;
        try { v2.visitUnknown(sample, 0); } catch (UnknownElementException ex) { threw = true; check(ex.getUnknownElement() == sample, "throws with element"); }
        check(threw, "default visitUnknown throws UnknownElementException");
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
