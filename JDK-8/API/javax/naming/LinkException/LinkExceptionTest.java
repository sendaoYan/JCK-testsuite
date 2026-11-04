/*
 * @test
 * @summary javax.naming.LinkException conformance: full contract coverage.
 *          message/hierarchy plus link resolved/remaining name (clone
 *          semantics), link resolved obj, link explanation, and toString /
 *          toString(boolean) with Link Remaining Name and Link Resolved Object.
 * @run main LinkExceptionTest
 */

import javax.naming.CompositeName;
import javax.naming.LinkException;
import javax.naming.Name;

public class LinkExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- original coverage (kept) ------------------------------------------

    static void testBasic() {
        LinkException e = new LinkException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e instanceof javax.naming.NamingException, "is NamingException");
        check(e.getLinkRemainingName() == null, "linkRemainingName null");
        check(e.getLinkResolvedName() == null, "linkResolvedName null");
        check(e.getLinkResolvedObj() == null, "linkResolvedObj null");
        check(e.getLinkExplanation() == null, "linkExplanation null");
        LinkException e0 = new LinkException();
        check(e0.getMessage() == null, "no-arg message null");
    }

    static void testAccessors() throws Exception {
        LinkException e = new LinkException();
        Name n = new CompositeName("a/b");
        e.setLinkResolvedName(n);
        check(e.getLinkResolvedName().equals(n) && e.getLinkResolvedName() != n,
                "setLinkResolvedName clones");
        e.setLinkRemainingName(n);
        check(e.getLinkRemainingName().equals(n) && e.getLinkRemainingName() != n,
                "setLinkRemainingName clones");
        Object obj = new Object();
        e.setLinkResolvedObj(obj);
        check(e.getLinkResolvedObj() == obj, "setLinkResolvedObj");
        e.setLinkExplanation("expl");
        check("expl".equals(e.getLinkExplanation()), "setLinkExplanation");
    }

    static void testToString() throws Exception {
        LinkException e = new LinkException("boom");
        check("javax.naming.LinkException: boom; Link Remaining Name: 'null'".equals(e.toString()),
                "toString with null remaining name");
        e.setLinkRemainingName(new CompositeName("rem"));
        check(e.toString().endsWith("; Link Remaining Name: 'rem'"), "toString link remaining name");
        e.setLinkResolvedObj("obj");
        String plain = e.toString();
        check(!plain.contains("Link Resolved Object"), "toString (detail=false) omits resolved obj");
        check(e.toString(true).contains("; Link Resolved Object: obj"), "toString(detail=true) has resolved obj");
    }

    public static void main(String[] args) throws Exception {
        testBasic();
        testAccessors();
        testToString();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
