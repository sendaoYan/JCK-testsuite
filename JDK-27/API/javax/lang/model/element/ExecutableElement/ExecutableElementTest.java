/*
 * @test
 * @summary javax.lang.model.element.ExecutableElement conformance: white-box structure + black-box behavior.
 * @run main ExecutableElementTest
 */

import java.util.*;
import javax.lang.model.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.*;
import javax.tools.*;
import com.sun.source.util.*;

public class ExecutableElementTest {
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
        Class<?> c = Class.forName("javax.lang.model.element.ExecutableElement");
        check(c != null, "javax.lang.model.element.ExecutableElement loads");
        check(c.getName().equals("javax.lang.model.element.ExecutableElement"), "binary name is javax.lang.model.element.ExecutableElement");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "javax.lang.model.element.ExecutableElement is public");
        check(c.isInterface() && !c.isAnnotation(), "is a plain interface");
        check(ifaceNames(c).contains("javax.lang.model.element.Element"), "extends javax.lang.model.element.Element");
        check(ifaceNames(c).contains("javax.lang.model.element.Parameterizable"), "extends javax.lang.model.element.Parameterizable");
        check(declares(c, "getTypeParameters"), "declares getTypeParameters()");
        check(declares(c, "getReturnType"), "declares getReturnType()");
        check(declares(c, "getParameters"), "declares getParameters()");
        check(declares(c, "getReceiverType"), "declares getReceiverType()");
        check(declares(c, "isVarArgs"), "declares isVarArgs()");
        check(declares(c, "isDefault"), "declares isDefault()");
        check(declares(c, "getThrownTypes"), "declares getThrownTypes()");
        check(declares(c, "getDefaultValue"), "declares getDefaultValue()");
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
        ExecutableElement ctor = null, method = null;
        for (Element e : sample.getEnclosedElements()) {
            if (e.getKind() == ElementKind.CONSTRUCTOR && ((ExecutableElement) e).getParameters().isEmpty()) ctor = (ExecutableElement) e;
            if (e.getKind() == ElementKind.METHOD && e.getSimpleName().contentEquals("method")) method = (ExecutableElement) e;
        }
        check(ctor != null && method != null, "constructor and method resolved");
        check("java.lang.String".equals(method.getReturnType().toString()), "method return String");
        check(method.getParameters().size() == 1 && method.getParameters().get(0).asType().getKind() == TypeKind.INT, "method param int");
        check(!method.isVarArgs() && !method.isDefault(), "method !varargs !default");
        check(method.getThrownTypes().isEmpty(), "method no thrown");
        check(method.getDefaultValue() == null, "method no default value");
        check(ctor.getReturnType().getKind() == TypeKind.VOID, "constructor return VOID");
        check(ctor.getParameters().isEmpty(), "no-arg constructor");
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
