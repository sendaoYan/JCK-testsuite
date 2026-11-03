/*
 * @test
 * @summary com.sun.source.util.Trees conformance: white-box structure + black-box behavior.
 * @run main TreesTest
 */

import java.lang.reflect.*;

public class TreesTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static java.util.Set<String> ifaceNames(Class<?> c) {
        java.util.Set<String> s = new java.util.HashSet<>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static boolean declares(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("com.sun.source.util.Trees");
        check(c != null, "com.sun.source.util.Trees loads");
        check(c.getName().equals("com.sun.source.util.Trees"), "binary name is com.sun.source.util.Trees");
        check(Modifier.isPublic(c.getModifiers()), "com.sun.source.util.Trees is public");
        check(!c.isInterface(), "com.sun.source.util.Trees is not an interface");
        check(Modifier.isAbstract(c.getModifiers()), "com.sun.source.util.Trees is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass is java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
    }

    // ---- shared util helper ----
    static class U { com.sun.source.util.JavacTask t; com.sun.source.tree.CompilationUnitTree cu; com.sun.source.util.Trees trees; com.sun.source.util.DocTrees dt; }
    static class MSrc extends javax.tools.SimpleJavaFileObject {
        final String c;
        MSrc(String n, String c){ super(java.net.URI.create("string:///"+n+".java"), javax.tools.JavaFileObject.Kind.SOURCE); this.c=c; }
        public CharSequence getCharContent(boolean i){ return c; }
    }
    static U setup(String code) throws Exception {
        javax.tools.JavaCompiler cc = javax.tools.ToolProvider.getSystemJavaCompiler();
        javax.tools.DiagnosticCollector<javax.tools.JavaFileObject> diags = new javax.tools.DiagnosticCollector<>();
        javax.tools.StandardJavaFileManager fm = cc.getStandardFileManager(diags, java.util.Locale.ROOT, null);
        com.sun.source.util.JavacTask t = (com.sun.source.util.JavacTask) cc.getTask(null, fm, diags, java.util.List.of("-proc:none"), null, java.util.List.of(new MSrc("S.java", code)));
        com.sun.source.tree.CompilationUnitTree cu = t.parse().iterator().next();
        U u = new U(); u.t = t; u.cu = cu; u.trees = com.sun.source.util.Trees.instance(t); u.dt = com.sun.source.util.DocTrees.instance(t); return u;
    }
    static com.sun.source.tree.ClassTree firstClass(com.sun.source.tree.CompilationUnitTree cu) {
        for (com.sun.source.tree.Tree d : cu.getTypeDecls()) if (d instanceof com.sun.source.tree.ClassTree) return (com.sun.source.tree.ClassTree) d;
        return null;
    }
    static void testBlackBox() throws Exception {
        U u = setup("package p; class D { int f; void m(){} }");
        u.t.analyze();
        com.sun.source.util.Trees trees = com.sun.source.util.Trees.instance(u.t);
        check(trees != null, "Trees.instance non-null");
        check(trees.getSourcePositions() != null, "getSourcePositions");
        com.sun.source.tree.ClassTree ct = firstClass(u.cu);
        com.sun.source.util.TreePath tp = com.sun.source.util.TreePath.getPath(u.cu, ct);
        javax.lang.model.element.Element el = trees.getElement(tp);
        check(el != null, "getElement non-null");
        javax.lang.model.type.TypeMirror tm = trees.getTypeMirror(tp);
        check(tm != null, "getTypeMirror non-null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
