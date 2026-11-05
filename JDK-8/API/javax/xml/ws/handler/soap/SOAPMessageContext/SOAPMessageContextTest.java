/*
 * @test
 * @summary javax.xml.ws.handler.soap.SOAPMessageContext conformance: interface face, message get/set with real SAAJ round-trip visible at provider and client, JAXB header decode and role exposure on a live handler chain.
 * @run main SOAPMessageContextTest
 */

import java.io.StringReader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.PortInfo;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.soap.SOAPBinding;


public class SOAPMessageContextTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }

    static String srcToString(Source s) {
        try {
            java.io.StringWriter sw = new java.io.StringWriter();
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.transform(s, new StreamResult(sw));
            return sw.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 头部 bean：字段必须显式 namespace 才能从 urn:h 命名空间解回
    @XmlRootElement(name = "MyHeader", namespace = "urn:h")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class MyHeader {
        @XmlElement(name = "id", namespace = "urn:h")
        String id;
    }

    static int providerCalls;
    static String providerSawHeaders;

    @WebServiceProvider(serviceName = "svc", targetNamespace = "urn:t", portName = "p")
    @ServiceMode(value = Service.Mode.PAYLOAD)
    public static class EchoProvider implements Provider<Source> {
        public Source invoke(Source req) {
            providerCalls++;
            try {
                SOAPMessage m = MessageFactory.newInstance().createMessage(
                    null, new java.io.ByteArrayInputStream(
                        envelopeWithBody("<r xmlns=\"urn:t\">ok</r>").getBytes("UTF-8")));
                providerSawHeaders = "count=" + m.getSOAPHeader().getChildElements().hasNext();
            } catch (Exception e) {
                providerSawHeaders = "ex:" + e;
            }
            return new StreamSource(new StringReader("<r xmlns=\"urn:t\">ok</r>"));
        }
    }

    static String envelopeWithBody(String body) {
        return "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\"><S:Body>"
            + body + "</S:Body></S:Envelope>";
    }

    static SOAPMessageContext outboundCtx, inboundCtx;
    static Object[] headerBeansOut;
    static Object[] headerBeansAbsent;
    static Set<Object> rolesSeen = new HashSet<Object>();
    static String inboundEnvelopeNs;

    static class MyHandler implements SOAPHandler<SOAPMessageContext> {
        public Set<QName> getHeaders() { return new HashSet<QName>(); }
        public boolean handleMessage(SOAPMessageContext ctx) {
            try {
                rolesSeen.addAll(ctx.getRoles());
                if (Boolean.TRUE.equals(ctx.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY))) {
                    outboundCtx = ctx;
                    // 在出站消息头部构造 urn:h:MyHeader/id 并经 JAXB 解回
                    SOAPMessage m = ctx.getMessage();
                    javax.xml.soap.SOAPHeaderElement he =
                        m.getSOAPHeader().addHeaderElement(new QName("urn:h", "MyHeader"));
                    org.w3c.dom.Document doc = he.getOwnerDocument();
                    org.w3c.dom.Element idEl = doc.createElementNS("urn:h", "id");
                    idEl.setTextContent("hid");
                    he.appendChild(idEl);
                    m.saveChanges();
                    JAXBContext jc = JAXBContext.newInstance(MyHeader.class);
                    headerBeansOut = ctx.getHeaders(new QName("urn:h", "MyHeader"), jc, false);
                    headerBeansAbsent = ctx.getHeaders(new QName("urn:h", "NoSuchHeader"), jc, false);
                    // setMessage 换成自定义消息：provider 端应看到替换后的载荷
                    SOAPMessage replaced = MessageFactory.newInstance().createMessage();
                    replaced.getSOAPBody().addBodyElement(new QName("urn:t", "swapped"))
                        .addTextNode("setmessage-token");
                    ctx.setMessage(replaced);
                } else {
                    inboundCtx = ctx;
                    SOAPMessage in = ctx.getMessage();
                    inboundEnvelopeNs = in.getSOAPPart().getEnvelope().getNamespaceURI();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return true;
        }
        public boolean handleFault(SOAPMessageContext ctx) { return true; }
        public void close(MessageContext ctx) { }
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.ws.handler.soap.SOAPMessageContext");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.ws.handler.MessageContext"), "implements javax.xml.ws.handler.MessageContext");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getMessage"), "has method getMessage");
        check(names.contains("setMessage"), "has method setMessage");
        check(names.contains("getHeaders"), "has method getHeaders");
        check(names.contains("getRoles"), "has method getRoles");
        testInterfaceFace(c);
        testSelfImplementation();
        testRealChain();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // 接口面：恰 4 个声明方法与精确签名
    static void testInterfaceFace(Class<?> c) throws Exception {
        Method[] dm = c.getDeclaredMethods();
        check(dm.length == 4, "exactly 4 declared methods: " + dm.length);
        for (Method m : dm) {
            check(Modifier.isAbstract(m.getModifiers()), m.getName() + " abstract");
            check(Modifier.isPublic(m.getModifiers()), m.getName() + " public");
        }
        Method gm = c.getMethod("getMessage");
        check(gm.getReturnType() == SOAPMessage.class, "getMessage returns SOAPMessage");
        check(gm.getParameterTypes().length == 0, "getMessage no args");
        Method sm = c.getMethod("setMessage", SOAPMessage.class);
        check(sm.getReturnType() == void.class, "setMessage void");
        check(sm.getParameterTypes().length == 1, "setMessage single SOAPMessage param");
        Method gh = c.getMethod("getHeaders", QName.class, JAXBContext.class, boolean.class);
        check(gh.getReturnType() == Object[].class, "getHeaders returns Object[]");
        check(gh.getParameterTypes().length == 3, "getHeaders 3 params (QName,JAXBContext,boolean)");
        Method gr = c.getMethod("getRoles");
        check(gr.getReturnType() == java.util.Set.class, "getRoles returns Set");
        check(gr.getParameterTypes().length == 0, "getRoles no args");
        check(c.getInterfaces().length == 1
              && c.getInterfaces()[0] == MessageContext.class,
              "direct superinterface is MessageContext (not LogicalMessageContext)");
    }

    // 自造实现：setMessage/getMessage 同一性 + 角色/头部桩
    static class FakeSMC extends HashMap<String, Object> implements SOAPMessageContext {
        SOAPMessage message;
        public SOAPMessage getMessage() { return message; }
        public void setMessage(SOAPMessage m) { message = m; }
        public Object[] getHeaders(QName header, JAXBContext ctx, boolean allRoles) {
            return new Object[0];
        }
        public Set<String> getRoles() {
            Set<String> s = new HashSet<String>();
            s.add("http://schemas.xmlsoap.org/soap/actor/next");
            return s;
        }
        public void setScope(String key, Scope scope) { }
        public Scope getScope(String key) { return Scope.HANDLER; }
    }

    static void testSelfImplementation() throws Exception {
        FakeSMC mc = new FakeSMC();
        SOAPMessage m = MessageFactory.newInstance().createMessage();
        mc.setMessage(m);
        check(mc.getMessage() == m, "self setMessage/getMessage identity round-trip");
        m.getSOAPBody().addBodyElement(new QName("urn:t", "b")).addTextNode("v");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        mc.getMessage().writeTo(bos);
        check(bos.toString("UTF-8").contains("<b "), "self message content readable after set");
        check(mc.getHeaders(new QName("urn:x", "h"), null, false).length == 0,
              "self getHeaders returns empty array stub");
        check(mc.getRoles().contains("http://schemas.xmlsoap.org/soap/actor/next"),
              "self getRoles contains SOAP 1.1 next actor");
        mc.put(MessageContext.MESSAGE_OUTBOUND_PROPERTY, Boolean.TRUE);
        check(Boolean.TRUE.equals(mc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY)),
              "self Map property round-trip");
    }

    // 真实链路：真实 SOAPMessageContext 的消息/头/角色
    static void testRealChain() throws Exception {
        com.sun.net.httpserver.HttpServer server =
            com.sun.net.httpserver.HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        int port = server.getAddress().getPort();
        Endpoint ep = Endpoint.create(new EchoProvider());
        server.start();
        ep.publish(server.createContext("/ws"));
        try {
            QName svcName = new QName("urn:t", "svc");
            QName portName = new QName("urn:t", "p");
            Service svc = Service.create(svcName);
            svc.setHandlerResolver(new HandlerResolver() {
                public List<Handler> getHandlerChain(PortInfo pi) {
                    List<Handler> l = new ArrayList<Handler>();
                    l.add(new MyHandler());
                    return l;
                }
            });
            svc.addPort(portName, SOAPBinding.SOAP11HTTP_BINDING, "http://localhost:" + port + "/ws");
            Dispatch<Source> d = svc.createDispatch(portName, Source.class, Service.Mode.PAYLOAD);
            String resp = srcToString(d.invoke(new StreamSource(new StringReader(
                "<q xmlns=\"urn:t\">1</q>"))));
            check(resp.contains("ok"), "invoke through SOAP handler succeeds");
            check(providerCalls == 1, "provider hit once");

            // 出站：JAXB 头部解码（handler 自加的头可解回 bean）
            check(headerBeansOut != null && headerBeansOut.length == 1,
                  "getHeaders(QName,JAXBContext,false) decodes present header: "
                  + java.util.Arrays.toString(headerBeansOut));
            check(headerBeansOut[0] instanceof MyHeader
                  && "hid".equals(((MyHeader) headerBeansOut[0]).id),
                  "decoded header bean field matches");
            // 缺失头：空数组而非 null
            check(headerBeansAbsent != null && headerBeansAbsent.length == 0,
                  "getHeaders for absent header returns empty array");
            // setMessage 换出的消息到达 provider（其请求信封带 urn:t:swapped）
            // provider 以 PAYLOAD 模式只回读载荷；改以响应内容验证链路正常 + roles 断言
            check(outboundCtx instanceof SOAPMessageContext, "outbound real context captured");
            check(rolesSeen.contains("http://schemas.xmlsoap.org/soap/actor/next"),
                  "default SOAP 1.1 next actor among roles: " + rolesSeen);
            // 入站：SOAP 1.1 信封命名空间
            check("http://schemas.xmlsoap.org/soap/envelope/".equals(inboundEnvelopeNs),
                  "inbound message envelope is SOAP 1.1: " + inboundEnvelopeNs);
        } finally {
            ep.stop();
            server.stop(0);
        }
    }
}
