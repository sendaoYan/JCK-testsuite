/*
 * @test
 * @summary javax.lang.model.element.ModuleElement.Directive conformance: white-box structure + black-box behavior.
 * @run main DirectiveTest
 */

import java.util.*;
import javax.lang.model.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.*;
import javax.tools.*;
import com.sun.source.util.*;

public class DirectiveTest {
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
        Class<?> c = Class.forName("javax.lang.model.element.ModuleElement$Directive");
        check(c != null, "javax.lang.model.element.ModuleElement$Directive loads");
        check(c.getName().equals("javax.lang.model.element.ModuleElement$Directive"), "binary name is javax.lang.model.element.ModuleElement$Directive");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "javax.lang.model.element.ModuleElement$Directive is public");
        check(c.isInterface() && !c.isAnnotation(), "is a plain interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "getKind"), "declares getKind()");
        check(declares(c, "accept"), "declares accept()");
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
        List<? extends ModuleElement.Directive> dirs = el.getModuleElement("java.base").getDirectives();
        check(!dirs.isEmpty(), "java.base has directives");
        ModuleElement.Directive d = dirs.get(0);
        check(d.getKind() != null, "getKind non-null");
        ModuleElement.DirectiveVisitor<String,Void> v = new ModuleElement.DirectiveVisitor<String,Void>() {
            public String visitRequires(ModuleElement.RequiresDirective x, Void p){return "R";}
            public String visitExports(ModuleElement.ExportsDirective x, Void p){return "E";}
            public String visitOpens(ModuleElement.OpensDirective x, Void p){return "O";}
            public String visitUses(ModuleElement.UsesDirective x, Void p){return "U";}
            public String visitProvides(ModuleElement.ProvidesDirective x, Void p){return "P";}
        };
        String expected = switch (d.getKind()) {
            case REQUIRES -> "R";
            case EXPORTS -> "E";
            case OPENS -> "O";
            case USES -> "U";
            case PROVIDES -> "P";
        };
        check(expected.equals(d.accept(v, null)), "accept dispatches by kind");
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
