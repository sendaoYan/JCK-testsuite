/*
 * @test
 * @summary javax.lang.model.util.Types conformance: white-box structure + black-box behavior.
 * @run main TypesTest
 */

import java.util.*;
import javax.lang.model.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.*;
import javax.tools.*;
import com.sun.source.util.*;

public class TypesTest {
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
        Class<?> c = Class.forName("javax.lang.model.util.Types");
        check(c != null, "javax.lang.model.util.Types loads");
        check(c.getName().equals("javax.lang.model.util.Types"), "binary name is javax.lang.model.util.Types");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "javax.lang.model.util.Types is public");
        check(c.isInterface() && !c.isAnnotation(), "is a plain interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "asElement"), "declares asElement()");
        check(declares(c, "isSameType"), "declares isSameType()");
        check(declares(c, "isSubtype"), "declares isSubtype()");
        check(declares(c, "isAssignable"), "declares isAssignable()");
        check(declares(c, "contains"), "declares contains()");
        check(declares(c, "isSubsignature"), "declares isSubsignature()");
        check(declares(c, "directSupertypes"), "declares directSupertypes()");
        check(declares(c, "erasure"), "declares erasure()");
        check(declares(c, "boxedClass"), "declares boxedClass()");
        check(declares(c, "unboxedType"), "declares unboxedType()");
        check(declares(c, "capture"), "declares capture()");
        check(declares(c, "getPrimitiveType"), "declares getPrimitiveType()");
        check(declares(c, "getNullType"), "declares getNullType()");
        check(declares(c, "getNoType"), "declares getNoType()");
        check(declares(c, "getArrayType"), "declares getArrayType()");
        check(declares(c, "getWildcardType"), "declares getWildcardType()");
        check(declares(c, "getDeclaredType"), "declares getDeclaredType()");
        check(declares(c, "asMemberOf"), "declares asMemberOf()");
        check(declares(c, "stripAnnotations"), "declares stripAnnotations()");
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
        TypeElement obj = el.getTypeElement("java.lang.Object");
        TypeElement sample = el.getTypeElement("sample.Sample");
        TypeMirror strT = str.asType(), objT = obj.asType();
        check(ty.isSameType(strT, strT), "isSameType reflexive");
        check(!ty.isSameType(strT, objT), "!isSameType distinct");
        check(ty.isSubtype(strT, objT) && ty.isSubtype(sample.asType(), objT), "isSubtype to Object");
        check(!ty.isSubtype(objT, strT), "!isSubtype Object <: String");
        check(ty.isAssignable(strT, objT), "isAssignable String -> Object");
        check(!ty.isAssignable(objT, strT), "!isAssignable Object -> String");
        check(ty.asElement(strT).getSimpleName().contentEquals("String"), "asElement(declared)");
        check(ty.asElement(ty.getPrimitiveType(TypeKind.INT)) == null, "asElement(primitive) null");
        DeclaredType listStr = ty.getDeclaredType(el.getTypeElement("java.util.List"), strT);
        check("java.util.List".equals(ty.erasure(listStr).toString()), "erasure(List<String>)");
        List<String> dsn = new ArrayList<>();
        for (TypeMirror t : ty.directSupertypes(sample.asType())) dsn.add(t.toString());
        Collections.sort(dsn);
        check(dsn.equals(List.of("java.lang.Object", "java.lang.Runnable")), "Sample direct supertypes == [Object,Runnable]");
        check(ty.boxedClass(ty.getPrimitiveType(TypeKind.INT)).getQualifiedName().contentEquals("java.lang.Integer"), "boxedClass(int)");
        check(ty.unboxedType(el.getTypeElement("java.lang.Integer").asType()).getKind() == TypeKind.INT, "unboxedType(Integer)");
        boolean ubt = false;
        try { ty.unboxedType(ty.getNullType()); } catch (IllegalArgumentException e) { ubt = true; }
        check(ubt, "unboxedType(nulltype) throws IllegalArgumentException");
        check(ty.capture(listStr).equals(listStr), "capture(plain declared) identity");
        check(ty.contains(listStr, listStr), "contains reflexive");
        PrimitiveType intT = ty.getPrimitiveType(TypeKind.INT);
        check(intT.getKind() == TypeKind.INT, "getPrimitiveType INT");
        boolean gpt = false;
        try { ty.getPrimitiveType(TypeKind.VOID); } catch (IllegalArgumentException e) { gpt = true; }
        check(gpt, "getPrimitiveType(VOID) throws IllegalArgumentException");
        check(ty.getNullType().getKind() == TypeKind.NULL, "getNullType");
        check(ty.getNoType(TypeKind.VOID).getKind() == TypeKind.VOID, "getNoType VOID");
        check(ty.getNoType(TypeKind.NONE).getKind() == TypeKind.NONE, "getNoType NONE");
        check(el.getPackageElement("sample").asType().getKind() == TypeKind.PACKAGE, "package asType PACKAGE");
        check(el.getModuleElement("java.base").asType().getKind() == TypeKind.MODULE, "module asType MODULE");
        ArrayType arr = ty.getArrayType(strT);
        check(arr.getKind() == TypeKind.ARRAY && arr.getComponentType().equals(strT), "getArrayType");
        check(ty.getArrayType(arr).getComponentType().equals(arr), "array of array");
        WildcardType wc = ty.getWildcardType(null, null);
        check(wc.getExtendsBound() == null && wc.getSuperBound() == null, "unbounded wildcard");
        check(ty.getWildcardType(strT, null).getExtendsBound().equals(strT), "wildcard extends");
        check(ty.getWildcardType(null, strT).getSuperBound().equals(strT), "wildcard super");
        check(intT == ty.stripAnnotations(intT), "stripAnnotations primitive identity");
        check(listStr.getTypeArguments().size() == 1, "getDeclaredType type args");
        // asMemberOf: String.length -> int
        ExecutableElement length = null;
        for (Element e : el.getAllMembers(str)) if (e.getKind() == ElementKind.METHOD && e.getSimpleName().contentEquals("length")) length = (ExecutableElement) e;
        check(length != null, "String.length resolved");
        check(((ExecutableType) ty.asMemberOf((DeclaredType) strT, length)).getReturnType().getKind() == TypeKind.INT, "asMemberOf String.length returns int");
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
