/*
 * @test
 * @summary javax.xml.soap.AttachmentPart conformance: abstract class hierarchy and methods.
 * @run main AttachmentPartTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class AttachmentPartTest {
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
        Class<?> c = Class.forName("javax.xml.soap.AttachmentPart");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getSize"), "has method getSize");
        check(names.contains("clearContent"), "has method clearContent");
        check(names.contains("getContent"), "has method getContent");
        check(names.contains("getRawContent"), "has method getRawContent");
        check(names.contains("getRawContentBytes"), "has method getRawContentBytes");
        check(names.contains("getBase64Content"), "has method getBase64Content");
        check(names.contains("setContent"), "has method setContent");
        check(names.contains("setRawContent"), "has method setRawContent");
        check(names.contains("setRawContentBytes"), "has method setRawContentBytes");
        check(names.contains("setBase64Content"), "has method setBase64Content");
        check(names.contains("getDataHandler"), "has method getDataHandler");
        check(names.contains("setDataHandler"), "has method setDataHandler");
        check(names.contains("getContentId"), "has method getContentId");
        check(names.contains("getContentLocation"), "has method getContentLocation");
        check(names.contains("getContentType"), "has method getContentType");
        check(names.contains("setContentId"), "has method setContentId");
        check(names.contains("setContentLocation"), "has method setContentLocation");
        check(names.contains("setContentType"), "has method setContentType");
        check(names.contains("removeMimeHeader"), "has method removeMimeHeader");
        check(names.contains("removeAllMimeHeaders"), "has method removeAllMimeHeaders");
        check(names.contains("getMimeHeader"), "has method getMimeHeader");
        check(names.contains("setMimeHeader"), "has method setMimeHeader");
        check(names.contains("addMimeHeader"), "has method addMimeHeader");
        check(names.contains("getAllMimeHeaders"), "has method getAllMimeHeaders");
        check(names.contains("getMatchingMimeHeaders"), "has method getMatchingMimeHeaders");
        check(names.contains("getNonMatchingMimeHeaders"), "has method getNonMatchingMimeHeaders");
        testContentBasics();
        testHeaderFamily();
        testRawAndBase64();
        testClearContent();
        testLifecycleOnMessage();
        testMimeRoundTrip();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static javax.xml.soap.AttachmentPart newPart() throws Exception {
        return javax.xml.soap.MessageFactory.newInstance().createMessage().createAttachmentPart();
    }

    static int countIter(java.util.Iterator it) {
        int c = 0;
        while (it.hasNext()) { it.next(); c++; }
        return c;
    }

    // setContent(Object,String)/getContent 回读 + id/type/location 三属性
    static void testContentBasics() throws Exception {
        javax.xml.soap.AttachmentPart ap = newPart();
        check(ap.getContentId() == null, "default content id null");
        check(ap.getContentType() == null, "default content type null");
        check(ap.getContentLocation() == null, "default content location null");
        check(ap.getSize() == 0, "default size 0");
        ap.setContent("hello", "text/plain");
        check("hello".equals(ap.getContent()), "setContent(String)/getContent round-trip");
        check(ap.getContent() instanceof String, "getContent type follows setContent");
        check(ap.getSize() == 5, "size == content byte length");
        ap.setContentId("att-1");
        check("att-1".equals(ap.getContentId()), "setContentId round-trip");
        ap.setContentType("text/plain; charset=UTF-8");
        check("text/plain; charset=UTF-8".equals(ap.getContentType()), "setContentType round-trip");
        ap.setContentLocation("http://loc/");
        check("http://loc/".equals(ap.getContentLocation()), "setContentLocation round-trip");
        // setContentId 映射 Content-Id 头
        check(ap.getMimeHeader("Content-Id") != null && "att-1".equals(ap.getMimeHeader("Content-Id")[0]),
                "setContentId backs Content-Id header");
        // byte[] 内容
        javax.xml.soap.AttachmentPart ap2 = newPart();
        ap2.setContent(new byte[]{65, 66}, "application/octet-stream");
        check(ap2.getContent() instanceof byte[], "byte[] content stays byte[]");
        check(ap2.getSize() == 2, "byte[] size 2");
        // javax.activation.DataHandler 内容
        javax.activation.DataHandler dh = new javax.activation.DataHandler("abcd", "text/plain");
        javax.xml.soap.AttachmentPart ap3 = newPart();
        ap3.setDataHandler(dh);
        check(ap3.getDataHandler() == dh, "setDataHandler/getDataHandler same instance");
        check("abcd".equals(ap3.getContent()), "DataHandler-backed getContent");
        // 消息便捷工厂
        javax.xml.soap.SOAPMessage m = javax.xml.soap.MessageFactory.newInstance().createMessage();
        javax.xml.soap.AttachmentPart conv = m.createAttachmentPart("obj", "text/xml");
        check("obj".equals(conv.getContent()), "createAttachmentPart(Object,String) convenience");
        javax.xml.soap.AttachmentPart conv2 = m.createAttachmentPart(dh);
        check(conv2.getDataHandler() == dh, "createAttachmentPart(DataHandler) convenience");
    }

    // addMimeHeader/setMimeHeader/remove*/getAll*/getMatching*/getNonMatching*
    @SuppressWarnings("unchecked")
    static void testHeaderFamily() throws Exception {
        javax.xml.soap.AttachmentPart ap = newPart();
        ap.setContent("x", "text/plain");
        ap.removeAllMimeHeaders();
        check(countIter(ap.getAllMimeHeaders()) == 0, "after removeAllMimeHeaders empty");
        ap.addMimeHeader("X-A", "v1");
        ap.addMimeHeader("X-A", "v2");
        String[] multi = ap.getMimeHeader("X-A");
        check(multi.length == 2 && "v1".equals(multi[0]) && "v2".equals(multi[1]),
                "addMimeHeader multi-value order kept");
        check(ap.getMimeHeader("x-a") != null && ap.getMimeHeader("x-a").length == 2,
                "getMimeHeader case-insensitive");
        ap.setMimeHeader("X-A", "replaced");
        check(ap.getMimeHeader("X-A").length == 1 && "replaced".equals(ap.getMimeHeader("X-A")[0]),
                "setMimeHeader replaces all values");
        ap.addMimeHeader("X-B", "bv");
        check(countIter(ap.getAllMimeHeaders()) == 2, "getAllMimeHeaders yields 2");
        check(countIter(ap.getMatchingMimeHeaders(new String[]{"X-A"})) == 1, "matching 1");
        check(countIter(ap.getNonMatchingMimeHeaders(new String[]{"X-A"})) == 1, "non-matching 1");
        java.util.Iterator it = ap.getAllMimeHeaders();
        while (it.hasNext()) {
            check(it.next() instanceof javax.xml.soap.MimeHeader, "header iteration yields MimeHeader");
        }
        ap.removeMimeHeader("X-B");
        check(ap.getMimeHeader("X-B") == null, "removeMimeHeader removes");
        ap.removeMimeHeader("X-None");
        check(countIter(ap.getAllMimeHeaders()) == 1, "removeMimeHeader unknown is no-op");
    }

    // setRawContentBytes/setRawContent/getRawContentBytes/getBase64Content/setBase64Content 编解码对偶
    static void testRawAndBase64() throws Exception {
        javax.xml.soap.AttachmentPart ap = newPart();
        ap.setRawContentBytes(new byte[]{65, 66, 67}, 0, 3, "application/octet-stream");
        check(new String(ap.getRawContentBytes(), "UTF-8").equals("ABC"), "setRawContentBytes/getRawContentBytes");
        check("application/octet-stream".equals(ap.getContentType()), "contentType follows setRawContentBytes");
        check(ap.getSize() == 3, "size follows raw bytes");
        check(ap.getRawContent() != null, "getRawContent stream non-null");
        ap.setRawContent(new java.io.ByteArrayInputStream("XYZ".getBytes("UTF-8")), "text/plain");
        check(new String(ap.getRawContentBytes(), "UTF-8").equals("XYZ"), "setRawContent/getRawContentBytes");
        // base64 对偶：内容 "WXYZ" 的 base64 是 "V1hZWg=="
        javax.xml.soap.AttachmentPart ap2 = newPart();
        ap2.setContent("WXYZ", "text/plain");
        java.io.InputStream b64 = ap2.getBase64Content();
        byte[] buf = new byte[64];
        int r = b64.read(buf);
        String got = new String(buf, 0, Math.max(r, 0), "US-ASCII");
        check("V1hZWg==".equals(got), "getBase64Content returns base64 of content: " + got);
        // setBase64Content 输入 base64 → 原文恢复
        javax.xml.soap.AttachmentPart ap3 = newPart();
        ap3.setBase64Content(new java.io.ByteArrayInputStream("WFhY".getBytes("US-ASCII")), "text/plain");
        check(new String(ap3.getRawContentBytes(), "UTF-8").equals("XXX"), "setBase64Content decodes to raw");
    }

    // clearContent：内容失效（getContent 抛 SOAPException）但头保留、getSize 归 0（实测）
    static void testClearContent() throws Exception {
        javax.xml.soap.AttachmentPart ap = newPart();
        ap.setContent("hello", "text/plain");
        ap.setContentId("keep-me");
        ap.clearContent();
        check(ap.getSize() == 0, "size 0 after clearContent");
        try {
            ap.getContent();
            check(false, "getContent after clearContent should throw SOAPException");
        } catch (javax.xml.soap.SOAPException e) {
            check(e.getMessage() != null && e.getMessage().contains("No data handler"),
                    "cleared getContent message: " + e.getMessage());
        }
        try {
            ap.getRawContent();
            check(false, "getRawContent after clearContent should throw");
        } catch (javax.xml.soap.SOAPException e) {
            check(true, "cleared getRawContent throws SOAPException");
        }
        try {
            byte[] b = ap.getRawContentBytes();
            check(false, "getRawContentBytes after clearContent should throw");
        } catch (javax.xml.soap.SOAPException e) {
            check(true, "cleared getRawContentBytes throws SOAPException");
        }
        // 头不被 clearContent 清除
        check("keep-me".equals(ap.getContentId()), "headers survive clearContent");
        // 内容可重新设置
        ap.setContent("again", "text/plain");
        check("again".equals(ap.getContent()), "content resettable after clearContent");
    }

    // 消息侧全序：addAttachmentPart→countAttachments→getAttachments→getAttachments(hdr)→removeAttachments→清零
    @SuppressWarnings("unchecked")
    static void testLifecycleOnMessage() throws Exception {
        javax.xml.soap.SOAPMessage m = javax.xml.soap.MessageFactory.newInstance().createMessage();
        check(m.countAttachments() == 0, "no attachments initially");
        javax.xml.soap.AttachmentPart a1 = m.createAttachmentPart();
        a1.setContent("data1", "text/plain");
        a1.setContentId("id-1");
        javax.xml.soap.AttachmentPart a2 = m.createAttachmentPart();
        a2.setContent("data2", "application/octet-stream");
        a2.setContentId("id-2");
        m.addAttachmentPart(a1);
        m.addAttachmentPart(a2);
        check(m.countAttachments() == 2, "count 2 after adds");
        java.util.Iterator it = m.getAttachments();
        int n = 0;
        boolean saw1 = false;
        while (it.hasNext()) {
            javax.xml.soap.AttachmentPart ap = (javax.xml.soap.AttachmentPart) it.next();
            n++;
            check(ap instanceof javax.xml.soap.AttachmentPart, "iterated object is AttachmentPart");
            if ("id-1".equals(ap.getContentId())) saw1 = true;
        }
        check(n == 2, "iterator sees 2");
        check(saw1, "id-1 seen");
        javax.xml.soap.MimeHeaders f = new javax.xml.soap.MimeHeaders();
        f.addHeader("Content-Id", "id-1");
        check(countIter(m.getAttachments(f)) == 1, "filtered by id == 1");
        javax.xml.soap.MimeHeaders fct = new javax.xml.soap.MimeHeaders();
        fct.addHeader("Content-Type", "text/plain");
        check(countIter(m.getAttachments(fct)) == 1, "filtered by type == 1");
        javax.xml.soap.MimeHeaders none = new javax.xml.soap.MimeHeaders();
        none.addHeader("Content-Id", "none-such");
        check(countIter(m.getAttachments(none)) == 0, "filtered by unknown == 0");
        m.removeAttachments(f);
        check(m.countAttachments() == 1, "after removeAttachments(id-1) count 1");
        check("id-2".equals(((javax.xml.soap.AttachmentPart) m.getAttachments().next()).getContentId()),
                "id-2 survives");
        m.removeAllAttachments();
        check(m.countAttachments() == 0, "removeAllAttachments clears");
        // 附件对象本身仍可用（脱离消息）
        check("data1".equals(a1.getContent()), "detached attachment still readable");
    }

    // 含附件 MIME 往返：saveChanges+writeTo → 传 Content-Type boundary 读回 count==1 且内容保真
    @SuppressWarnings("unchecked")
    static void testMimeRoundTrip() throws Exception {
        javax.xml.soap.MessageFactory mf = javax.xml.soap.MessageFactory.newInstance();
        javax.xml.soap.SOAPMessage m = mf.createMessage();
        m.getSOAPBody().addBodyElement(new javax.xml.namespace.QName("http://x/", "pay", "p")).addTextNode("99");
        javax.xml.soap.AttachmentPart ap = m.createAttachmentPart();
        ap.setContent("attdata", "text/plain");
        ap.setContentId("att-r");
        m.addAttachmentPart(ap);
        m.saveChanges();
        java.io.ByteArrayOutputStream bo = new java.io.ByteArrayOutputStream();
        m.writeTo(bo);
        String mime = new String(bo.toByteArray(), "UTF-8");
        check(mime.contains("attdata"), "MIME serialization contains attachment payload");
        check(mime.contains("99"), "MIME serialization contains soap payload");
        String[] ct = m.getMimeHeaders().getHeader("Content-Type");
        check(ct[0].contains("multipart"), "Content-Type switched to multipart: " + ct[0]);
        javax.xml.soap.MimeHeaders hdrs = new javax.xml.soap.MimeHeaders();
        for (String h : ct) hdrs.addHeader("Content-Type", h);
        javax.xml.soap.SOAPMessage back = mf.createMessage(hdrs, new java.io.ByteArrayInputStream(bo.toByteArray()));
        check(back.countAttachments() == 1, "round-trip restores 1 attachment");
        javax.xml.soap.AttachmentPart backAp = (javax.xml.soap.AttachmentPart) back.getAttachments().next();
        check("attdata".equals(backAp.getContent()), "round-trip attachment content preserved");
        check("att-r".equals(backAp.getContentId()), "round-trip attachment id preserved");
        check(back.getSOAPBody().getTextContent().contains("99"), "round-trip soap body intact");
    }
}
