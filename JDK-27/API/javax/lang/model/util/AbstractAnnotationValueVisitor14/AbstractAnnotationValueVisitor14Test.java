/*
 * @test
 * @summary javax.lang.model.util.AbstractAnnotationValueVisitor14 conformance: white-box structure + black-box behavior.
 * @run main AbstractAnnotationValueVisitor14Test
 */

import java.util.*;
import javax.lang.model.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.*;
import javax.tools.*;
import com.sun.source.util.*;

public class AbstractAnnotationValueVisitor14Test {
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
        Class<?> c = Class.forName("javax.lang.model.util.AbstractAnnotationValueVisitor14");
        check(c != null, "javax.lang.model.util.AbstractAnnotationValueVisitor14 loads");
        check(c.getName().equals("javax.lang.model.util.AbstractAnnotationValueVisitor14"), "binary name is javax.lang.model.util.AbstractAnnotationValueVisitor14");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "javax.lang.model.util.AbstractAnnotationValueVisitor14 is public");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass().getName().equals("javax.lang.model.util.AbstractAnnotationValueVisitor9"), "superclass is javax.lang.model.util.AbstractAnnotationValueVisitor9");
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
        AbstractAnnotationValueVisitor14<String,Void> v = new AbstractAnnotationValueVisitor14<String,Void>() {
            public String visitBoolean(boolean b, Void p){return "bool";}
            public String visitByte(byte b, Void p){return "byte";}
            public String visitChar(char c, Void p){return "char";}
            public String visitDouble(double d, Void p){return "double";}
            public String visitFloat(float f, Void p){return "float";}
            public String visitInt(int i, Void p){return "int:"+i;}
            public String visitLong(long l, Void p){return "long";}
            public String visitShort(short s, Void p){return "short";}
            public String visitString(String s, Void p){return "str:"+s;}
            public String visitType(TypeMirror t, Void p){return "type";}
            public String visitEnumConstant(VariableElement c, Void p){return "enum";}
            public String visitAnnotation(AnnotationMirror a, Void p){return "ann";}
            public String visitArray(List<? extends AnnotationValue> vals, Void p){return "array";}
            public String visitUnknown(AnnotationValue x, Void p){return "unknown";}
        };
        check("str:unchecked".equals(av.accept(v, null)), "accept dispatches to visitString");
        check("int:7".equals(v.visitInt(7, null)), "visitInt");
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
