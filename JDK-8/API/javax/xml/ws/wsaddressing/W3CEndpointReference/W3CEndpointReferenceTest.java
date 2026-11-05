/*
 * @test
 * @summary javax.xml.ws.wsaddressing.W3CEndpointReference conformance: endpoint reference construction from Source, marshalling round-trip, exact negatives, and a live reference from a locally published Endpoint.
 * @run main W3CEndpointReferenceTest
 */

import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Endpoint;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;

public class W3CEndpointReferenceTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static String WSA = "http://www.w3.org/2005/08/addressing";
    static String GOOD = "<EndpointReference xmlns=\"" + WSA + "\">"
        + "<Address>http://a</Address></EndpointReference>";

    static String write(W3CEndpointReference e) throws Exception {
        StringWriter sw = new StringWriter();
        e.writeTo(new StreamResult(sw));
        return sw.toString();
    }

    @javax.jws.WebService(serviceName = "svc", targetNamespace = "urn:t", portName = "p")
    public static class P {
        public String m() { return "x"; }
    }

    public static void main(String[] args) throws Exception {
        testClassFace();
        // —— 保留的原有断言（builder 构建 + writeTo 通道）——
        javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder b = new javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder();
        b.address("http://example.com/service");
        javax.xml.ws.wsaddressing.W3CEndpointReference epr = b.build();
        check(epr != null, "epr non-null");
        check(epr instanceof javax.xml.ws.EndpointReference, "extends EndpointReference");
        javax.xml.transform.stream.StreamResult out = new javax.xml.transform.stream.StreamResult(new java.io.StringWriter());
        epr.writeTo(out);
        testConstruction();
        testNegatives();
        testLenientRoots();
        testEqualsAndToString();
        testReadFrom();
        testGetPort();
        testLiveReference();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // 类面：final，extends EndpointReference，public(Source)+protected() 构造器，writeTo 覆写，NS 常量
    static void testClassFace() throws Exception {
        Class<?> c = Class.forName("javax.xml.ws.wsaddressing.W3CEndpointReference");
        check(Modifier.isFinal(c.getModifiers()), "class final");
        check(Modifier.isPublic(c.getModifiers()), "class public");
        check(c.getSuperclass() == EndpointReference.class, "extends EndpointReference");
        check(c.getInterfaces().length == 0, "implements no interfaces");
        Constructor<?>[] ctors = c.getDeclaredConstructors();
        check(ctors.length == 2, "exactly 2 declared ctors: " + ctors.length);
        Constructor<?> pub = c.getConstructor(Source.class);
        check(Modifier.isPublic(pub.getModifiers()), "public ctor takes Source");
        check(pub.getParameterTypes().length == 1
              && pub.getParameterTypes()[0] == Source.class, "public ctor single Source param");
        boolean hasProtectedNoArg = false;
        for (Constructor<?> k : ctors) {
            if (k.getParameterTypes().length == 0 && Modifier.isProtected(k.getModifiers())) {
                hasProtectedNoArg = true;
            }
        }
        check(hasProtectedNoArg, "protected no-arg ctor for subclass-free RI use");
        Method[] dm = c.getDeclaredMethods();
        int publicOwn = 0;
        Method writeTo = null;
        for (Method m : dm) {
            if (!Modifier.isPublic(m.getModifiers()) || m.isSynthetic()) continue;
            publicOwn++;
            if (m.getName().equals("writeTo")) writeTo = m;
        }
        check(publicOwn == 1, "exactly 1 public own method (writeTo): " + publicOwn);
        check(writeTo != null, "declares writeTo");
        check(writeTo.getReturnType() == void.class, "writeTo void");
        check(writeTo.getParameterTypes().length == 1
              && writeTo.getParameterTypes()[0] == Result.class, "writeTo takes Result");
        // NS 命名空间常量（protected static）
        Field ns = c.getDeclaredField("NS");
        check(Modifier.isStatic(ns.getModifiers()) && Modifier.isProtected(ns.getModifiers()),
              "NS protected static");
        ns.setAccessible(true);
        check(WSA.equals(ns.get(null)), "NS value: " + ns.get(null));
    }

    // 构造与序列化往返：writeTo/toString 均含 W3C Addressing 信封
    static void testConstruction() throws Exception {
        W3CEndpointReference e = new W3CEndpointReference(new StreamSource(new StringReader(GOOD)));
        check(e != null, "ctor(StreamSource) non-null");
        String xml = write(e);
        check(xml.contains(WSA), "writeTo carries WSA namespace");
        check(xml.contains("http://a"), "writeTo carries Address");
        check(xml.contains("EndpointReference"), "writeTo root element");
        check(e.toString().contains("http://a"), "toString mirrors marshalled form");
        check(e.toString().contains("EndpointReference"), "toString root present");
        // 带子元素的完整形式
        String rich = "<EndpointReference xmlns=\"" + WSA + "\">"
            + "<Address>http://b</Address>"
            + "<ReferenceParameters><mid xmlns=\"urn:m\">1</mid></ReferenceParameters>"
            + "<Metadata><tns xmlns=\"urn:m\">meta</tns></Metadata>"
            + "</EndpointReference>";
        W3CEndpointReference r = new W3CEndpointReference(new StreamSource(new StringReader(rich)));
        String rx = write(r);
        check(rx.contains("http://b"), "rich form keeps Address");
        check(rx.contains("ReferenceParameters"), "rich form keeps ReferenceParameters");
        check(rx.contains("Metadata"), "rich form keeps Metadata");
        // writeTo 可重复调用（幂等序列化）
        check(write(r).equals(rx), "writeTo repeatable and stable");
    }

    // 负例：异常类型与消息精确
    static void testNegatives() {
        try {
            new W3CEndpointReference(null);
            check(false, "null source must throw");
        } catch (IllegalArgumentException iae) {
            check(true, "ctor(null) -> IllegalArgumentException");
        }
        try {
            new W3CEndpointReference(new StreamSource(new StringReader("<bad")));
            check(false, "malformed must throw");
        } catch (WebServiceException wse) {
            check("Error unmarshalling W3CEndpointReference".equals(wse.getMessage().trim()),
                  "ctor(malformed) message: " + wse.getMessage());
        }
        try {
            W3CEndpointReference e = new W3CEndpointReference(
                new StreamSource(new StringReader(GOOD)));
            e.writeTo(null);
            check(false, "writeTo(null) must throw");
        } catch (WebServiceException wse) {
            check(wse.getMessage() != null && wse.getMessage().contains("Error marshalling"),
                  "writeTo(null) message: " + wse.getMessage());
        }
    }

    // 宽容面（实测）：错根/错命名空间不抛异常，写出被规范化为 wsa:EndpointReference
    static void testLenientRoots() throws Exception {
        W3CEndpointReference wrongRoot = new W3CEndpointReference(
            new StreamSource(new StringReader("<foo xmlns=\"" + WSA + "\"/>")));
        String x1 = write(wrongRoot);
        check(x1.contains(WSA), "wrong root: output still WSA-namespaced");
        check(x1.contains("EndpointReference"), "wrong root canonicalized to EndpointReference");
        W3CEndpointReference wrongNs = new W3CEndpointReference(
            new StreamSource(new StringReader("<EndpointReference xmlns=\"urn:zz\"/>")));
        String x2 = write(wrongNs);
        check(x2.contains(WSA), "wrong ns: output canonical WSA namespace");
        check(!x2.contains("urn:zz"), "wrong ns: original namespace dropped");
    }

    // equals 按身份（未覆写）；hashCode 自一致
    static void testEqualsAndToString() throws Exception {
        W3CEndpointReference a = new W3CEndpointReference(new StreamSource(new StringReader(GOOD)));
        W3CEndpointReference b = new W3CEndpointReference(new StreamSource(new StringReader(GOOD)));
        check(a.equals(a), "equals self true");
        check(!a.equals(b), "identity semantics: equal XML, distinct instances not equal");
        check(a.hashCode() == a.hashCode(), "hashCode stable");
        check(!a.equals(new Object()), "equals non-EPR false");
        check(a != b, "distinct objects");
    }

    // 静态工厂 readFrom（继承自 EndpointReference）：产出 W3CEndpointReference
    static void testReadFrom() {
        EndpointReference r = EndpointReference.readFrom(new StreamSource(new StringReader(GOOD)));
        check(r != null, "readFrom non-null");
        check(r instanceof W3CEndpointReference, "readFrom yields W3CEndpointReference: "
              + r.getClass().getName());
        try {
            EndpointReference.readFrom(null);
            check(false, "readFrom(null) must throw");
        } catch (IllegalArgumentException iae) {
            check("source parameter must not be null".equals(iae.getMessage()),
                  "readFrom(null) message: " + iae.getMessage());
        }
        try {
            EndpointReference.readFrom(new StreamSource(new StringReader("<bad")));
            check(false, "readFrom(malformed) must throw");
        } catch (WebServiceException wse) {
            check(true, "readFrom(malformed) -> WebServiceException");
        }
    }

    // getPort 无 WSDL 元数据 -> WebServiceException（精确消息实测）
    static void testGetPort() {
        W3CEndpointReference e = new W3CEndpointReference(new StreamSource(new StringReader(GOOD)));
        try {
            e.getPort(Runnable.class);
            check(false, "getPort without metadata must throw");
        } catch (WebServiceException wse) {
            check(wse.getMessage() != null && wse.getMessage().contains("WSDL metadata is missing"),
                  "getPort message: " + wse.getMessage());
        }
        try {
            e.getPort(null);
            check(false, "getPort(null) must throw");
        } catch (WebServiceException wse) {
            check(true, "getPort(null) -> WebServiceException (no usable metadata)");
        } catch (NullPointerException npe) {
            check(true, "getPort(null) -> NPE (recorded)");
        }
    }

    // 真实通道：localhost:0 发布端点，getEndpointReference 取活引用（发布后立即停止）
    static void testLiveReference() throws Exception {
        Endpoint ep = Endpoint.create(new P());
        ep.publish("http://localhost:0/p");
        try {
            check(ep.isPublished(), "published for EPR");
            W3CEndpointReference live = ep.getEndpointReference(W3CEndpointReference.class);
            check(live != null, "getEndpointReference non-null");
            check(live instanceof W3CEndpointReference, "typed getEndpointReference");
            String xml = write(live);
            check(xml.contains("http://localhost:"), "live Address carries bound host:port");
            check(xml.contains("/p"), "live Address path /p");
            check(xml.contains("EndpointName=\"p\""), "metadata carries EndpointName p");
            check(xml.contains("svc"), "metadata carries ServiceName svc");
            check(xml.contains("?wsdl"), "metadata carries wsdlLocation");
            // 无类型重载也产出 W3C 形式
            EndpointReference untyped = ep.getEndpointReference();
            check(untyped instanceof W3CEndpointReference, "untyped overload also W3CEndpointReference");
        } finally {
            ep.stop();
        }
        check(!ep.isPublished(), "endpoint stopped");
    }
}
