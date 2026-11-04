/*
 * @test
 * @summary javax.lang.model.element.AnnotationValueVisitor conformance: white-box structure + black-box behavior.
 * @run main AnnotationValueVisitorTest
 */

import java.util.*;
import javax.lang.model.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.*;
import javax.tools.*;
import com.sun.source.util.*;

public class AnnotationValueVisitorTest {
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
        Class<?> c = Class.forName("javax.lang.model.element.AnnotationValueVisitor");
        check(c != null, "javax.lang.model.element.AnnotationValueVisitor loads");
        check(c.getName().equals("javax.lang.model.element.AnnotationValueVisitor"), "binary name is javax.lang.model.element.AnnotationValueVisitor");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "javax.lang.model.element.AnnotationValueVisitor is public");
        check(c.isInterface() && !c.isAnnotation(), "is a plain interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "visit"), "declares visit()");
        check(declares(c, "visitBoolean"), "declares visitBoolean()");
        check(declares(c, "visitString"), "declares visitString()");
        check(declares(c, "visitUnknown"), "declares visitUnknown()");
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
        TypeElement w = el.getTypeElement("sample.W");
        AnnotationValue av = el.getAllAnnotationMirrors(w).get(0).getElementValues().values().iterator().next();
        final String[] called = new String[1];
        AnnotationValueVisitor<String,Void> v = new AnnotationValueVisitor<String,Void>() {
            public String visit(AnnotationValue a, Void p){called[0]="visit"; return "v:" + a.getValue();}
            public String visitBoolean(boolean b, Void p){return "b";}
            public String visitByte(byte b, Void p){return "by";}
            public String visitChar(char c, Void p){return "ch";}
            public String visitDouble(double d, Void p){return "d";}
            public String visitFloat(float f, Void p){return "f";}
            public String visitInt(int i, Void p){return "i";}
            public String visitLong(long l, Void p){return "l";}
            public String visitShort(short s, Void p){return "sh";}
            public String visitString(String s, Void p){called[0]="string"; return "s:" + s;}
            public String visitType(TypeMirror t, Void p){return "t";}
            public String visitEnumConstant(VariableElement c, Void p){return "e";}
            public String visitAnnotation(AnnotationMirror a, Void p){return "a";}
            public String visitArray(List<? extends AnnotationValue> vals, Void p){return "arr";}
            public String visitUnknown(AnnotationValue a, Void p){called[0]="unknown"; return "u";}
        };
        check("s:unchecked".equals(av.accept(v, null)), "accept dispatches to visitString");
        check("string".equals(called[0]), "visitString invoked (not visit)");
        check("v:unchecked".equals(v.visit(av)), "single-arg visit delegates to visit(av,null)");
        check("i".equals(v.visitInt(7, null)), "direct visitInt");
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
