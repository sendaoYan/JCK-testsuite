/*
 * @test
 * @summary com.sun.source.doctree.InlineTagTree conformance: white-box structure + black-box behavior.
 * @run main InlineTagTreeTest
 */

import java.lang.reflect.*;

public class InlineTagTreeTest {
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
        Class<?> c = Class.forName("com.sun.source.doctree.InlineTagTree");
        check(c != null, "com.sun.source.doctree.InlineTagTree loads");
        check(c.getName().equals("com.sun.source.doctree.InlineTagTree"), "binary name is com.sun.source.doctree.InlineTagTree");
        check(Modifier.isPublic(c.getModifiers()), "com.sun.source.doctree.InlineTagTree is public");
        check(c.isInterface(), "com.sun.source.doctree.InlineTagTree is an interface");
        check(ifaceNames(c).equals(java.util.Set.of("com.sun.source.doctree.DocTree")), "direct superinterfaces");
    }

    static void testBlackBox() throws Exception {
        javax.tools.JavaCompiler c = javax.tools.ToolProvider.getSystemJavaCompiler();
        javax.tools.DiagnosticCollector<javax.tools.JavaFileObject> diags = new javax.tools.DiagnosticCollector<>();
        javax.tools.StandardJavaFileManager fm = c.getStandardFileManager(diags, java.util.Locale.ROOT, null);
        com.sun.source.util.JavacTask t = (com.sun.source.util.JavacTask) c.getTask(null, fm, diags, java.util.List.of("-proc:none"), null, null);
        com.sun.source.util.DocTreeFactory f = com.sun.source.util.DocTrees.instance(t).getDocTreeFactory();
        javax.lang.model.element.Name nm = t.getElements().getName("x");
        com.sun.source.doctree.LiteralTree node = f.newCodeTree(f.newTextTree("int x=1;"));
        check(node instanceof com.sun.source.doctree.InlineTagTree, "instanceof InlineTagTree");
        check(node.getKind() == com.sun.source.doctree.DocTree.Kind.CODE, "getKind");
        check(node.getTagName().equals("code"), "getTagName");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
