/*
 * @test
 * @summary javax.lang.model.util.Elements conformance: white-box structure + black-box behavior.
 * @run main ElementsTest
 */

import java.util.*;
import javax.lang.model.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.*;
import javax.tools.*;
import com.sun.source.util.*;

public class ElementsTest {
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
        Class<?> c = Class.forName("javax.lang.model.util.Elements");
        check(c != null, "javax.lang.model.util.Elements loads");
        check(c.getName().equals("javax.lang.model.util.Elements"), "binary name is javax.lang.model.util.Elements");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "javax.lang.model.util.Elements is public");
        check(c.isInterface() && !c.isAnnotation(), "is a plain interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "getPackageElement"), "declares getPackageElement()");
        check(declares(c, "getTypeElement"), "declares getTypeElement()");
        check(declares(c, "getElementValuesWithDefaults"), "declares getElementValuesWithDefaults()");
        check(declares(c, "getDocComment"), "declares getDocComment()");
        check(declares(c, "isDeprecated"), "declares isDeprecated()");
        check(declares(c, "getBinaryName"), "declares getBinaryName()");
        check(declares(c, "getPackageOf"), "declares getPackageOf()");
        check(declares(c, "getAllMembers"), "declares getAllMembers()");
        check(declares(c, "getAllAnnotationMirrors"), "declares getAllAnnotationMirrors()");
        check(declares(c, "hides"), "declares hides()");
        check(declares(c, "overrides"), "declares overrides()");
        check(declares(c, "getConstantExpression"), "declares getConstantExpression()");
        check(declares(c, "printElements"), "declares printElements()");
        check(declares(c, "getName"), "declares getName()");
        check(declares(c, "isFunctionalInterface"), "declares isFunctionalInterface()");
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
        TypeElement str = el.getTypeElement("java.lang.String");
        TypeElement sample = el.getTypeElement("sample.Sample");
        check(str != null && sample != null, "getTypeElement resolves");
        check(el.getPackageElement("sample") != null, "getPackageElement resolves");
        check(el.getPackageElement("no.such.pkg") == null, "getPackageElement unknown -> null");
        check(el.getTypeElement("no.Such") == null, "getTypeElement unknown -> null");
        check(el.getPackageOf(sample).getQualifiedName().contentEquals("sample"), "getPackageOf(Sample)");
        check(el.getPackageOf(str).getQualifiedName().contentEquals("java.lang"), "getPackageOf(String)");
        List<String> members = new ArrayList<>();
        for (Element e : el.getAllMembers(str)) members.add(e.getSimpleName().toString());
        Collections.sort(members);
        check(members.contains("length") && members.contains("toString") && members.contains("hashCode"), "getAllMembers(String) sorted contains length/toString/hashCode");
        check(el.getBinaryName(sample).contentEquals("sample.Sample"), "getBinaryName");
        check(el.getName("sample.Sample").contentEquals("sample.Sample"), "getName roundtrip");
        TypeElement runnable = el.getTypeElement("java.lang.Runnable");
        check(el.isFunctionalInterface(runnable), "Runnable is functional interface");
        check(!el.isFunctionalInterface(sample), "Sample is not functional interface");
        ModuleElement base = el.getModuleElement("java.base");
        check(base != null, "getModuleElement(java.base)");
        check(el.getModuleOf(str) != null, "getModuleOf(String)");
        check(!el.isAutomaticModule(base), "java.base !automatic");
        TypeElement old = el.getTypeElement("sample.Old");
        List<? extends AnnotationMirror> anns = el.getAllAnnotationMirrors(old);
        check(anns.size() == 1, "Old has one annotation");
        check(anns.get(0).getAnnotationType().asElement().getSimpleName().contentEquals("Deprecated"), "annotation is Deprecated");
        Map<String,Object> dv = new HashMap<>();
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> en : el.getElementValuesWithDefaults(anns.get(0)).entrySet())
            dv.put(en.getKey().getSimpleName().toString(), en.getValue().getValue());
        check("".equals(dv.get("since")), "Deprecated.since default empty");
        check(Boolean.FALSE.equals(dv.get("forRemoval")), "Deprecated.forRemoval default false");
        check(el.isDeprecated(old), "Old isDeprecated");
        check(!el.isDeprecated(sample), "Sample !isDeprecated");
        check(el.getDocComment(sample).contains("Doc for Sample"), "getDocComment retains source comment");
        check(el.getDocCommentKind(sample) == Elements.DocCommentKind.TRADITIONAL, "getDocCommentKind TRADITIONAL");
        check(el.getOrigin(sample) == Elements.Origin.EXPLICIT, "getOrigin EXPLICIT");
        ExecutableElement sampleRun = null;
        ExecutableElement runnableRun = null;
        for (Element e : el.getAllMembers(sample)) if (e.getKind() == ElementKind.METHOD && e.getSimpleName().contentEquals("run")) sampleRun = (ExecutableElement) e;
        for (Element e : el.getAllMembers(runnable)) if (e.getKind() == ElementKind.METHOD && e.getSimpleName().contentEquals("run")) runnableRun = (ExecutableElement) e;
        check(sampleRun != null && runnableRun != null, "run methods resolved");
        check(el.overrides(sampleRun, runnableRun, sample), "Sample.run overrides Runnable.run");
        check(!el.isBridge(sampleRun), "Sample.run !bridge");
        check("42".equals(el.getConstantExpression(Integer.valueOf(42))), "constant expression int");
        check("\"abc\"".equals(el.getConstantExpression("abc")), "constant expression string");
        boolean threw = false;
        try { el.getConstantExpression(null); } catch (IllegalArgumentException e) { threw = true; }
        check(threw, "getConstantExpression(null) throws IllegalArgumentException");
        java.io.StringWriter sw = new java.io.StringWriter();
        el.printElements(sw, sample);
        check(sw.toString().contains("Sample") && sw.toString().contains("package sample"), "printElements writes element");
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
