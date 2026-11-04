/*
 * @test
 * @summary com.sun.source.doctree.ErroneousTree conformance: white-box structure + black-box behavior.
 * @modules jdk.compiler
 * @run main ErroneousTreeTest
 */

import java.lang.reflect.*;

public class ErroneousTreeTest {
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
        Class<?> c = Class.forName("com.sun.source.doctree.ErroneousTree");
        check(c != null, "com.sun.source.doctree.ErroneousTree loads");
        check(c.getName().equals("com.sun.source.doctree.ErroneousTree"), "binary name is com.sun.source.doctree.ErroneousTree");
        check(Modifier.isPublic(c.getModifiers()), "com.sun.source.doctree.ErroneousTree is public");
        check(c.isInterface(), "com.sun.source.doctree.ErroneousTree is an interface");
        check(ifaceNames(c).equals(java.util.Set.of("com.sun.source.doctree.TextTree")), "direct superinterfaces");
    }

    static void testBlackBox() throws Exception {
        javax.tools.JavaCompiler c = javax.tools.ToolProvider.getSystemJavaCompiler();
        javax.tools.DiagnosticCollector<javax.tools.JavaFileObject> diags = new javax.tools.DiagnosticCollector<>();
        javax.tools.StandardJavaFileManager fm = c.getStandardFileManager(diags, java.util.Locale.ROOT, null);
        final String src = "package p; /** @param */ class C {}";
        com.sun.source.util.JavacTask t = (com.sun.source.util.JavacTask) c.getTask(null, fm, diags, java.util.List.of("-proc:none"), null,
                java.util.List.of(new javax.tools.SimpleJavaFileObject(java.net.URI.create("string:///C.java"), javax.tools.JavaFileObject.Kind.SOURCE) {
                    public CharSequence getCharContent(boolean ignore) { return src; }
                }));
        com.sun.source.tree.CompilationUnitTree cu = t.parse().iterator().next();
        t.analyze();
        javax.lang.model.element.Element el = t.getElements().getTypeElement("p.C");
        com.sun.source.doctree.DocCommentTree dc = com.sun.source.util.DocTrees.instance(t).getDocCommentTree(el);
        check(dc != null, "doc comment parsed");

        final java.util.List<com.sun.source.doctree.ErroneousTree> errs = new java.util.ArrayList<>();
        new com.sun.source.util.DocTreeScanner<Void,Void>() {
            @Override public Void visitErroneous(com.sun.source.doctree.ErroneousTree node, Void v) {
                errs.add(node);
                return super.visitErroneous(node, v);
            }
        }.scan(dc, null);
        check(!errs.isEmpty(), "com.sun.source.doctree.ErroneousTree present in parsed doc");
        com.sun.source.doctree.ErroneousTree e = errs.get(0);
        check(e.getKind() == com.sun.source.doctree.DocTree.Kind.ERRONEOUS, "kind == ERRONEOUS");

        javax.tools.Diagnostic<javax.tools.JavaFileObject> d = e.getDiagnostic();
        check(d != null, "getDiagnostic() non-null");
        check(d.getMessage(java.util.Locale.ROOT) != null, "getDiagnostic() message non-null");

        final boolean[] ok = new boolean[1];
        e.accept(new com.sun.source.util.SimpleDocTreeVisitor<Void,Void>() {
            @Override public Void visitErroneous(com.sun.source.doctree.ErroneousTree node, Void v) { ok[0]=true; return null; }
        }, null);
        check(ok[0], "accept dispatches");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
