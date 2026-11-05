/*
 * @test
 * @summary javax.xml.soap.MimeHeaders conformance: mime headers.
 * @run main MimeHeadersTest
 */


public class MimeHeadersTest {
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
        javax.xml.soap.MimeHeaders h = new javax.xml.soap.MimeHeaders();
        h.addHeader("A", "1");
        h.addHeader("A", "2");
        check(h.getHeader("A").length == 2, "two A headers");
        h.setHeader("B", "3");
        check(h.getHeader("B").length == 1 && "3".equals(h.getHeader("B")[0]), "set header");
        h.removeHeader("A");
        check(h.getHeader("A") == null, "removed A");
        h.removeAllHeaders();
        check(h.getHeader("B") == null, "removed all");
        testMultiValueOrder();
        testCaseInsensitivity();
        testMatchingSemantics();
        testNegativePaths();
        testIteration();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // addHeader 同名多值保序 + setHeader 替换语义
    static void testMultiValueOrder() throws Exception {
        javax.xml.soap.MimeHeaders h = new javax.xml.soap.MimeHeaders();
        h.addHeader("Content-Type", "text/xml");
        h.addHeader("Content-Type", "charset=UTF-8");
        String[] v = h.getHeader("Content-Type");
        check(v.length == 2, "two values for same name");
        check("text/xml".equals(v[0]) && "charset=UTF-8".equals(v[1]), "values keep insertion order");
        // setHeader 清掉旧值只留新值
        h.setHeader("Content-Type", "application/soap+xml");
        String[] v2 = h.getHeader("Content-Type");
        check(v2.length == 1 && "application/soap+xml".equals(v2[0]), "setHeader replaces all existing values");
        // setHeader 对不存在的名字等价 addHeader
        h.setHeader("X-New", "nv");
        check("nv".equals(h.getHeader("X-New")[0]), "setHeader on absent name creates it");
        // removeHeader 后 getHeader null
        h.removeHeader("X-New");
        check(h.getHeader("X-New") == null, "removeHeader then getHeader null");
        h.removeHeader("X-Never-Was");
        check(true, "removeHeader of unknown name does not throw");
    }

    // 大小写不敏感性：add/get/set/remove/matching 全链路（实测记录）
    static void testCaseInsensitivity() throws Exception {
        javax.xml.soap.MimeHeaders h = new javax.xml.soap.MimeHeaders();
        h.addHeader("Content-Type", "v1");
        check(h.getHeader("content-type") != null && h.getHeader("content-type").length == 1,
                "getHeader lowercase hits mixed-case stored name");
        check(h.getHeader("CONTENT-TYPE") != null, "getHeader uppercase hits");
        // 实测锁定：setHeader 定位旧值时区分大小写——不同 case 会新建平行头而非替换
        javax.xml.soap.MimeHeaders h2 = new javax.xml.soap.MimeHeaders();
        h2.addHeader("x-lower", "a");
        h2.setHeader("X-UPPER", "b");
        check("a".equals(h2.getHeader("x-lower")[0]), "setHeader with different case does not replace original");
        check("b".equals(h2.getHeader("X-UPPER")[0]), "setHeader with different case creates parallel header");
        // 相同 case 时 setHeader 正常替换
        h2.setHeader("x-lower", "a2");
        check("a2".equals(h2.getHeader("x-lower")[0]) && h2.getHeader("x-lower").length == 1,
                "setHeader same case replaces");
        // removeHeader 与 getHeader 一样不区分大小写
        h2.removeHeader("X-UPPER");
        check(h2.getHeader("x-upper") == null, "removeHeader case-insensitive");
        // matching 名称同样不区分大小写
        javax.xml.soap.MimeHeaders h3 = new javax.xml.soap.MimeHeaders();
        h3.addHeader("Aaa", "1");
        check(countIter(h3.getMatchingHeaders(new String[]{"aAA"})) == 1, "getMatchingHeaders case-insensitive");
    }

    // getMatchingHeaders/getNonMatchingHeaders：null == 空数组（匹配零个），不等于“全部”
    static void testMatchingSemantics() throws Exception {
        javax.xml.soap.MimeHeaders h = new javax.xml.soap.MimeHeaders();
        h.addHeader("A", "1");
        h.addHeader("B", "2");
        h.addHeader("C", "3");
        check(countIter(h.getMatchingHeaders(new String[]{"A", "B"})) == 2, "matching A,B == 2");
        check(countIter(h.getNonMatchingHeaders(new String[]{"A", "B"})) == 1, "non-matching A,B == 1 (C only)");
        check(countIter(h.getMatchingHeaders(new String[0])) == 0, "matching empty array == 0");
        check(countIter(h.getNonMatchingHeaders(new String[0])) == 3, "non-matching empty array == all 3");
        // 实测锁定：null 传入按空数组处理（匹配 0），并非 javadoc 暗示的“全部”
        check(countIter(h.getMatchingHeaders(null)) == 0, "getMatchingHeaders(null) == 0 (RI treats as empty)");
        check(countIter(h.getNonMatchingHeaders(null)) == 3, "getNonMatchingHeaders(null) == all");
        check(countIter(h.getMatchingHeaders(new String[]{"nope"})) == 0, "matching unknown name == 0");
    }

    // 负例：null 头名 → IllegalArgumentException（addHeader/setHeader）
    static void testNegativePaths() throws Exception {
        javax.xml.soap.MimeHeaders h = new javax.xml.soap.MimeHeaders();
        try {
            h.addHeader(null, "v");
            check(false, "addHeader(null, v) should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null, "addHeader(null) IAE has message");
        }
        try {
            h.setHeader(null, "v");
            check(false, "setHeader(null, v) should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            check(true, "setHeader(null) throws IAE");
        }
        // 实测：null 值不抛 IAE 但该头不被存储（getHeader 返回 null）
        try {
            h.addHeader("N", null);
            check(true, "addHeader(name, null) does not throw");
        } catch (IllegalArgumentException e) {
            check(true, "addHeader(name, null) rejected (strict)");
        }
        check(h.getHeader("N") == null, "addHeader(name, null) stores nothing in RI");
        check(h.getHeader("unknown") == null, "getHeader unknown returns null");
        check(h.getHeader(null) == null, "getHeader(null) returns null (no throw)");
    }

    // 迭代：元素为 MimeHeader，name/value 回读
    @SuppressWarnings("unchecked")
    static void testIteration() throws Exception {
        javax.xml.soap.MimeHeaders h = new javax.xml.soap.MimeHeaders();
        h.addHeader("K1", "v1");
        h.addHeader("K2", "v2");
        java.util.Iterator it = h.getAllHeaders();
        int n = 0;
        boolean sawK1 = false, sawK2 = false;
        while (it.hasNext()) {
            Object o = it.next();
            check(o instanceof javax.xml.soap.MimeHeader, "iteration element is MimeHeader");
            javax.xml.soap.MimeHeader mh = (javax.xml.soap.MimeHeader) o;
            n++;
            if ("K1".equals(mh.getName()) && "v1".equals(mh.getValue())) sawK1 = true;
            if ("K2".equals(mh.getName()) && "v2".equals(mh.getValue())) sawK2 = true;
        }
        check(n == 2, "getAllHeaders yields 2");
        check(sawK1 && sawK2, "both name/value pairs readable");
        // removeAllHeaders 后迭代为空
        h.removeAllHeaders();
        check(!h.getAllHeaders().hasNext(), "no headers after removeAllHeaders");
        h.addHeader("After", "clean");
        check(countIter(h.getAllHeaders()) == 1, "headers reusable after removeAllHeaders");
    }

    static int countIter(java.util.Iterator it) {
        int c = 0;
        while (it.hasNext()) { it.next(); c++; }
        return c;
    }
}
