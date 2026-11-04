/*
 * @test
 * @summary com.sun.source.util.TaskListener conformance: white-box structure + black-box behavior.
 * @modules jdk.compiler
 * @run main TaskListenerTest
 */

import java.lang.reflect.*;

public class TaskListenerTest {
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
        Class<?> c = Class.forName("com.sun.source.util.TaskListener");
        check(c != null, "com.sun.source.util.TaskListener loads");
        check(c.getName().equals("com.sun.source.util.TaskListener"), "binary name is com.sun.source.util.TaskListener");
        check(Modifier.isPublic(c.getModifiers()), "com.sun.source.util.TaskListener is public");
        check(c.isInterface(), "com.sun.source.util.TaskListener is an interface");
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
        final int[] started = new int[1];
        final int[] finished = new int[1];
        com.sun.source.util.TaskListener l = new com.sun.source.util.TaskListener() {
            public void started(com.sun.source.util.TaskEvent e) { started[0]++; }
            public void finished(com.sun.source.util.TaskEvent e) { finished[0]++; }
        };

        com.sun.source.util.TaskEvent ev = new com.sun.source.util.TaskEvent(com.sun.source.util.TaskEvent.Kind.PARSE);
        l.started(ev);
        check(started[0] == 1, "started direct call");
        l.finished(ev);
        check(finished[0] == 1, "finished direct call");

        u.t.addTaskListener(l);
        u.t.analyze();
        check(started[0] > 1, "started fired by compiler");
        check(finished[0] > 1, "finished fired by compiler");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
