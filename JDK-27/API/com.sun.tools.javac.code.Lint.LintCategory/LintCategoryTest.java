/*
 * @test
 * @summary com.sun.tools.javac.code.Lint.LintCategory conformance: internal enum reflection.
 * @modules jdk.compiler/com.sun.tools.javac.code:+open
 * @run main LintCategoryTest
 */
public class LintCategoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.code.Lint$LintCategory", false, LintCategoryTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.code.Lint$LintCategory"), "name");
        check(c.isEnum(), "enum");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "interfaces count");
        java.util.Set<String> ec = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant()) ec.add(f.getName());
        check(ec.size() == 37, "enum constants count");
        check(ec.contains("AUXILIARYCLASS"), "enum constant AUXILIARYCLASS");
        check(ec.contains("CAST"), "enum constant CAST");
        check(ec.contains("CLASSFILE"), "enum constant CLASSFILE");
        check(ec.contains("DANGLING_DOC_COMMENTS"), "enum constant DANGLING_DOC_COMMENTS");
        check(ec.contains("DEPRECATION"), "enum constant DEPRECATION");
        check(ec.contains("DEP_ANN"), "enum constant DEP_ANN");
        check(ec.contains("DIVZERO"), "enum constant DIVZERO");
        check(ec.contains("EMPTY"), "enum constant EMPTY");
        check(ec.contains("EXPORTS"), "enum constant EXPORTS");
        check(ec.contains("FALLTHROUGH"), "enum constant FALLTHROUGH");
        check(ec.contains("FINALLY"), "enum constant FINALLY");
        check(ec.contains("IDENTITY"), "enum constant IDENTITY");
        check(ec.contains("INCUBATING"), "enum constant INCUBATING");
        check(ec.contains("LOSSY_CONVERSIONS"), "enum constant LOSSY_CONVERSIONS");
        check(ec.contains("MISSING_EXPLICIT_CTOR"), "enum constant MISSING_EXPLICIT_CTOR");
        check(ec.contains("MODULE"), "enum constant MODULE");
        check(ec.contains("OPENS"), "enum constant OPENS");
        check(ec.contains("OPTIONS"), "enum constant OPTIONS");
        check(ec.contains("OUTPUT_FILE_CLASH"), "enum constant OUTPUT_FILE_CLASH");
        check(ec.contains("OVERLOADS"), "enum constant OVERLOADS");
        check(ec.contains("OVERRIDES"), "enum constant OVERRIDES");
        check(ec.contains("PATH"), "enum constant PATH");
        check(ec.contains("PREVIEW"), "enum constant PREVIEW");
        check(ec.contains("PROCESSING"), "enum constant PROCESSING");
        check(ec.contains("RAWTYPES"), "enum constant RAWTYPES");
        check(ec.contains("REMOVAL"), "enum constant REMOVAL");
        check(ec.contains("REQUIRES_AUTOMATIC"), "enum constant REQUIRES_AUTOMATIC");
        check(ec.contains("REQUIRES_TRANSITIVE_AUTOMATIC"), "enum constant REQUIRES_TRANSITIVE_AUTOMATIC");
        check(ec.contains("RESTRICTED"), "enum constant RESTRICTED");
        check(ec.contains("SERIAL"), "enum constant SERIAL");
        check(ec.contains("STATIC"), "enum constant STATIC");
        check(ec.contains("STRICTFP"), "enum constant STRICTFP");
        check(ec.contains("TEXT_BLOCKS"), "enum constant TEXT_BLOCKS");
        check(ec.contains("THIS_ESCAPE"), "enum constant THIS_ESCAPE");
        check(ec.contains("TRY"), "enum constant TRY");
        check(ec.contains("UNCHECKED"), "enum constant UNCHECKED");
        check(ec.contains("VARARGS"), "enum constant VARARGS");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
