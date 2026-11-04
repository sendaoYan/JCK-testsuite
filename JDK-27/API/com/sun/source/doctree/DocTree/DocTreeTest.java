/*
 * @test
 * @summary com.sun.source.doctree.DocTree conformance: white-box structure + black-box behavior.
 * @run main DocTreeTest
 */

import java.lang.reflect.*;

public class DocTreeTest {
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
        Class<?> c = Class.forName("com.sun.source.doctree.DocTree");
        check(c != null, "com.sun.source.doctree.DocTree loads");
        check(c.getName().equals("com.sun.source.doctree.DocTree"), "binary name is com.sun.source.doctree.DocTree");
        check(Modifier.isPublic(c.getModifiers()), "com.sun.source.doctree.DocTree is public");
        check(c.isInterface(), "com.sun.source.doctree.DocTree is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "getKind"), "declares getKind()");
        check(declares(c, "accept"), "declares accept()");
    }

    static void testBlackBox() throws Exception {
        javax.tools.JavaCompiler c = javax.tools.ToolProvider.getSystemJavaCompiler();
        javax.tools.DiagnosticCollector<javax.tools.JavaFileObject> diags = new javax.tools.DiagnosticCollector<>();
        javax.tools.StandardJavaFileManager fm = c.getStandardFileManager(diags, java.util.Locale.ROOT, null);
        com.sun.source.util.JavacTask t = (com.sun.source.util.JavacTask) c.getTask(null, fm, diags, java.util.List.of("-proc:none"), null, null);
        com.sun.source.util.DocTreeFactory f = com.sun.source.util.DocTrees.instance(t).getDocTreeFactory();
        javax.lang.model.element.Name nm = t.getElements().getName("x");
        com.sun.source.doctree.TextTree node = f.newTextTree("hello");
        check(node instanceof com.sun.source.doctree.DocTree, "instanceof DocTree");
        check(node.getKind() == com.sun.source.doctree.DocTree.Kind.TEXT, "getKind");
        final boolean[] ok = new boolean[1];
        node.accept(new com.sun.source.util.SimpleDocTreeVisitor<Void,Void>() {
            @Override public Void visitText(com.sun.source.doctree.TextTree n, Void v) { ok[0]=true; return null; }
        }, null);
        check(ok[0], "accept dispatches");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
