/*
 * @test
 * @summary javax.xml.crypto.dsig.spec.ExcC14NParameterSpec conformance: exclusive c14n parameter spec.
 * @run main ExcC14NParameterSpecTest
 */


public class ExcC14NParameterSpecTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        javax.xml.crypto.dsig.spec.ExcC14NParameterSpec s = new javax.xml.crypto.dsig.spec.ExcC14NParameterSpec();
        check(s.getPrefixList() != null, "prefix list non-null");
        java.util.List<String> pl = new java.util.ArrayList<String>();
        pl.add("ds");
        javax.xml.crypto.dsig.spec.ExcC14NParameterSpec s2 = new javax.xml.crypto.dsig.spec.ExcC14NParameterSpec(pl);
        check(s2.getPrefixList().equals(pl), "prefix list");
        check(s2 instanceof javax.xml.crypto.dsig.spec.C14NMethodParameterSpec, "is C14NMethodParameterSpec");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
