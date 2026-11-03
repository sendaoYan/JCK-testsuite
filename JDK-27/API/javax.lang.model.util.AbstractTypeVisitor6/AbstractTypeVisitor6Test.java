/*
 * @test
 * @summary javax.lang.model.util.AbstractTypeVisitor6 conformance: white-box structure + black-box behavior.
 * @run main AbstractTypeVisitor6Test
 */

import java.util.*;
import javax.lang.model.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.*;
import javax.tools.*;
import com.sun.source.util.*;

public class AbstractTypeVisitor6Test {
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
        Class<?> c = Class.forName("javax.lang.model.util.AbstractTypeVisitor6");
        check(c != null, "javax.lang.model.util.AbstractTypeVisitor6 loads");
        check(c.getName().equals("javax.lang.model.util.AbstractTypeVisitor6"), "binary name is javax.lang.model.util.AbstractTypeVisitor6");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "javax.lang.model.util.AbstractTypeVisitor6 is public");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass is java.lang.Object");
        check(ifaceNames(c).contains("javax.lang.model.type.TypeVisitor"), "implements javax.lang.model.type.TypeVisitor");
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
        AbstractTypeVisitor6<String,Void> v = new AbstractTypeVisitor6<String,Void>() {
            public String visitPrimitive(PrimitiveType t, Void p) { return "P:" + t; }
            public String visitNull(NullType t, Void p) { return "NULL"; }
            public String visitArray(ArrayType t, Void p) { return "ARR"; }
            public String visitDeclared(DeclaredType t, Void p) { return "D:" + t.asElement().getSimpleName(); }
            public String visitError(ErrorType t, Void p) { return "ERR"; }
            public String visitTypeVariable(TypeVariable t, Void p) { return "TV"; }
            public String visitWildcard(WildcardType t, Void p) { return "WC"; }
            public String visitExecutable(ExecutableType t, Void p) { return "EXEC"; }
            public String visitNoType(NoType t, Void p) { return "NO"; }
            public String visitUnion(UnionType t, Void p) { return "UNION"; }
            public String visitIntersection(IntersectionType t, Void p) { return "INTER"; }
            public String visitUnknown(TypeMirror t, Void p) { return "U"; }
        };
        check("P:int".equals(v.visit(ty.getPrimitiveType(TypeKind.INT), null)), "visit(primitive) -> visitPrimitive");
        check("D:String".equals(v.visit(el.getTypeElement("java.lang.String").asType(), null)), "visit(declared) -> visitDeclared");
        check("NULL".equals(v.visit(ty.getNullType(), null)), "visit(null) -> visitNull");
        check("ARR".equals(v.visit(ty.getArrayType(el.getTypeElement("java.lang.String").asType()), null)), "visit(array) -> visitArray");
        check("NO".equals(v.visit(ty.getNoType(TypeKind.VOID), null)), "visit(noType) -> visitNoType");
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
