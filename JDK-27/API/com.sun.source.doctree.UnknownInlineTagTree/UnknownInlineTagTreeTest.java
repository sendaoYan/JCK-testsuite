/*
 * @test
 * @summary com.sun.source.doctree.UnknownInlineTagTree conformance: white-box structure + black-box behavior.
 * @run main UnknownInlineTagTreeTest
 */

import java.lang.reflect.*;

public class UnknownInlineTagTreeTest {
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
        Class<?> c = Class.forName("com.sun.source.doctree.UnknownInlineTagTree");
        check(c != null, "com.sun.source.doctree.UnknownInlineTagTree loads");
        check(c.getName().equals("com.sun.source.doctree.UnknownInlineTagTree"), "binary name is com.sun.source.doctree.UnknownInlineTagTree");
        check(Modifier.isPublic(c.getModifiers()), "com.sun.source.doctree.UnknownInlineTagTree is public");
        check(c.isInterface(), "com.sun.source.doctree.UnknownInlineTagTree is an interface");
        check(ifaceNames(c).equals(java.util.Set.of("com.sun.source.doctree.InlineTagTree")), "direct superinterfaces");
    }

    static void testBlackBox() throws Exception {
        javax.tools.JavaCompiler c = javax.tools.ToolProvider.getSystemJavaCompiler();
        javax.tools.DiagnosticCollector<javax.tools.JavaFileObject> diags = new javax.tools.DiagnosticCollector<>();
        javax.tools.StandardJavaFileManager fm = c.getStandardFileManager(diags, java.util.Locale.ROOT, null);
        com.sun.source.util.JavacTask t = (com.sun.source.util.JavacTask) c.getTask(null, fm, diags, java.util.List.of("-proc:none"), null, null);
        com.sun.source.util.DocTreeFactory f = com.sun.source.util.DocTrees.instance(t).getDocTreeFactory();
        javax.lang.model.element.Name nm = t.getElements().getName("x");
        com.sun.source.doctree.UnknownInlineTagTree node = f.newUnknownInlineTagTree(nm, java.util.List.of(f.newTextTree("c")));
        com.sun.source.doctree.UnknownInlineTagTree n = node;
        check(n.getKind() == com.sun.source.doctree.DocTree.Kind.UNKNOWN_INLINE_TAG, "kind");
        check(n.getTagName().equals("x"), "getTagName");
        check(n.getContent().size() == 1, "getContent size");
        final boolean[] ok = new boolean[1];
        n.accept(new com.sun.source.util.SimpleDocTreeVisitor<Void,Void>() {
            @Override public Void visitUnknownInlineTag(com.sun.source.doctree.UnknownInlineTagTree node, Void v) { ok[0]=true; return null; }
        }, null);
        check(ok[0], "accept dispatches");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
