/*
 * @test
 * @summary javax.lang.model.type.TypeMirror conformance: white-box structure + black-box behavior.
 * @run main TypeMirrorTest
 */

import java.util.*;
import javax.lang.model.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.*;
import javax.tools.*;
import com.sun.source.util.*;

public class TypeMirrorTest {
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
        Class<?> c = Class.forName("javax.lang.model.type.TypeMirror");
        check(c != null, "javax.lang.model.type.TypeMirror loads");
        check(c.getName().equals("javax.lang.model.type.TypeMirror"), "binary name is javax.lang.model.type.TypeMirror");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "javax.lang.model.type.TypeMirror is public");
        check(c.isInterface() && !c.isAnnotation(), "is a plain interface");
        check(ifaceNames(c).contains("javax.lang.model.AnnotatedConstruct"), "extends javax.lang.model.AnnotatedConstruct");
        check(declares(c, "getKind"), "declares getKind()");
        check(declares(c, "accept"), "declares accept()");
        check(declares(c, "getAnnotationMirrors"), "declares getAnnotationMirrors()");
        check(declares(c, "getAnnotation"), "declares getAnnotation()");
        check(declares(c, "getAnnotationsByType"), "declares getAnnotationsByType()");
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
        TypeMirror t = el.getTypeElement("java.lang.String").asType();
        check(t.getKind() == TypeKind.DECLARED, "kind DECLARED");
        check("java.lang.String".equals(t.toString()), "toString");
        check(t.getAnnotationMirrors() != null, "annotation mirrors non-null");
        check(t.getAnnotation(Deprecated.class) == null, "String no Deprecated");
        String r = t.accept(new SimpleTypeVisitor6<String,Void>("d") { public String visitDeclared(DeclaredType x, Void p) { return x.toString(); } }, null);
        check("java.lang.String".equals(r), "accept dispatches to visitDeclared");
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
