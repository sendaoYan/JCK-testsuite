/*
 * @test
 * @summary javax.xml.soap.SOAPMessage conformance: abstract class hierarchy and methods.
 * @run main SOAPMessageTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class SOAPMessageTest {
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
        Class<?> c = Class.forName("javax.xml.soap.SOAPMessage");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("setContentDescription"), "has method setContentDescription");
        check(names.contains("getContentDescription"), "has method getContentDescription");
        check(names.contains("getSOAPPart"), "has method getSOAPPart");
        check(names.contains("getSOAPBody"), "has method getSOAPBody");
        check(names.contains("getSOAPHeader"), "has method getSOAPHeader");
        check(names.contains("removeAllAttachments"), "has method removeAllAttachments");
        check(names.contains("countAttachments"), "has method countAttachments");
        check(names.contains("getAttachments"), "has method getAttachments");
        check(names.contains("removeAttachments"), "has method removeAttachments");
        check(names.contains("getAttachment"), "has method getAttachment");
        check(names.contains("addAttachmentPart"), "has method addAttachmentPart");
        check(names.contains("createAttachmentPart"), "has method createAttachmentPart");
        check(names.contains("getMimeHeaders"), "has method getMimeHeaders");
        check(names.contains("saveChanges"), "has method saveChanges");
        check(names.contains("saveRequired"), "has method saveRequired");
        check(names.contains("writeTo"), "has method writeTo");
        check(names.contains("setProperty"), "has method setProperty");
        check(names.contains("getProperty"), "has method getProperty");
        testRoundTrip();
        testDescriptionAndProperties();
        testSaveChangesLifecycle();
        testAttachmentManagement();
        testAttachmentRoundTrip();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // writeTo → createMessage(MimeHeaders, InputStream) 往返闭环（最高优先）
    @SuppressWarnings("unchecked")
    static void testRoundTrip() throws Exception {
        javax.xml.soap.MessageFactory mf = javax.xml.soap.MessageFactory.newInstance();
        javax.xml.soap.SOAPMessage msg = mf.createMessage();
        javax.xml.namespace.QName pay = new javax.xml.namespace.QName("http://x/", "pay", "p");
        msg.getSOAPBody().addBodyElement(pay).addTextNode("99");
        msg.saveChanges();
        java.io.ByteArrayOutputStream bo = new java.io.ByteArrayOutputStream();
        msg.writeTo(bo);
        check(bo.size() > 0, "writeTo produced bytes");
        byte[] bytes = bo.toByteArray();
        String xml = new String(bytes, "UTF-8");
        check(xml.contains("<") , "serialized bytes look like XML");
        // 保存后消息自带含 boundary 的 Content-Type，读回时必须传递才能识别附件
        String[] ct = msg.getMimeHeaders().getHeader("Content-Type");
        check(ct != null && ct.length > 0, "Content-Type header present after saveChanges+writeTo");
        check(ct[0].contains("text/xml"), "Content-Type is text/xml for SOAP 1.1: " + ct[0]);
        javax.xml.soap.MimeHeaders hdrs = new javax.xml.soap.MimeHeaders();
        for (String h : ct) hdrs.addHeader("Content-Type", h);
        javax.xml.soap.SOAPMessage back = mf.createMessage(hdrs, new java.io.ByteArrayInputStream(bytes));
        check(back != null, "re-created message non-null");
        org.w3c.dom.NodeList nl = back.getSOAPBody().getElementsByTagNameNS("http://x/", "pay");
        check(nl.getLength() == 1, "round-trip payload element count == 1");
        check(back.getSOAPBody().getTextContent().contains("99"), "round-trip payload text keeps '99'");
        check(back.getSOAPBody().getElementsByTagNameNS("http://x/", "nopay").getLength() == 0,
                "round-trip unknown element lookup == 0");
        check(back.getSOAPPart().getEnvelope().getNamespaceURI().equals("http://schemas.xmlsoap.org/soap/envelope/"),
                "round-trip envelope ns preserved");
        // getMimeHeaders() 大小写不敏感读取（SAAJ 头表契约）
        javax.xml.soap.MimeHeaders mh = back.getMimeHeaders();
        check(mh.getHeader("content-type") != null, "getMimeHeaders lookup is case-insensitive");
        check(mh == back.getMimeHeaders(), "getMimeHeaders stable identity");
    }

    // contentDescription set/get、setProperty/getProperty 行为矩阵（RI 实测：未知名不抛、get 未知为 null）
    static void testDescriptionAndProperties() throws Exception {
        javax.xml.soap.MessageFactory mf = javax.xml.soap.MessageFactory.newInstance();
        javax.xml.soap.SOAPMessage msg = mf.createMessage();
        check(msg.getContentDescription() == null, "default contentDescription null");
        msg.setContentDescription("desc-x");
        check("desc-x".equals(msg.getContentDescription()), "contentDescription round-trip");
        msg.setContentDescription(null);
        check(msg.getContentDescription() == null, "contentDescription cleared by null");
        // WRITE_XML_DECLARATION set/get 回读
        msg.setProperty(javax.xml.soap.SOAPMessage.WRITE_XML_DECLARATION, "true");
        check("true".equals(String.valueOf(msg.getProperty(javax.xml.soap.SOAPMessage.WRITE_XML_DECLARATION))),
                "WRITE_XML_DECLARATION set/get round-trip");
        msg.setProperty(javax.xml.soap.SOAPMessage.WRITE_XML_DECLARATION, "false");
        check("false".equals(String.valueOf(msg.getProperty(javax.xml.soap.SOAPMessage.WRITE_XML_DECLARATION))),
                "WRITE_XML_DECLARATION reset to false");
        // CHARACTER_SET_ENCODING set/get 回读
        msg.setProperty(javax.xml.soap.SOAPMessage.CHARACTER_SET_ENCODING, "UTF-8");
        check("UTF-8".equals(String.valueOf(msg.getProperty(javax.xml.soap.SOAPMessage.CHARACTER_SET_ENCODING))),
                "CHARACTER_SET_ENCODING round-trip");
        // 新消息默认属性为 null（不跨消息泄漏）
        javax.xml.soap.SOAPMessage fresh = mf.createMessage();
        check(fresh.getProperty(javax.xml.soap.SOAPMessage.WRITE_XML_DECLARATION) == null,
                "fresh message default WRITE_XML_DECLARATION null");
        check(fresh.getProperty(javax.xml.soap.SOAPMessage.CHARACTER_SET_ENCODING) == null,
                "fresh message default CHARACTER_SET_ENCODING null");
        // RI 实测：未知属性名 setProperty/getProperty 均不抛、get 返回 null（比 javadoc 宽松，锁定 RI 行为）
        try {
            msg.setProperty("bogus.prop", "v");
            check(true, "setProperty with unknown name does not throw in RI");
        } catch (javax.xml.soap.SOAPException e) {
            check(false, "setProperty unknown should not throw in RI: " + e.getMessage());
        }
        check(msg.getProperty("bogus.prop2") == null, "getProperty unknown returns null in RI");
        check("javax.xml.soap.write-xml-declaration".equals(javax.xml.soap.SOAPMessage.WRITE_XML_DECLARATION),
                "WRITE_XML_DECLARATION constant value");
        check("javax.xml.soap.character-set-encoding".equals(javax.xml.soap.SOAPMessage.CHARACTER_SET_ENCODING),
                "CHARACTER_SET_ENCODING constant value");
    }

    // saveChanges 幂等 + saveRequired 状态翻转
    static void testSaveChangesLifecycle() throws Exception {
        javax.xml.soap.MessageFactory mf = javax.xml.soap.MessageFactory.newInstance();
        javax.xml.soap.SOAPMessage msg = mf.createMessage();
        msg.getSOAPBody().addBodyElement(new javax.xml.namespace.QName("http://x/", "pay", "p")).addTextNode("99");
        check(msg.saveRequired(), "saveRequired true after tree edit");
        msg.saveChanges();
        check(!msg.saveRequired(), "saveRequired false after saveChanges");
        msg.saveChanges();
        check(!msg.saveRequired(), "saveChanges idempotent");
        check(msg.countAttachments() == 0, "countAttachments == 0 after saveChanges without attachments");
        // RI 实测锁定：saveChanges 后再改树，saveRequired 仍为 false（保存标志闩锁语义，不因树编辑复位）
        msg.getSOAPBody().addBodyElement(new javax.xml.namespace.QName("http://x/", "pay2", "p"));
        check(!msg.saveRequired(), "saveRequired stays false after post-save edit (RI latch semantics)");
        // 全新消息（未动树）saveRequired == true（RI 实测）
        check(mf.createMessage().saveRequired(), "fresh untouched message saveRequired true");
    }

    // 附件全序：create/add/count/iterate/filter/remove/removeAll
    @SuppressWarnings("unchecked")
    static void testAttachmentManagement() throws Exception {
        javax.xml.soap.MessageFactory mf = javax.xml.soap.MessageFactory.newInstance();
        javax.xml.soap.SOAPMessage msg = mf.createMessage();
        check(msg.countAttachments() == 0, "fresh message countAttachments == 0");
        check(!msg.getAttachments().hasNext(), "fresh message getAttachments empty");
        javax.xml.soap.AttachmentPart a1 = msg.createAttachmentPart();
        a1.setContent("data1", "text/plain");
        a1.setContentId("id-1");
        javax.xml.soap.AttachmentPart a2 = msg.createAttachmentPart();
        a2.setContent("data2", "application/octet-stream");
        a2.setContentId("id-2");
        msg.addAttachmentPart(a1);
        msg.addAttachmentPart(a2);
        check(msg.countAttachments() == 2, "countAttachments == 2 after two adds");
        java.util.Iterator<javax.xml.soap.AttachmentPart> it = msg.getAttachments();
        int n = 0;
        boolean saw1 = false, saw2 = false;
        while (it.hasNext()) {
            javax.xml.soap.AttachmentPart ap = it.next();
            n++;
            if ("id-1".equals(ap.getContentId())) saw1 = true;
            if ("id-2".equals(ap.getContentId())) saw2 = true;
        }
        check(n == 2, "getAttachments iterates 2 parts");
        check(saw1 && saw2, "both attachment ids seen in iteration");
        // getAttachments(MimeHeaders) 过滤：Content-Id 命中 1 条
        javax.xml.soap.MimeHeaders matchId = new javax.xml.soap.MimeHeaders();
        matchId.addHeader("Content-Id", "id-1");
        check(countIter(msg.getAttachments(matchId)) == 1, "filter by Content-Id matches 1");
        // Content-Type 过滤
        javax.xml.soap.MimeHeaders matchCt = new javax.xml.soap.MimeHeaders();
        matchCt.addHeader("Content-Type", "text/plain");
        check(countIter(msg.getAttachments(matchCt)) == 1, "filter by Content-Type text/plain matches 1");
        javax.xml.soap.MimeHeaders matchNone = new javax.xml.soap.MimeHeaders();
        matchNone.addHeader("Content-Id", "no-such-id");
        check(countIter(msg.getAttachments(matchNone)) == 0, "filter by unknown id matches 0");
        // 空 MimeHeaders 匹配全部（SAAJ 语义，实测锁定）
        check(countIter(msg.getAttachments(new javax.xml.soap.MimeHeaders())) == 2,
                "empty MimeHeaders filter matches all attachments");
        // removeAttachments(匹配) 只移除匹配项
        javax.xml.soap.MimeHeaders rm = new javax.xml.soap.MimeHeaders();
        rm.addHeader("Content-Id", "id-2");
        msg.removeAttachments(rm);
        check(msg.countAttachments() == 1, "after removeAttachments(id-2) count == 1");
        check("id-1".equals(((javax.xml.soap.AttachmentPart) msg.getAttachments().next()).getContentId()),
                "surviving attachment is id-1");
        // getAttachment(SOAPElement)：href="cid:..." 与 Content-Id 尖括号形式匹配（实测）
        javax.xml.soap.AttachmentPart a3 = msg.createAttachmentPart();
        a3.setContent("d3", "text/plain");
        a3.addMimeHeader("Content-Id", "<br-1>");
        msg.addAttachmentPart(a3);
        javax.xml.soap.SOAPElement ref = msg.getSOAPBody().addChildElement(new javax.xml.namespace.QName("http://x/", "ref"));
        ref.addAttribute(new javax.xml.namespace.QName("href"), "cid:br-1");
        javax.xml.soap.AttachmentPart got = msg.getAttachment(ref);
        check(got != null, "getAttachment resolves href cid:br-1");
        javax.xml.soap.SOAPElement noRef = msg.getSOAPBody().addChildElement(new javax.xml.namespace.QName("http://x/", "noref"));
        check(msg.getAttachment(noRef) == null, "getAttachment on element without href returns null");
        // removeAllAttachments 清零
        msg.removeAllAttachments();
        check(msg.countAttachments() == 0, "after removeAllAttachments count == 0");
        check(!msg.getAttachments().hasNext(), "iterator empty after removeAllAttachments");
        // removeAllAttachments 幂等
        msg.removeAllAttachments();
        check(msg.countAttachments() == 0, "removeAllAttachments idempotent");
        // createAttachmentPart 便捷重载
        javax.xml.soap.AttachmentPart conv = msg.createAttachmentPart("obj", "text/xml");
        check("obj".equals(conv.getContent()), "createAttachmentPart(Object,String) keeps content");
        javax.activation.DataHandler dh = new javax.activation.DataHandler("abcd", "text/plain");
        javax.xml.soap.AttachmentPart conv2 = msg.createAttachmentPart(dh);
        check(conv2 != null && conv2.getDataHandler() != null, "createAttachmentPart(DataHandler) non-null");
    }

    // 含附件完整往返：saveChanges+writeTo → 依 Content-Type boundary 读回 countAttachments==1
    @SuppressWarnings("unchecked")
    static void testAttachmentRoundTrip() throws Exception {
        javax.xml.soap.MessageFactory mf = javax.xml.soap.MessageFactory.newInstance();
        javax.xml.soap.SOAPMessage msg = mf.createMessage();
        msg.getSOAPBody().addBodyElement(new javax.xml.namespace.QName("http://x/", "pay", "p")).addTextNode("99");
        javax.xml.soap.AttachmentPart ap = msg.createAttachmentPart();
        ap.setContent("attdata", "text/plain");
        ap.setContentId("att-r");
        msg.addAttachmentPart(ap);
        msg.saveChanges();
        check(msg.countAttachments() == 1, "countAttachments == 1 after saveChanges");
        java.io.ByteArrayOutputStream bo = new java.io.ByteArrayOutputStream();
        msg.writeTo(bo);
        String mime = new String(bo.toByteArray(), "UTF-8");
        check(mime.contains("attdata"), "serialized MIME contains attachment payload");
        javax.xml.soap.MimeHeaders hdrs = new javax.xml.soap.MimeHeaders();
        String[] ct = msg.getMimeHeaders().getHeader("Content-Type");
        for (String h : ct) hdrs.addHeader("Content-Type", h);
        check(ct[0].contains("multipart"), "Content-Type becomes multipart with attachment: " + ct[0]);
        javax.xml.soap.SOAPMessage back = mf.createMessage(hdrs, new java.io.ByteArrayInputStream(bo.toByteArray()));
        check(back.countAttachments() == 1, "round-trip keeps exactly 1 attachment");
        java.util.Iterator<javax.xml.soap.AttachmentPart> it = back.getAttachments();
        check(it.hasNext(), "round-trip attachment iterator non-empty");
        javax.xml.soap.AttachmentPart backAp = it.next();
        check("attdata".equals(backAp.getContent()), "round-trip attachment content preserved");
        check(back.getSOAPBody().getTextContent().contains("99"), "round-trip soap body preserved alongside attachment");
    }

    static int countIter(java.util.Iterator it) {
        int c = 0;
        while (it.hasNext()) { it.next(); c++; }
        return c;
    }
}
