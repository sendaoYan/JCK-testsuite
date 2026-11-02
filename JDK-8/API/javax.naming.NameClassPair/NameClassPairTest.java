/*
 * @test
 * @summary javax.naming.NameClassPair conformance: name/class/relative.
 * @run main NameClassPairTest
 */


public class NameClassPairTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.naming.NameClassPair p = new javax.naming.NameClassPair("foo", "java.lang.String");
        check("foo".equals(p.getName()), "getName");
        check("java.lang.String".equals(p.getClassName()), "getClassName");
        check(p.isRelative(), "isRelative true default");
        p.setName("bar"); p.setClassName("java.lang.Integer"); p.setRelative(false);
        check("bar".equals(p.getName()), "setName");
        check("java.lang.Integer".equals(p.getClassName()), "setClassName");
        check(!p.isRelative(), "setRelative false");
        p.setRelative(true);
        check(p.isRelative(), "setRelative true");
        p.setNameInNamespace("ns/bar");
        check("ns/bar".equals(p.getNameInNamespace()), "getNameInNamespace");        System.out.println("PASS (" + passed + " assertions)");
    }
}
