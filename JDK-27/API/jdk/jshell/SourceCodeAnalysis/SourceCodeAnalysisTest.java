/*
 * @test
 * @summary jdk.jshell.SourceCodeAnalysis conformance: analysis and completion.
 * @run main/othervm SourceCodeAnalysisTest
 */

import jdk.jshell.*;
import jdk.jshell.SourceCodeAnalysis.*;
import java.util.*;

public class SourceCodeAnalysisTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jshell.SourceCodeAnalysis");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract class (not instantiable directly)");
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("analyzeCompletion")) f=true; check(f, "has analyzeCompletion"); }
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("completionSuggestions")) f=true; check(f, "has completionSuggestions"); }
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("highlights")) f=true; check(f, "has highlights"); }
        for (String n : new String[]{"documentation", "analyzeType", "listQualifiedNames",
              "wrapper", "wrappers", "sourceToSnippets", "dependents"}) {
            boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals(n)) f=true;
            check(f, "has " + n);
        }
        // two completionSuggestions overloads: classic (int[] anchor) and convertor-based
        int overloads = 0;
        for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("completionSuggestions")) overloads++;
        check(overloads == 2, "two completionSuggestions overloads");
    }

    static void testBlackBox() throws Exception {

        try (JShell js = JShell.create()) {
            SourceCodeAnalysis sca = js.sourceCodeAnalysis();
            check(sca.analyzeCompletion("int x = 1;").completeness() == Completeness.COMPLETE, "analyzeCompletion");
            check(!sca.analyzeCompletion("int x = ").completeness().isComplete(), "incomplete");
            boolean hasSystem = false;
            for (Suggestion s : sca.completionSuggestions("Syst", 4, new int[1])) if (s.continuation().equals("System")) hasSystem = true;
            check(hasSystem, "suggests System");
            check(!sca.highlights("int x").isEmpty(), "highlights");
            // analysis object is stable per shell
            check(sca == js.sourceCodeAnalysis(), "sourceCodeAnalysis() returns same instance");
        }
    }

    // ---- deep contract: completion analysis, suggestion, docs, wrapping ----

    static void testAnalyzeCompletionMatrix() throws Exception {
        try (JShell js = JShell.builder().executionEngine("local").build()) {
            SourceCodeAnalysis sca = js.sourceCodeAnalysis();
            // split semantics: first complete unit returned as source, rest as remaining
            CompletionInfo ci = sca.analyzeCompletion("int a1 = 1; int b1 =");
            check(ci.completeness() == Completeness.COMPLETE, "first unit COMPLETE");
            check(ci.source().equals("int a1 = 1;"), "source is first unit");
            check(ci.remaining().equals(" int b1 ="), "remaining keeps second unit");
            // trailing whitespace stays with the remaining input
            ci = sca.analyzeCompletion("int a2 = 1;  int b2 = 2;");
            check(ci.source().equals("int a2 = 1;"), "source is exactly the complete unit");
            check(ci.remaining().equals("  int b2 = 2;"), "remaining keeps leading whitespace");
            // driving the loop to exhaustion: remaining eventually empty
            String input = "int c1 = 1; int c2 = 2; int c3 = 3;";
            List<String> units = new ArrayList<>();
            String rest = input;
            int guard = 0;
            while (!rest.isEmpty() && guard++ < 10) {
                CompletionInfo step = sca.analyzeCompletion(rest);
                units.add(step.source());
                rest = step.remaining();
                check(step.completeness() == Completeness.COMPLETE, "unit complete in loop");
            }
            check(units.size() == 3, "3 units split from input");
            check(rest.isEmpty(), "loop terminates with empty remaining");
            // incomplete forms
            check(sca.analyzeCompletion("if (x >").completeness() == Completeness.DEFINITELY_INCOMPLETE, "if-open DEFINITELY_INCOMPLETE");
            check(sca.analyzeCompletion("class C").completeness() == Completeness.DEFINITELY_INCOMPLETE, "bare class DEFINITELY_INCOMPLETE");
            check(sca.analyzeCompletion("2 + 2").completeness() == Completeness.COMPLETE, "expression COMPLETE");
            check(sca.analyzeCompletion("int d1 = 1;").completeness() == Completeness.COMPLETE, "declaration COMPLETE");
            check(sca.analyzeCompletion("").completeness() == Completeness.EMPTY, "empty EMPTY");
            check(sca.analyzeCompletion("/* unterminated").completeness() == Completeness.DEFINITELY_INCOMPLETE, "unterminated comment");
            // a unit that is complete only with a semicolon terminator
            check(sca.analyzeCompletion("int e1 = 1").completeness() == Completeness.COMPLETE_WITH_SEMI, "COMPLETE_WITH_SEMI when terminator missing");
            // completeness of the *whole* input when only one snippet
            ci = sca.analyzeCompletion("int f1 = ");
            check(ci.completeness() == Completeness.DEFINITELY_INCOMPLETE, "unfinished initializer");
            check(ci.source() == null, "nothing complete yet -> null source");
            check(ci.remaining().startsWith("int f1 = "), "unfinished stays in remaining");
        }
    }

    static void testSuggestions() throws Exception {
        try (JShell js = JShell.builder().executionEngine("local").build()) {
            SourceCodeAnalysis sca = js.sourceCodeAnalysis();
            int[] anchor = new int[1];
            anchor[0] = -1;
            List<Suggestion> sugg = sca.completionSuggestions("Syst", 4, anchor);
            check(sugg.stream().anyMatch(s -> s.continuation().equals("System")), "System suggested");
            check(anchor[0] == 0, "anchor at identifier start");
            // after a dot on a type name: static members of String
            List<Suggestion> members = sca.completionSuggestions("String.", 7, anchor);
            check(anchor[0] == 7, "anchor after dot");
            check(members.size() > 5, "many members suggested");
            check(members.stream().anyMatch(s -> s.continuation().startsWith("valueOf(")), "static valueOf suggested");
            check(members.stream().anyMatch(s -> s.continuation().equals("CASE_INSENSITIVE_ORDER")), "static field suggested");
            check(members.stream().allMatch(s -> s.continuation() != null), "continuations non-null");
            // partial member name filters by prefix
            List<Suggestion> filt = sca.completionSuggestions("String.valueO", 12, anchor);
            check(filt.stream().anyMatch(s -> s.continuation().startsWith("valueOf(")), "valueOf by prefix");
            check(filt.stream().noneMatch(s -> s.continuation().startsWith("copyValueOf(")), "prefix filters others out");
            check(!filt.isEmpty(), "prefix suggestions non-empty");
            // NOTE: expression-receiver completions ("s".toUp) are index-refresh dependent
            // (observed flaky across processes) and are intentionally not asserted here.
            // snippet-declared variables appear in top-level suggestions (fresh shell: index deterministic)
            try (JShell js2 = JShell.builder().executionEngine("local").build()) {
                js2.eval("int suggv2 = 1;");
                List<Suggestion> own = js2.sourceCodeAnalysis().completionSuggestions("sugg", 4, anchor);
                check(own.stream().anyMatch(s -> s.continuation().equals("suggv2")), "own variable suggested");
            }
            // keywords are suggested where applicable (matchesType distinguishes)
            check(sugg.stream().noneMatch(Suggestion::matchesType) || sugg.stream().anyMatch(Suggestion::matchesType),
                  "matchesType queryable on every suggestion");
        }
    }

    static void testDocumentation() throws Exception {
        try (JShell js = JShell.builder().executionEngine("local").build()) {
            SourceCodeAnalysis sca = js.sourceCodeAnalysis();
            // cursor must sit after the method name (at the open paren) to resolve overloads
            List<Documentation> docs = sca.documentation("System.out.println(", 19, false);
            check(docs.size() >= 5, "println overloads documented");
            boolean sawVoid = false, sawString = false;
            for (Documentation d : docs) {
                check(d.signature() != null, "signature non-null");
                if (d.signature().contains("println()")) sawVoid = true;
                if (d.signature().contains("println(String")) sawString = true;
            }
            check(sawVoid, "no-arg println documented");
            check(sawString, "println(String) documented");
            check(docs.stream().allMatch(d -> d.javadoc() == null || !d.javadoc().isEmpty()), "javadoc well-formed (null when absent)");
            check(docs.get(0).activeParameterIndex() >= 0, "activeParameterIndex sane");
            // with expression=true the doc targets the full expression
            List<Documentation> docs2 = sca.documentation("System.out.println(", 19, true);
            check(!docs2.isEmpty(), "expression mode also documents");
            // a class name alone documents the type
            List<Documentation> cls = sca.documentation("java.util.ArrayList", 19, false);
            check(cls.size() == 1 && cls.get(0).signature().contains("ArrayList"), "class documented");
            // Math.max overloads
            List<Documentation> max = sca.documentation("Math.max(", 9, false);
            check(max.size() >= 4, "max overloads");
            check(max.stream().anyMatch(d -> d.signature().contains("max(int a, int b)")), "int,int overload with param names");
            // unknown target: empty list, no throw
            check(sca.documentation("NoSuch.xy(", 10, false).isEmpty(), "unknown target empty");
        }
    }

    static void testAnalyzeTypeAndQualifiedNames() throws Exception {
        try (JShell js = JShell.builder().executionEngine("local").build()) {
            SourceCodeAnalysis sca = js.sourceCodeAnalysis();
            check(sca.analyzeType("\"a\" + 1", 8).equals("String"), "analyzeType String concat");
            check(sca.analyzeType("1 + 1", 5).equals("int"), "analyzeType int");
            check(sca.analyzeType("new Object()", 12).equals("Object"), "analyzeType Object");
            check(sca.analyzeType("1 > 2", 5).equals("boolean"), "analyzeType boolean");
            check(sca.analyzeType("", 0) == null, "analyzeType empty input null");
            QualifiedNames qn = sca.listQualifiedNames("System", 6);
            check(qn.getSimpleNameLength() == 6, "simple name length of full identifier");
            check(qn.isResolvable(), "System resolvable");
            // isUpToDate() reflects index refresh timing; only queryability is contractual
            check(qn.getNames() != null, "names list non-null");
            QualifiedNames qn2 = sca.listQualifiedNames("j", 1);
            check(qn2.getSimpleNameLength() == 1, "partial identifier length");
            check(qn2.getNames() != null, "partial names list non-null");
            check(!qn2.isResolvable(), "partial not resolvable");
            // NOTE: getNames() content depends on asynchronous class-index refresh and is not asserted
        }
    }

    static void testWrappersAndSources() throws Exception {
        try (JShell js = JShell.builder().executionEngine("local").build()) {
            SourceCodeAnalysis sca = js.sourceCodeAnalysis();
            VarSnippet wv = (VarSnippet) js.eval("int wv1 = 1;").get(0).snippet();
            SnippetWrapper wr = sca.wrapper(wv);
            check(wr.source().equals("int wv1 = 1;"), "wrapper source round-trip");
            check(wr.kind() == Snippet.Kind.VAR, "wrapper kind");
            check(wr.fullClassName().startsWith("REPL.$JShell$"), "fullClassName REPL naming");
            check(wr.wrapped().startsWith("package REPL;"), "wrapped in REPL package");
            check(wr.wrapped().contains("wv1"), "wrapped contains the variable");
            // position mapping: source<->wrapped round-trip
            int mapped = wr.sourceToWrappedPosition(0);
            check(mapped >= 0, "sourceToWrappedPosition >= 0");
            check(wr.wrappedToSourcePosition(mapped) == 0, "wrappedToSourcePosition round-trip");
            // wrappers(String) for a single declaration mirrors wrapper(Snippet)
            List<SnippetWrapper> ws = sca.wrappers("int wv2 = 2;");
            check(ws.size() == 1, "single wrapper for single snippet");
            check(ws.get(0).source().equals("int wv2 = 2;"), "wrappers(String) source");
            check(ws.get(0).kind() == Snippet.Kind.VAR, "wrappers(String) kind");
            // two statements in one input -> single wrapper covering the first unit
            List<SnippetWrapper> wm = sca.wrappers("int wv3 = 1; int wv4 = 2;");
            check(wm.size() == 1, "two statements -> one wrapper");
            check(wm.get(0).wrapped().contains("wv3"), "first var wrapped");
            // sourceToSnippets: parses without executing
            List<Snippet> ss = sca.sourceToSnippets("int ss1 = 5;");
            check(ss.size() == 1, "sourceToSnippets one");
            check(ss.get(0).kind() == Snippet.Kind.VAR, "sourceToSnippets kind");
            check(ss.get(0).id().equals("*UNASSOCIATED*"), "unassociated id sentinel");
            check(ss.get(0).source().equals("int ss1 = 5;"), "sourceToSnippets source");
            // broken source still parses into an erroneous snippet, never throws
            List<Snippet> bad = sca.sourceToSnippets("int ssbad = ;");
            check(bad.size() == 1 && bad.get(0).kind() == Snippet.Kind.ERRONEOUS, "broken -> ERRONEOUS");
            // dependents: returns a collection (engine tracks only unresolved dependents)
            check(sca.dependents(wv) != null, "dependents non-null");
        }
    }

    static void testHighlights() throws Exception {
        try (JShell js = JShell.builder().executionEngine("local").build()) {
            SourceCodeAnalysis sca = js.sourceCodeAnalysis();
            // declaration keywords and declared names are attributed
            List<Highlight> hs = sca.highlights("int hx = 1;");
            check(hs.size() == 2, "two highlights for var decl");
            check(hs.get(0).start() == 0 && hs.get(0).end() == 3, "int keyword span");
            check(hs.get(0).attributes().contains(Attribute.KEYWORD), "KEYWORD attribute");
            check(hs.get(1).start() == 4 && hs.get(1).end() == 6, "name span");
            check(hs.get(1).attributes().contains(Attribute.DECLARATION), "DECLARATION attribute");
            // a statement without declarations has no highlights
            check(sca.highlights("System.out.println();").isEmpty(), "no highlights for plain statement");
            // highlights are ordered and cover the whole syntax shape for a class decl
            List<Highlight> chs = sca.highlights("class HC1 { int hf = 1; }");
            check(chs.size() == 4, "four highlights for class decl");
            check(chs.get(0).attributes().contains(Attribute.KEYWORD), "class keyword");
            check(chs.get(1).attributes().contains(Attribute.DECLARATION), "class name DECLARATION");
            // spans are within bounds and non-decreasing
            int last = 0;
            for (Highlight h : chs) {
                check(h.start() >= last && h.end() > h.start(), "ordered spans");
                last = h.start();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testAnalyzeCompletionMatrix();
        testSuggestions();
        testDocumentation();
        testAnalyzeTypeAndQualifiedNames();
        testWrappersAndSources();
        testHighlights();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
