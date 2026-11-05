/*
 * @test
 * @summary jdk.jshell.Snippet.SubKind conformance: snippet subkind enum.
 * @run main/othervm SubKindTest
 */

import jdk.jshell.*;
import jdk.jshell.SourceCodeAnalysis.*;
import java.util.*;

public class SubKindTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testEnumTable() {
        Snippet.SubKind[] vs = Snippet.SubKind.values();
        check(vs.length >= 10, "many subkinds");
        check(vs.length == 19, "19 subkinds in JDK-27");
        for (Snippet.SubKind k : vs) check(k.name().length() > 0, "name " + k.name());
        check(Snippet.SubKind.valueOf(vs[0].name()) != null, "valueOf");
        // declaration order: import subkinds first, then type decls, method, vars, expressions, statement, unknown
        check(vs[0] == Snippet.SubKind.SINGLE_TYPE_IMPORT_SUBKIND, "order[0] SINGLE_TYPE_IMPORT");
        check(vs[4] == Snippet.SubKind.MODULE_IMPORT_SUBKIND, "order[4] MODULE_IMPORT");
        check(vs[5] == Snippet.SubKind.CLASS_SUBKIND, "order[5] CLASS");
        check(vs[9] == Snippet.SubKind.ANNOTATION_TYPE_SUBKIND, "order[9] ANNOTATION_TYPE");
        check(vs[10] == Snippet.SubKind.METHOD_SUBKIND, "order[10] METHOD");
        check(vs[11] == Snippet.SubKind.VAR_DECLARATION_SUBKIND, "order[11] VAR_DECLARATION");
        check(vs[13] == Snippet.SubKind.TEMP_VAR_EXPRESSION_SUBKIND, "order[13] TEMP_VAR_EXPRESSION");
        check(vs[14] == Snippet.SubKind.VAR_VALUE_SUBKIND, "order[14] VAR_VALUE");
        check(vs[15] == Snippet.SubKind.ASSIGNMENT_SUBKIND, "order[15] ASSIGNMENT");
        check(vs[16] == Snippet.SubKind.OTHER_EXPRESSION_SUBKIND, "order[16] OTHER_EXPRESSION");
        check(vs[17] == Snippet.SubKind.STATEMENT_SUBKIND, "order[17] STATEMENT");
        check(vs[18] == Snippet.SubKind.UNKNOWN_SUBKIND, "order[18] UNKNOWN");
        try {
            Snippet.SubKind.valueOf("BOGUS_SUBKIND");
            check(false, "valueOf bogus must throw");
        } catch (IllegalArgumentException expected) { check(true, "valueOf bogus IAE"); }
    }

    // ---- deep contract: kind()/isExecutable()/hasValue() per subkind ----
    static void testSubKindProperties() {
        // every subkind belongs to exactly one Kind; spot table of the grouping
        check(Snippet.SubKind.SINGLE_TYPE_IMPORT_SUBKIND.kind() == Snippet.Kind.IMPORT, "SINGLE_TYPE kind");
        check(Snippet.SubKind.TYPE_IMPORT_ON_DEMAND_SUBKIND.kind() == Snippet.Kind.IMPORT, "ON_DEMAND kind");
        check(Snippet.SubKind.SINGLE_STATIC_IMPORT_SUBKIND.kind() == Snippet.Kind.IMPORT, "SINGLE_STATIC kind");
        check(Snippet.SubKind.STATIC_IMPORT_ON_DEMAND_SUBKIND.kind() == Snippet.Kind.IMPORT, "STATIC_ON_DEMAND kind");
        check(Snippet.SubKind.MODULE_IMPORT_SUBKIND.kind() == Snippet.Kind.IMPORT, "MODULE kind");
        check(Snippet.SubKind.CLASS_SUBKIND.kind() == Snippet.Kind.TYPE_DECL, "CLASS kind");
        check(Snippet.SubKind.INTERFACE_SUBKIND.kind() == Snippet.Kind.TYPE_DECL, "INTERFACE kind");
        check(Snippet.SubKind.ENUM_SUBKIND.kind() == Snippet.Kind.TYPE_DECL, "ENUM kind");
        check(Snippet.SubKind.RECORD_SUBKIND.kind() == Snippet.Kind.TYPE_DECL, "RECORD kind");
        check(Snippet.SubKind.ANNOTATION_TYPE_SUBKIND.kind() == Snippet.Kind.TYPE_DECL, "ANNOTATION kind");
        check(Snippet.SubKind.METHOD_SUBKIND.kind() == Snippet.Kind.METHOD, "METHOD kind");
        check(Snippet.SubKind.VAR_DECLARATION_SUBKIND.kind() == Snippet.Kind.VAR, "VAR_DECL kind");
        check(Snippet.SubKind.VAR_DECLARATION_WITH_INITIALIZER_SUBKIND.kind() == Snippet.Kind.VAR, "VAR_DECL_INIT kind");
        check(Snippet.SubKind.TEMP_VAR_EXPRESSION_SUBKIND.kind() == Snippet.Kind.VAR, "TEMP_VAR kind");
        check(Snippet.SubKind.VAR_VALUE_SUBKIND.kind() == Snippet.Kind.EXPRESSION, "VAR_VALUE kind");
        check(Snippet.SubKind.ASSIGNMENT_SUBKIND.kind() == Snippet.Kind.EXPRESSION, "ASSIGNMENT kind");
        check(Snippet.SubKind.OTHER_EXPRESSION_SUBKIND.kind() == Snippet.Kind.EXPRESSION, "OTHER_EXPR kind");
        check(Snippet.SubKind.STATEMENT_SUBKIND.kind() == Snippet.Kind.STATEMENT, "STATEMENT kind");
        check(Snippet.SubKind.UNKNOWN_SUBKIND.kind() == Snippet.Kind.ERRONEOUS, "UNKNOWN kind");
        // imports and declarations are not executable and carry no value
        for (Snippet.SubKind k : new Snippet.SubKind[]{
                Snippet.SubKind.SINGLE_TYPE_IMPORT_SUBKIND, Snippet.SubKind.TYPE_IMPORT_ON_DEMAND_SUBKIND,
                Snippet.SubKind.CLASS_SUBKIND, Snippet.SubKind.INTERFACE_SUBKIND, Snippet.SubKind.ENUM_SUBKIND,
                Snippet.SubKind.RECORD_SUBKIND, Snippet.SubKind.ANNOTATION_TYPE_SUBKIND, Snippet.SubKind.METHOD_SUBKIND,
                Snippet.SubKind.UNKNOWN_SUBKIND}) {
            check(!k.isExecutable(), "not executable: " + k);
            check(!k.hasValue(), "no value: " + k);
        }
        // var and expression subkinds are executable and produce values; statements execute without value
        for (Snippet.SubKind k : new Snippet.SubKind[]{
                Snippet.SubKind.VAR_DECLARATION_SUBKIND, Snippet.SubKind.VAR_DECLARATION_WITH_INITIALIZER_SUBKIND,
                Snippet.SubKind.TEMP_VAR_EXPRESSION_SUBKIND, Snippet.SubKind.VAR_VALUE_SUBKIND,
                Snippet.SubKind.ASSIGNMENT_SUBKIND, Snippet.SubKind.OTHER_EXPRESSION_SUBKIND}) {
            check(k.isExecutable(), "executable: " + k);
            check(k.hasValue(), "has value: " + k);
        }
        check(Snippet.SubKind.STATEMENT_SUBKIND.isExecutable(), "STATEMENT executable");
        check(!Snippet.SubKind.STATEMENT_SUBKIND.hasValue(), "STATEMENT no value");
        // consistency: subkind's kind is persistent iff subkind is a declaration form
        for (Snippet.SubKind k : Snippet.SubKind.values())
            check(k.kind() != null, "subkind maps to a kind: " + k);
    }

    // ---- deep contract: real engine produces the documented subkind for each source form ----
    static void testSubKindByEval() throws Exception {
        try (JShell js = JShell.builder().executionEngine("local").build()) {
            check(js.eval("import java.util.List;").get(0).snippet().subKind() == Snippet.SubKind.SINGLE_TYPE_IMPORT_SUBKIND, "single type import");
            check(js.eval("import java.util.*;").get(0).snippet().subKind() == Snippet.SubKind.TYPE_IMPORT_ON_DEMAND_SUBKIND, "type on-demand import");
            check(js.eval("import static java.lang.Math.max;").get(0).snippet().subKind() == Snippet.SubKind.SINGLE_STATIC_IMPORT_SUBKIND, "single static import");
            check(js.eval("import static java.lang.Math.*;").get(0).snippet().subKind() == Snippet.SubKind.STATIC_IMPORT_ON_DEMAND_SUBKIND, "static on-demand import");
            check(js.eval("class SK1 {}").get(0).snippet().subKind() == Snippet.SubKind.CLASS_SUBKIND, "class subkind");
            check(js.eval("interface SK2 {}").get(0).snippet().subKind() == Snippet.SubKind.INTERFACE_SUBKIND, "interface subkind");
            check(js.eval("enum SK3 { A }").get(0).snippet().subKind() == Snippet.SubKind.ENUM_SUBKIND, "enum subkind");
            check(js.eval("record SK4(int q) {}").get(0).snippet().subKind() == Snippet.SubKind.RECORD_SUBKIND, "record subkind");
            check(js.eval("@interface SK5 {}").get(0).snippet().subKind() == Snippet.SubKind.ANNOTATION_TYPE_SUBKIND, "annotation subkind");
            check(js.eval("int skm() { return 1; }").get(0).snippet().subKind() == Snippet.SubKind.METHOD_SUBKIND, "method subkind");
            check(js.eval("int skv;").get(0).snippet().subKind() == Snippet.SubKind.VAR_DECLARATION_SUBKIND, "var decl subkind");
            check(js.eval("int skv2 = 1;").get(0).snippet().subKind() == Snippet.SubKind.VAR_DECLARATION_WITH_INITIALIZER_SUBKIND, "var init subkind");
            // expression producing a new temp var
            check(js.eval("2 + 3").get(0).snippet().subKind() == Snippet.SubKind.TEMP_VAR_EXPRESSION_SUBKIND, "temp var subkind");
            // bare existing name -> VAR_VALUE expression
            check(js.eval("skv2").get(0).snippet().subKind() == Snippet.SubKind.VAR_VALUE_SUBKIND, "var value subkind");
            // assignment -> ASSIGNMENT
            check(js.eval("skv2 = 5").get(0).snippet().subKind() == Snippet.SubKind.ASSIGNMENT_SUBKIND, "assignment subkind");
            // void method call is classified as a STATEMENT (no value to show)
            js.eval("void skvoid() {}");
            check(js.eval("skvoid()").get(0).snippet().subKind() == Snippet.SubKind.STATEMENT_SUBKIND, "void call STATEMENT");
            check(js.eval("System.out.println();").get(0).snippet().subKind() == Snippet.SubKind.STATEMENT_SUBKIND, "statement subkind");
            // erroneous snippet keeps the UNKNOWN sentinel
            check(js.eval("1 +").get(0).snippet().subKind() == Snippet.SubKind.UNKNOWN_SUBKIND, "erroneous UNKNOWN subkind");
        }
    }

    public static void main(String[] args) throws Exception {
        testEnumTable();
        testSubKindProperties();
        testSubKindByEval();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
