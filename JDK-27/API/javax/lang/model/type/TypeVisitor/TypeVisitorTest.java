/*
 * @test
 * @summary javax.lang.model.type.TypeVisitor conformance: white-box structure + black-box behavior.
 * @run main TypeVisitorTest
 */

import java.util.*;
import javax.lang.model.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.*;
import javax.tools.*;
import com.sun.source.util.*;

public class TypeVisitorTest {
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
        Class<?> c = Class.forName("javax.lang.model.type.TypeVisitor");
        check(c != null, "javax.lang.model.type.TypeVisitor loads");
        check(c.getName().equals("javax.lang.model.type.TypeVisitor"), "binary name is javax.lang.model.type.TypeVisitor");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "javax.lang.model.type.TypeVisitor is public");
        check(c.isInterface() && !c.isAnnotation(), "is a plain interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "visit"), "declares visit()");
        check(declares(c, "visitPrimitive"), "declares visitPrimitive()");
        check(declares(c, "visitNull"), "declares visitNull()");
        check(declares(c, "visitArray"), "declares visitArray()");
        check(declares(c, "visitDeclared"), "declares visitDeclared()");
        check(declares(c, "visitError"), "declares visitError()");
        check(declares(c, "visitTypeVariable"), "declares visitTypeVariable()");
        check(declares(c, "visitWildcard"), "declares visitWildcard()");
        check(declares(c, "visitExecutable"), "declares visitExecutable()");
        check(declares(c, "visitNoType"), "declares visitNoType()");
        check(declares(c, "visitUnknown"), "declares visitUnknown()");
        check(declares(c, "visitUnion"), "declares visitUnion()");
        check(declares(c, "visitIntersection"), "declares visitIntersection()");
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
        TypeMirror strT = el.getTypeElement("java.lang.String").asType();
        final String[] called = new String[1];
        TypeVisitor<String,Void> v = new TypeVisitor<String,Void>() {
            public String visit(TypeMirror t, Void p){called[0]="visit"; return "v:" + t;}
            public String visitPrimitive(PrimitiveType t, Void p){return "prim";}
            public String visitNull(NullType t, Void p){return "null";}
            public String visitArray(ArrayType t, Void p){return "arr";}
            public String visitDeclared(DeclaredType t, Void p){called[0]="declared"; return "decl:" + t.asElement().getSimpleName();}
            public String visitError(ErrorType t, Void p){return "err";}
            public String visitTypeVariable(TypeVariable t, Void p){return "tv";}
            public String visitWildcard(WildcardType t, Void p){return "wc";}
            public String visitExecutable(ExecutableType t, Void p){return "exec";}
            public String visitNoType(NoType t, Void p){return "no";}
            public String visitUnion(UnionType t, Void p){return "union";}
            public String visitIntersection(IntersectionType t, Void p){return "inter";}
            public String visitUnknown(TypeMirror t, Void p){called[0]="unknown"; return "u";}
        };
        check("decl:String".equals(strT.accept(v, null)), "accept dispatches to visitDeclared");
        check("declared".equals(called[0]), "visitDeclared invoked (not visit)");
        check("v:java.lang.String".equals(v.visit(strT)), "single-arg visit delegates to visit(t,null)");
        check("prim".equals(v.visitPrimitive(ty.getPrimitiveType(TypeKind.INT), null)), "direct visitPrimitive");
        check("union".equals(v.visitUnion(null, null)) && "inter".equals(v.visitIntersection(null, null)), "direct visitUnion/visitIntersection");
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
