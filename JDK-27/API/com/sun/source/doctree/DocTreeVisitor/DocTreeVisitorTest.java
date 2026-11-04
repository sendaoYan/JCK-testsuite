/*
 * @test
 * @summary com.sun.source.doctree.DocTreeVisitor conformance: white-box structure + black-box behavior.
 * @modules jdk.compiler
 * @run main DocTreeVisitorTest
 */

import java.lang.reflect.*;

public class DocTreeVisitorTest {
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
        Class<?> c = Class.forName("com.sun.source.doctree.DocTreeVisitor");
        check(c != null, "com.sun.source.doctree.DocTreeVisitor loads");
        check(c.getName().equals("com.sun.source.doctree.DocTreeVisitor"), "binary name is com.sun.source.doctree.DocTreeVisitor");
        check(Modifier.isPublic(c.getModifiers()), "com.sun.source.doctree.DocTreeVisitor is public");
        check(c.isInterface(), "com.sun.source.doctree.DocTreeVisitor is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
    }

    static void testBlackBox() throws Exception {
        // Real doc tree accept() dispatch path.
        javax.tools.JavaCompiler c = javax.tools.ToolProvider.getSystemJavaCompiler();
        javax.tools.DiagnosticCollector<javax.tools.JavaFileObject> diags = new javax.tools.DiagnosticCollector<>();
        javax.tools.StandardJavaFileManager fm = c.getStandardFileManager(diags, java.util.Locale.ROOT, null);
        com.sun.source.util.JavacTask t = (com.sun.source.util.JavacTask) c.getTask(null, fm, diags, java.util.List.of("-proc:none"), null, null);
        com.sun.source.util.DocTreeFactory f = com.sun.source.util.DocTrees.instance(t).getDocTreeFactory();
        javax.lang.model.element.Name nm = t.getElements().getName("x");
        com.sun.source.doctree.TextTree txt = f.newTextTree("hello");
        final int[] n = new int[1];
        txt.accept(new com.sun.source.util.SimpleDocTreeVisitor<Void,Void>() {
            @Override public Void visitText(com.sun.source.doctree.TextTree node, Void v) { n[0]++; return null; }
        }, null);
        check(n[0] == 1, "DocTreeVisitor.visitText dispatched via accept");

        // Direct invokeinterface calls to every visit method of the interface.
        com.sun.source.doctree.DocTreeVisitor<String, Void> v = new com.sun.source.doctree.DocTreeVisitor<String, Void>() {
            public String visitAttribute(com.sun.source.doctree.AttributeTree t, Void p) { return "visitAttribute"; }
            public String visitAuthor(com.sun.source.doctree.AuthorTree t, Void p) { return "visitAuthor"; }
            public String visitComment(com.sun.source.doctree.CommentTree t, Void p) { return "visitComment"; }
            public String visitDeprecated(com.sun.source.doctree.DeprecatedTree t, Void p) { return "visitDeprecated"; }
            public String visitDocComment(com.sun.source.doctree.DocCommentTree t, Void p) { return "visitDocComment"; }
            public String visitDocRoot(com.sun.source.doctree.DocRootTree t, Void p) { return "visitDocRoot"; }
            public String visitDocType(com.sun.source.doctree.DocTypeTree t, Void p) { return "visitDocType"; }
            public String visitEndElement(com.sun.source.doctree.EndElementTree t, Void p) { return "visitEndElement"; }
            public String visitEntity(com.sun.source.doctree.EntityTree t, Void p) { return "visitEntity"; }
            public String visitErroneous(com.sun.source.doctree.ErroneousTree t, Void p) { return "visitErroneous"; }
            public String visitEscape(com.sun.source.doctree.EscapeTree t, Void p) { return "visitEscape"; }
            public String visitHidden(com.sun.source.doctree.HiddenTree t, Void p) { return "visitHidden"; }
            public String visitIdentifier(com.sun.source.doctree.IdentifierTree t, Void p) { return "visitIdentifier"; }
            public String visitIndex(com.sun.source.doctree.IndexTree t, Void p) { return "visitIndex"; }
            public String visitInheritDoc(com.sun.source.doctree.InheritDocTree t, Void p) { return "visitInheritDoc"; }
            public String visitLink(com.sun.source.doctree.LinkTree t, Void p) { return "visitLink"; }
            public String visitLiteral(com.sun.source.doctree.LiteralTree t, Void p) { return "visitLiteral"; }
            public String visitOther(com.sun.source.doctree.DocTree t, Void p) { return "visitOther"; }
            public String visitParam(com.sun.source.doctree.ParamTree t, Void p) { return "visitParam"; }
            public String visitProvides(com.sun.source.doctree.ProvidesTree t, Void p) { return "visitProvides"; }
            public String visitRawText(com.sun.source.doctree.RawTextTree t, Void p) { return "visitRawText"; }
            public String visitReference(com.sun.source.doctree.ReferenceTree t, Void p) { return "visitReference"; }
            public String visitReturn(com.sun.source.doctree.ReturnTree t, Void p) { return "visitReturn"; }
            public String visitSee(com.sun.source.doctree.SeeTree t, Void p) { return "visitSee"; }
            public String visitSerial(com.sun.source.doctree.SerialTree t, Void p) { return "visitSerial"; }
            public String visitSerialData(com.sun.source.doctree.SerialDataTree t, Void p) { return "visitSerialData"; }
            public String visitSerialField(com.sun.source.doctree.SerialFieldTree t, Void p) { return "visitSerialField"; }
            public String visitSince(com.sun.source.doctree.SinceTree t, Void p) { return "visitSince"; }
            public String visitSnippet(com.sun.source.doctree.SnippetTree t, Void p) { return "visitSnippet"; }
            public String visitSpec(com.sun.source.doctree.SpecTree t, Void p) { return "visitSpec"; }
            public String visitStartElement(com.sun.source.doctree.StartElementTree t, Void p) { return "visitStartElement"; }
            public String visitSummary(com.sun.source.doctree.SummaryTree t, Void p) { return "visitSummary"; }
            public String visitSystemProperty(com.sun.source.doctree.SystemPropertyTree t, Void p) { return "visitSystemProperty"; }
            public String visitText(com.sun.source.doctree.TextTree t, Void p) { return "visitText"; }
            public String visitThrows(com.sun.source.doctree.ThrowsTree t, Void p) { return "visitThrows"; }
            public String visitUnknownBlockTag(com.sun.source.doctree.UnknownBlockTagTree t, Void p) { return "visitUnknownBlockTag"; }
            public String visitUnknownInlineTag(com.sun.source.doctree.UnknownInlineTagTree t, Void p) { return "visitUnknownInlineTag"; }
            public String visitUses(com.sun.source.doctree.UsesTree t, Void p) { return "visitUses"; }
            public String visitValue(com.sun.source.doctree.ValueTree t, Void p) { return "visitValue"; }
            public String visitVersion(com.sun.source.doctree.VersionTree t, Void p) { return "visitVersion"; }
        };

        check("visitAttribute".equals(v.visitAttribute(null, null)), "visitAttribute direct");
        check("visitAuthor".equals(v.visitAuthor(null, null)), "visitAuthor direct");
        check("visitComment".equals(v.visitComment(null, null)), "visitComment direct");
        check("visitDeprecated".equals(v.visitDeprecated(null, null)), "visitDeprecated direct");
        check("visitDocComment".equals(v.visitDocComment(null, null)), "visitDocComment direct");
        check("visitDocRoot".equals(v.visitDocRoot(null, null)), "visitDocRoot direct");
        check("visitDocType".equals(v.visitDocType(null, null)), "visitDocType direct");
        check("visitEndElement".equals(v.visitEndElement(null, null)), "visitEndElement direct");
        check("visitEntity".equals(v.visitEntity(null, null)), "visitEntity direct");
        check("visitErroneous".equals(v.visitErroneous(null, null)), "visitErroneous direct");
        check("visitEscape".equals(v.visitEscape(null, null)), "visitEscape direct");
        check("visitHidden".equals(v.visitHidden(null, null)), "visitHidden direct");
        check("visitIdentifier".equals(v.visitIdentifier(null, null)), "visitIdentifier direct");
        check("visitIndex".equals(v.visitIndex(null, null)), "visitIndex direct");
        check("visitInheritDoc".equals(v.visitInheritDoc(null, null)), "visitInheritDoc direct");
        check("visitLink".equals(v.visitLink(null, null)), "visitLink direct");
        check("visitLiteral".equals(v.visitLiteral(null, null)), "visitLiteral direct");
        check("visitOther".equals(v.visitOther(null, null)), "visitOther direct");
        check("visitParam".equals(v.visitParam(null, null)), "visitParam direct");
        check("visitProvides".equals(v.visitProvides(null, null)), "visitProvides direct");
        check("visitRawText".equals(v.visitRawText(null, null)), "visitRawText direct");
        check("visitReference".equals(v.visitReference(null, null)), "visitReference direct");
        check("visitReturn".equals(v.visitReturn(null, null)), "visitReturn direct");
        check("visitSee".equals(v.visitSee(null, null)), "visitSee direct");
        check("visitSerial".equals(v.visitSerial(null, null)), "visitSerial direct");
        check("visitSerialData".equals(v.visitSerialData(null, null)), "visitSerialData direct");
        check("visitSerialField".equals(v.visitSerialField(null, null)), "visitSerialField direct");
        check("visitSince".equals(v.visitSince(null, null)), "visitSince direct");
        check("visitSnippet".equals(v.visitSnippet(null, null)), "visitSnippet direct");
        check("visitSpec".equals(v.visitSpec(null, null)), "visitSpec direct");
        check("visitStartElement".equals(v.visitStartElement(null, null)), "visitStartElement direct");
        check("visitSummary".equals(v.visitSummary(null, null)), "visitSummary direct");
        check("visitSystemProperty".equals(v.visitSystemProperty(null, null)), "visitSystemProperty direct");
        check("visitText".equals(v.visitText(null, null)), "visitText direct");
        check("visitThrows".equals(v.visitThrows(null, null)), "visitThrows direct");
        check("visitUnknownBlockTag".equals(v.visitUnknownBlockTag(null, null)), "visitUnknownBlockTag direct");
        check("visitUnknownInlineTag".equals(v.visitUnknownInlineTag(null, null)), "visitUnknownInlineTag direct");
        check("visitUses".equals(v.visitUses(null, null)), "visitUses direct");
        check("visitValue".equals(v.visitValue(null, null)), "visitValue direct");
        check("visitVersion".equals(v.visitVersion(null, null)), "visitVersion direct");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
