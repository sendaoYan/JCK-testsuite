/*
 * @test
 * @summary javax.naming.Binding conformance: name/class/object.
 * @run main BindingTest
 */


public class BindingTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.naming.Binding b = new javax.naming.Binding("cn=foo", "value");
        check("cn=foo".equals(b.getName()), "getName");
        check("java.lang.String".equals(b.getClassName()), "getClassName");
        check("value".equals(b.getObject()), "getObject");
        b.setObject("other");
        check("other".equals(b.getObject()), "setObject");
        javax.naming.Binding b2 = new javax.naming.Binding("n", "java.lang.Integer", Integer.valueOf(3), true);
        check(b2.isRelative(), "isRelative");
        check("java.lang.Integer".equals(b2.getClassName()), "getClassName with class");        System.out.println("PASS (" + passed + " assertions)");
    }
}
