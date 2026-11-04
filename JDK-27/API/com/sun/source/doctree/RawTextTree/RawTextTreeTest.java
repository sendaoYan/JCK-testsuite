/*
 * @test
 * @summary com.sun.source.doctree.RawTextTree conformance: white-box structure + black-box behavior.
 * @run main RawTextTreeTest
 */

import java.lang.reflect.*;

public class RawTextTreeTest {
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
        Class<?> c = Class.forName("com.sun.source.doctree.RawTextTree");
        check(c != null, "com.sun.source.doctree.RawTextTree loads");
        check(c.getName().equals("com.sun.source.doctree.RawTextTree"), "binary name is com.sun.source.doctree.RawTextTree");
        check(Modifier.isPublic(c.getModifiers()), "com.sun.source.doctree.RawTextTree is public");
        check(c.isInterface(), "com.sun.source.doctree.RawTextTree is an interface");
        check(ifaceNames(c).equals(java.util.Set.of("com.sun.source.doctree.DocTree")), "direct superinterfaces");
    }

    static void testBlackBox() throws Exception {
        javax.tools.JavaCompiler c = javax.tools.ToolProvider.getSystemJavaCompiler();
        javax.tools.DiagnosticCollector<javax.tools.JavaFileObject> diags = new javax.tools.DiagnosticCollector<>();
        javax.tools.StandardJavaFileManager fm = c.getStandardFileManager(diags, java.util.Locale.ROOT, null);
        com.sun.source.util.JavacTask t = (com.sun.source.util.JavacTask) c.getTask(null, fm, diags, java.util.List.of("-proc:none"), null, null);
        com.sun.source.util.DocTreeFactory f = com.sun.source.util.DocTrees.instance(t).getDocTreeFactory();
        javax.lang.model.element.Name nm = t.getElements().getName("x");
        com.sun.source.doctree.RawTextTree node = f.newRawTextTree(com.sun.source.doctree.DocTree.Kind.MARKDOWN, "raw");
        com.sun.source.doctree.RawTextTree n = node;
        check(n.getKind() == com.sun.source.doctree.DocTree.Kind.MARKDOWN, "kind");
        check(n.getContent().equals("raw"), "getContent");
        final boolean[] ok = new boolean[1];
        n.accept(new com.sun.source.util.SimpleDocTreeVisitor<Void,Void>() {
            @Override public Void visitRawText(com.sun.source.doctree.RawTextTree node, Void v) { ok[0]=true; return null; }
        }, null);
        check(ok[0], "accept dispatches");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
