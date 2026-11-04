/*
 * @test
 * @summary javax.lang.model.type.ErrorType conformance: white-box structure + black-box behavior.
 * @run main ErrorTypeTest
 */

import java.util.*;
import javax.lang.model.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.*;
import javax.tools.*;
import com.sun.source.util.*;

public class ErrorTypeTest {
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
        Class<?> c = Class.forName("javax.lang.model.type.ErrorType");
        check(c != null, "javax.lang.model.type.ErrorType loads");
        check(c.getName().equals("javax.lang.model.type.ErrorType"), "binary name is javax.lang.model.type.ErrorType");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "javax.lang.model.type.ErrorType is public");
        check(c.isInterface() && !c.isAnnotation(), "is a plain interface");
        check(ifaceNames(c).contains("javax.lang.model.type.DeclaredType"), "extends javax.lang.model.type.DeclaredType");
    }

    static void testBlackBox() throws Exception {
        String src = """
package sample;
public class Sample { Undefined foo; }
""";
        compile(src);
        TypeElement sample = el.getTypeElement("sample.Sample");
        VariableElement foo = null;
        for (Element e : sample.getEnclosedElements()) if (e.getKind() == ElementKind.FIELD) foo = (VariableElement) e;
        ErrorType et = (ErrorType) foo.asType();
        check(et.getKind() == TypeKind.ERROR, "kind ERROR");
        check(et instanceof DeclaredType, "is DeclaredType");
        check("Undefined".equals(et.toString()), "toString Undefined");
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
