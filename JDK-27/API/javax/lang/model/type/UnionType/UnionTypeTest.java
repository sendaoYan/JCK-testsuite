/*
 * @test
 * @summary javax.lang.model.type.UnionType conformance: white-box structure + black-box behavior.
 * @run main UnionTypeTest
 */

import java.util.*;
import javax.lang.model.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.*;
import javax.tools.*;
import com.sun.source.util.*;

public class UnionTypeTest {
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
        Class<?> c = Class.forName("javax.lang.model.type.UnionType");
        check(c != null, "javax.lang.model.type.UnionType loads");
        check(c.getName().equals("javax.lang.model.type.UnionType"), "binary name is javax.lang.model.type.UnionType");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "javax.lang.model.type.UnionType is public");
        check(c.isInterface() && !c.isAnnotation(), "is a plain interface");
        check(ifaceNames(c).contains("javax.lang.model.type.TypeMirror"), "extends javax.lang.model.type.TypeMirror");
        check(declares(c, "getAlternatives"), "declares getAlternatives()");
    }

    static void testBlackBox() throws Exception {
        String src = """
package sample;
public class Sample { public void u(){ try{int i=0;} catch(RuntimeException|Error e){} } }
""";
        compile(src);
        TypeElement sample = el.getTypeElement("sample.Sample");
        ExecutableElement u = null;
        for (Element e : sample.getEnclosedElements()) if (e.getSimpleName().contentEquals("u")) u = (ExecutableElement) e;
        com.sun.source.util.Trees trees = com.sun.source.util.Trees.instance(jt);
        final UnionType[] got = new UnionType[1];
        new com.sun.source.util.TreePathScanner<Void, Void>() {
            public Void visitVariable(com.sun.source.tree.VariableTree node, Void p) {
                TypeMirror tm = trees.getTypeMirror(getCurrentPath());
                if (tm != null && tm.getKind() == TypeKind.UNION) got[0] = (UnionType) tm;
                return super.visitVariable(node, p);
            }
        }.scan(trees.getPath(u), null);
        check(got[0] != null, "union type obtained from multi-catch");
        List<String> alts = new ArrayList<>();
        for (TypeMirror a : got[0].getAlternatives()) alts.add(a.toString());
        Collections.sort(alts);
        check(alts.equals(List.of("java.lang.Error", "java.lang.RuntimeException")), "alternatives Error/RuntimeException");
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
