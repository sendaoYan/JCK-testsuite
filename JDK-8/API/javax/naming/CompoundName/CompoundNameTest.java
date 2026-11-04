/*
 * @test
 * @summary javax.naming.CompoundName conformance: syntax properties.
 * @run main CompoundNameTest
 */


public class CompoundNameTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        java.util.Properties props = new java.util.Properties();
        props.setProperty("jndi.syntax.direction", "left_to_right");
        props.setProperty("jndi.syntax.separator", ".");
        props.setProperty("jndi.syntax.ignorecase", "false");
        props.setProperty("jndi.syntax.escape", "\\");
        javax.naming.CompoundName n = new javax.naming.CompoundName("a.b.c", props);
        check(n.size() == 3, "size");
        check("a".equals(n.get(0)), "get(0)");
        check("a.b.c".equals(n.toString()), "toString");
        javax.naming.CompoundName clone = (javax.naming.CompoundName) n.clone();
        check(n.equals(clone), "clone equals");        System.out.println("PASS (" + passed + " assertions)");
    }
}
