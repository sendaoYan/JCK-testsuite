/*
 * @test
 * @summary javax.naming.Reference conformance: className/factory/add/get.
 * @run main ReferenceTest
 */


public class ReferenceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.naming.Reference r = new javax.naming.Reference("java.lang.String");
        check("java.lang.String".equals(r.getClassName()), "getClassName");
        check(r.getFactoryClassName() == null, "factory null default");
        r.add(new javax.naming.StringRefAddr("t", "v"));
        r.add(new javax.naming.StringRefAddr("u", "w"));
        check(r.size() == 2, "size");
        check("v".equals(((javax.naming.StringRefAddr) r.get("t")).getContent()), "get");
        check("v".equals(((javax.naming.StringRefAddr) r.get(0)).getContent()), "get(0)");
        check(r.getAll() != null, "getAll");
        javax.naming.Reference r2 = new javax.naming.Reference("java.lang.String", "com.Factory", null);
        check("com.Factory".equals(r2.getFactoryClassName()), "getFactoryClassName");
        check(r2.getFactoryClassLocation() == null, "getFactoryClassLocation null");        System.out.println("PASS (" + passed + " assertions)");
    }
}
