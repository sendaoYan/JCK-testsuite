/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.compiler.sym conformance: internal package reflection.
 * @modules java.xml/com.sun.org.apache.xalan.internal.xsltc.compiler:+open
 * @run main symTest
 */
public class symTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.compiler.sym", false, symTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xalan.internal.xsltc.compiler.sym"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        java.util.Set<String> ifs = new java.util.HashSet<>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 0, "interface-count");
        java.util.Set<String> ec = new java.util.HashSet<>();
        java.util.Set<String> fd = new java.util.HashSet<>();
        java.util.Set<String> md = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) {
            if (f.isEnumConstant()) ec.add(f.getName());
            else if (java.lang.reflect.Modifier.isPublic(f.getModifiers()) || java.lang.reflect.Modifier.isProtected(f.getModifiers())) fd.add(f.getName());
        }
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (java.lang.reflect.Modifier.isPublic(m.getModifiers()) || java.lang.reflect.Modifier.isProtected(m.getModifiers())) md.add(m.getName());
        }
        check(ec.size() == 0, "enum-constant-count");
        check(fd.size() == 55, "field-count");
        check(fd.contains("Literal"), "field-Literal");
        check(fd.contains("GE"), "field-GE");
        check(fd.contains("NAMESPACE"), "field-NAMESPACE");
        check(fd.contains("SLASH"), "field-SLASH");
        check(fd.contains("PI"), "field-PI");
        check(fd.contains("COMMENT"), "field-COMMENT");
        check(fd.contains("PIPARAM"), "field-PIPARAM");
        check(fd.contains("FOLLOWINGSIBLING"), "field-FOLLOWINGSIBLING");
        check(fd.contains("ATSIGN"), "field-ATSIGN");
        check(fd.contains("DSLASH"), "field-DSLASH");
        check(fd.contains("LPAREN"), "field-LPAREN");
        check(fd.contains("EXPRESSION"), "field-EXPRESSION");
        check(fd.contains("INT"), "field-INT");
        check(fd.contains("CHILD"), "field-CHILD");
        check(fd.contains("MINUS"), "field-MINUS");
        check(fd.contains("STAR"), "field-STAR");
        check(fd.contains("DESCENDANTORSELF"), "field-DESCENDANTORSELF");
        check(fd.contains("RPAREN"), "field-RPAREN");
        check(fd.contains("AND"), "field-AND");
        check(fd.contains("LT"), "field-LT");
        check(fd.contains("OR"), "field-OR");
        check(fd.contains("COMMA"), "field-COMMA");
        check(fd.contains("DDOT"), "field-DDOT");
        check(fd.contains("DIV"), "field-DIV");
        check(fd.contains("PLUS"), "field-PLUS");
        check(fd.contains("DOT"), "field-DOT");
        check(fd.contains("ID"), "field-ID");
        check(fd.contains("LE"), "field-LE");
        check(fd.contains("QNAME"), "field-QNAME");
        check(fd.contains("DESCENDANT"), "field-DESCENDANT");
        check(fd.contains("PRECEDINGSIBLING"), "field-PRECEDINGSIBLING");
        check(fd.contains("EOF"), "field-EOF");
        check(fd.contains("error"), "field-error");
        check(fd.contains("SELF"), "field-SELF");
        check(fd.contains("VBAR"), "field-VBAR");
        check(fd.contains("MOD"), "field-MOD");
        check(fd.contains("ANCESTORORSELF"), "field-ANCESTORORSELF");
        check(fd.contains("PRECEDING"), "field-PRECEDING");
        check(fd.contains("EQ"), "field-EQ");
        check(fd.contains("PATTERN"), "field-PATTERN");
        check(fd.contains("LBRACK"), "field-LBRACK");
        check(fd.contains("REAL"), "field-REAL");
        check(fd.contains("ANCESTOR"), "field-ANCESTOR");
        check(fd.contains("RBRACK"), "field-RBRACK");
        check(fd.contains("DCOLON"), "field-DCOLON");
        check(fd.contains("DOLLAR"), "field-DOLLAR");
        check(fd.contains("NE"), "field-NE");
        check(fd.contains("PARENT"), "field-PARENT");
        check(fd.contains("FOLLOWING"), "field-FOLLOWING");
        check(fd.contains("KEY"), "field-KEY");
        check(fd.contains("TEXT"), "field-TEXT");
        check(fd.contains("ATTRIBUTE"), "field-ATTRIBUTE");
        check(fd.contains("GT"), "field-GT");
        check(fd.contains("NODE"), "field-NODE");
        check(fd.contains("OPERATORS"), "field-OPERATORS");
        check(md.size() == 0, "method-count");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
