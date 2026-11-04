/*
 * @test
 * @summary com.sun.source.util.DocTreePathScanner conformance: white-box structure + black-box behavior.
 * @run main DocTreePathScannerTest
 */

import java.lang.reflect.*;

public class DocTreePathScannerTest {
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
        Class<?> c = Class.forName("com.sun.source.util.DocTreePathScanner");
        check(c != null, "com.sun.source.util.DocTreePathScanner loads");
        check(c.getName().equals("com.sun.source.util.DocTreePathScanner"), "binary name is com.sun.source.util.DocTreePathScanner");
        check(Modifier.isPublic(c.getModifiers()), "com.sun.source.util.DocTreePathScanner is public");
        check(!c.isInterface(), "com.sun.source.util.DocTreePathScanner is not an interface");
        check(!Modifier.isAbstract(c.getModifiers()), "com.sun.source.util.DocTreePathScanner is concrete");
        check(c.getSuperclass().getName().equals("com.sun.source.util.DocTreeScanner"), "superclass is com.sun.source.util.DocTreeScanner");
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
        U u = setup("package p;\n/** Sum.\n * @return r\n */\npublic class D { public int m() { return 1; } }");
        com.sun.source.tree.ClassTree ct = firstClass(u.cu);
        com.sun.source.util.TreePath tp = com.sun.source.util.TreePath.getPath(u.cu, ct);
        com.sun.source.doctree.DocCommentTree dct = u.dt.getDocCommentTree(tp);
        com.sun.source.util.DocTreePath dtp = new com.sun.source.util.DocTreePath(tp, dct);
        final int[] n = new int[1];
        new com.sun.source.util.DocTreePathScanner<Void,Void>() {
            @Override public Void scan(com.sun.source.doctree.DocTree t, Void v) { if (t!=null) n[0]++; return super.scan(t, v); }
        }.scan(dtp, null);
        check(n[0] > 0, "DocTreePathScanner scanned nodes");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
