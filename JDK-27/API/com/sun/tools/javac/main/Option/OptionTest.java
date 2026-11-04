/*
 * @test
 * @summary com.sun.tools.javac.main.Option conformance: internal enum reflection.
 * @modules jdk.compiler/com.sun.tools.javac.main:+open
 * @run main OptionTest
 */
public class OptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.main.Option", false, OptionTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.main.Option"), "name");
        check(c.isEnum(), "enum");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "interfaces count");
        java.util.Set<String> ec = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant()) ec.add(f.getName());
        check(ec.size() == 85, "enum constants count");
        check(ec.contains("A"), "enum constant A");
        check(ec.contains("ADD_EXPORTS"), "enum constant ADD_EXPORTS");
        check(ec.contains("ADD_MODULES"), "enum constant ADD_MODULES");
        check(ec.contains("ADD_OPENS"), "enum constant ADD_OPENS");
        check(ec.contains("ADD_READS"), "enum constant ADD_READS");
        check(ec.contains("AT"), "enum constant AT");
        check(ec.contains("BOOT_CLASS_PATH"), "enum constant BOOT_CLASS_PATH");
        check(ec.contains("CLASS_PATH"), "enum constant CLASS_PATH");
        check(ec.contains("D"), "enum constant D");
        check(ec.contains("DEBUG"), "enum constant DEBUG");
        check(ec.contains("DEFAULT_MODULE_FOR_CREATED_FILES"), "enum constant DEFAULT_MODULE_FOR_CREATED_FILES");
        check(ec.contains("DEPRECATION"), "enum constant DEPRECATION");
        check(ec.contains("DIAGS"), "enum constant DIAGS");
        check(ec.contains("DISABLE_LINE_DOC_COMMENTS"), "enum constant DISABLE_LINE_DOC_COMMENTS");
        check(ec.contains("DJAVA_ENDORSED_DIRS"), "enum constant DJAVA_ENDORSED_DIRS");
        check(ec.contains("DJAVA_EXT_DIRS"), "enum constant DJAVA_EXT_DIRS");
        check(ec.contains("DOE"), "enum constant DOE");
        check(ec.contains("ENCODING"), "enum constant ENCODING");
        check(ec.contains("ENDORSEDDIRS"), "enum constant ENDORSEDDIRS");
        check(ec.contains("EXTDIRS"), "enum constant EXTDIRS");
        check(ec.contains("FULLVERSION"), "enum constant FULLVERSION");
        check(ec.contains("G"), "enum constant G");
        check(ec.contains("G_CUSTOM"), "enum constant G_CUSTOM");
        check(ec.contains("G_NONE"), "enum constant G_NONE");
        check(ec.contains("H"), "enum constant H");
        check(ec.contains("HELP"), "enum constant HELP");
        check(ec.contains("HELP_LINT"), "enum constant HELP_LINT");
        check(ec.contains("IMPLICIT"), "enum constant IMPLICIT");
        check(ec.contains("INHERIT_RUNTIME_ENVIRONMENT"), "enum constant INHERIT_RUNTIME_ENVIRONMENT");
        check(ec.contains("J"), "enum constant J");
        check(ec.contains("LIMIT_MODULES"), "enum constant LIMIT_MODULES");
        check(ec.contains("MODULE"), "enum constant MODULE");
        check(ec.contains("MODULE_PATH"), "enum constant MODULE_PATH");
        check(ec.contains("MODULE_SOURCE_PATH"), "enum constant MODULE_SOURCE_PATH");
        check(ec.contains("MODULE_VERSION"), "enum constant MODULE_VERSION");
        check(ec.contains("MOREINFO"), "enum constant MOREINFO");
        check(ec.contains("MULTIRELEASE"), "enum constant MULTIRELEASE");
        check(ec.contains("NOWARN"), "enum constant NOWARN");
        check(ec.contains("O"), "enum constant O");
        check(ec.contains("PARAMETERS"), "enum constant PARAMETERS");
        check(ec.contains("PATCH_MODULE"), "enum constant PATCH_MODULE");
        check(ec.contains("PLUGIN"), "enum constant PLUGIN");
        check(ec.contains("PREVIEW"), "enum constant PREVIEW");
        check(ec.contains("PRINTSOURCE"), "enum constant PRINTSOURCE");
        check(ec.contains("PROC"), "enum constant PROC");
        check(ec.contains("PROCESSOR"), "enum constant PROCESSOR");
        check(ec.contains("PROCESSOR_MODULE_PATH"), "enum constant PROCESSOR_MODULE_PATH");
        check(ec.contains("PROCESSOR_PATH"), "enum constant PROCESSOR_PATH");
        check(ec.contains("PROFILE"), "enum constant PROFILE");
        check(ec.contains("PROMPT"), "enum constant PROMPT");
        check(ec.contains("RELEASE"), "enum constant RELEASE");
        check(ec.contains("S"), "enum constant S");
        check(ec.contains("SHOULDSTOP"), "enum constant SHOULDSTOP");
        check(ec.contains("SOURCE"), "enum constant SOURCE");
        check(ec.contains("SOURCEFILE"), "enum constant SOURCEFILE");
        check(ec.contains("SOURCE_PATH"), "enum constant SOURCE_PATH");
        check(ec.contains("SYSTEM"), "enum constant SYSTEM");
        check(ec.contains("TARGET"), "enum constant TARGET");
        check(ec.contains("UPGRADE_MODULE_PATH"), "enum constant UPGRADE_MODULE_PATH");
        check(ec.contains("VERBOSE"), "enum constant VERBOSE");
        check(ec.contains("VERSION"), "enum constant VERSION");
        check(ec.contains("WARNUNCHECKED"), "enum constant WARNUNCHECKED");
        check(ec.contains("WERROR"), "enum constant WERROR");
        check(ec.contains("WERROR_CUSTOM"), "enum constant WERROR_CUSTOM");
        check(ec.contains("X"), "enum constant X");
        check(ec.contains("XBOOTCLASSPATH"), "enum constant XBOOTCLASSPATH");
        check(ec.contains("XBOOTCLASSPATH_APPEND"), "enum constant XBOOTCLASSPATH_APPEND");
        check(ec.contains("XBOOTCLASSPATH_PREPEND"), "enum constant XBOOTCLASSPATH_PREPEND");
        check(ec.contains("XD"), "enum constant XD");
        check(ec.contains("XDIAGS"), "enum constant XDIAGS");
        check(ec.contains("XDOCLINT"), "enum constant XDOCLINT");
        check(ec.contains("XDOCLINT_CUSTOM"), "enum constant XDOCLINT_CUSTOM");
        check(ec.contains("XDOCLINT_PACKAGE"), "enum constant XDOCLINT_PACKAGE");
        check(ec.contains("XJCOV"), "enum constant XJCOV");
        check(ec.contains("XLINT"), "enum constant XLINT");
        check(ec.contains("XLINT_CUSTOM"), "enum constant XLINT_CUSTOM");
        check(ec.contains("XMAXERRS"), "enum constant XMAXERRS");
        check(ec.contains("XMAXWARNS"), "enum constant XMAXWARNS");
        check(ec.contains("XPKGINFO"), "enum constant XPKGINFO");
        check(ec.contains("XPREFER"), "enum constant XPREFER");
        check(ec.contains("XPRINT"), "enum constant XPRINT");
        check(ec.contains("XPRINTPROCESSORINFO"), "enum constant XPRINTPROCESSORINFO");
        check(ec.contains("XPRINTROUNDS"), "enum constant XPRINTROUNDS");
        check(ec.contains("XSTDOUT"), "enum constant XSTDOUT");
        check(ec.contains("XXUSERPATHSFIRST"), "enum constant XXUSERPATHSFIRST");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
