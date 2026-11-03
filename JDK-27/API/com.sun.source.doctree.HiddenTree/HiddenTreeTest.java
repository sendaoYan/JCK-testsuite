/*
 * @test
 * @summary com.sun.source.doctree.HiddenTree conformance: white-box structure + black-box behavior.
 * @run main HiddenTreeTest
 */

import java.lang.reflect.*;

public class HiddenTreeTest {
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
        Class<?> c = Class.forName("com.sun.source.doctree.HiddenTree");
        check(c != null, "com.sun.source.doctree.HiddenTree loads");
        check(c.getName().equals("com.sun.source.doctree.HiddenTree"), "binary name is com.sun.source.doctree.HiddenTree");
        check(Modifier.isPublic(c.getModifiers()), "com.sun.source.doctree.HiddenTree is public");
        check(c.isInterface(), "com.sun.source.doctree.HiddenTree is an interface");
        check(ifaceNames(c).equals(java.util.Set.of("com.sun.source.doctree.BlockTagTree")), "direct superinterfaces");
    }

    static void testBlackBox() throws Exception {
        javax.tools.JavaCompiler c = javax.tools.ToolProvider.getSystemJavaCompiler();
        javax.tools.DiagnosticCollector<javax.tools.JavaFileObject> diags = new javax.tools.DiagnosticCollector<>();
        javax.tools.StandardJavaFileManager fm = c.getStandardFileManager(diags, java.util.Locale.ROOT, null);
        com.sun.source.util.JavacTask t = (com.sun.source.util.JavacTask) c.getTask(null, fm, diags, java.util.List.of("-proc:none"), null, null);
        com.sun.source.util.DocTreeFactory f = com.sun.source.util.DocTrees.instance(t).getDocTreeFactory();
        javax.lang.model.element.Name nm = t.getElements().getName("x");
        com.sun.source.doctree.HiddenTree node = f.newHiddenTree(java.util.List.of(f.newTextTree("h")));
        com.sun.source.doctree.HiddenTree n = node;
        check(n.getKind() == com.sun.source.doctree.DocTree.Kind.HIDDEN, "kind");
        check(n.getBody().size() == 1, "getBody size");
        final boolean[] ok = new boolean[1];
        n.accept(new com.sun.source.util.SimpleDocTreeVisitor<Void,Void>() {
            @Override public Void visitHidden(com.sun.source.doctree.HiddenTree node, Void v) { ok[0]=true; return null; }
        }, null);
        check(ok[0], "accept dispatches");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
