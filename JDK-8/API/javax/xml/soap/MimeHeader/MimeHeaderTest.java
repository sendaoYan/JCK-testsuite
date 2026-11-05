/*
 * @test
 * @summary javax.xml.soap.MimeHeader conformance: mime header.
 * @run main MimeHeaderTest
 */


public class MimeHeaderTest {
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
        javax.xml.soap.MimeHeader h = new javax.xml.soap.MimeHeader("Content-Type", "text/plain");
        check("Content-Type".equals(h.getName()), "name");
        check("text/plain".equals(h.getValue()), "value");
        testPublicConstructor();
        testViaSoapPart();
        testViaAttachment();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // 公开构造器直建实例（MimeHeader 少有的可实例化值类型）
    static void testPublicConstructor() throws Exception {
        javax.xml.soap.MimeHeader a = new javax.xml.soap.MimeHeader("K", "V");
        check("K".equals(a.getName()), "direct ctor name");
        check("V".equals(a.getValue()), "direct ctor value");
        // 实测锁定：MimeHeader 未覆写 equals/hashCode——同参两实例也不等（身份语义）
        javax.xml.soap.MimeHeader b = new javax.xml.soap.MimeHeader("K", "V");
        check(!a.equals(b), "same-param MimeHeaders NOT equal (identity semantics, no equals override)");
        check(a.hashCode() != b.hashCode(), "identity hashCode differs across instances");
        javax.xml.soap.MimeHeader c = new javax.xml.soap.MimeHeader("K", "OTHER");
        check(!a.equals(c), "different value not equal");
        javax.xml.soap.MimeHeader d = new javax.xml.soap.MimeHeader("OTHER", "V");
        check(!a.equals(d), "different name not equal");
        check(!a.equals(null), "not equal to null");
        check(!a.equals("K"), "not equal to String");
        // 空串合法
        javax.xml.soap.MimeHeader e = new javax.xml.soap.MimeHeader("", "");
        check("".equals(e.getName()) && "".equals(e.getValue()), "empty name/value accepted");
        // 实测：null 参数也被接受（宽松构造，锁定）
        javax.xml.soap.MimeHeader n = new javax.xml.soap.MimeHeader(null, null);
        check(n.getName() == null && n.getValue() == null, "null name/value accepted by RI ctor");
        // class 形态
        check(!javax.xml.soap.MimeHeader.class.isInterface(), "MimeHeader is a concrete class");
    }

    // SOAPPart 头表迭代取实例回读（真实通道 1）
    @SuppressWarnings("unchecked")
    static void testViaSoapPart() throws Exception {
        javax.xml.soap.SOAPPart part = javax.xml.soap.MessageFactory.newInstance()
                .createMessage().getSOAPPart();
        part.removeAllMimeHeaders();
        part.addMimeHeader("X-Mine", "mv1");
        part.addMimeHeader("X-Mine", "mv2");
        java.util.Iterator it = part.getAllMimeHeaders();
        int n = 0;
        while (it.hasNext()) {
            Object o = it.next();
            check(o instanceof javax.xml.soap.MimeHeader, "SOAPPart header iteration yields MimeHeader");
            javax.xml.soap.MimeHeader mh = (javax.xml.soap.MimeHeader) o;
            check("X-Mine".equals(mh.getName()), "header name read back");
            check("mv1".equals(mh.getValue()) || "mv2".equals(mh.getValue()), "header value one of stored");
            n++;
        }
        check(n == 2, "two headers iterated via SOAPPart");
    }

    // AttachmentPart 头表迭代取实例回读（真实通道 2）
    @SuppressWarnings("unchecked")
    static void testViaAttachment() throws Exception {
        javax.xml.soap.SOAPMessage m = javax.xml.soap.MessageFactory.newInstance().createMessage();
        javax.xml.soap.AttachmentPart ap = m.createAttachmentPart();
        ap.setContent("hello", "text/plain");
        ap.setContentId("mid-1");
        ap.addMimeHeader("X-Att", "av");
        java.util.Iterator it = ap.getAllMimeHeaders();
        int n = 0;
        boolean sawId = false, sawAtt = false, sawCt = false;
        while (it.hasNext()) {
            javax.xml.soap.MimeHeader mh = (javax.xml.soap.MimeHeader) it.next();
            n++;
            if ("Content-Id".equalsIgnoreCase(mh.getName()) && "mid-1".equals(mh.getValue())) sawId = true;
            if ("X-Att".equals(mh.getName()) && "av".equals(mh.getValue())) sawAtt = true;
            if ("Content-Type".equalsIgnoreCase(mh.getName()) && mh.getValue().contains("text/plain")) sawCt = true;
        }
        check(n >= 3, "attachment exposes id/type/custom headers: " + n);
        check(sawId, "Content-Id header pair readable");
        check(sawAtt, "custom header pair readable");
        check(sawCt, "Content-Type header pair readable");
        // matching 过滤视角
        java.util.Iterator m2 = ap.getMatchingMimeHeaders(new String[]{"X-Att"});
        check(m2.hasNext() && "av".equals(((javax.xml.soap.MimeHeader) m2.next()).getValue()),
                "attachment getMatchingMimeHeaders returns MimeHeader with value");
        java.util.Iterator nm = ap.getNonMatchingMimeHeaders(new String[]{"X-Att"});
        int c = 0;
        while (nm.hasNext()) { nm.next(); c++; }
        check(c >= 2, "non-matching excludes X-Att");
    }
}
