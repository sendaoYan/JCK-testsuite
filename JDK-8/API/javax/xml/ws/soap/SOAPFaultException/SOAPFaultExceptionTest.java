/*
 * @test
 * @summary javax.xml.ws.soap.SOAPFaultException conformance: hierarchy, single SOAPFault constructor, fault identity carry, null constructor negative and server fault propagation through a live endpoint.
 * @run main SOAPFaultExceptionTest
 */

import java.io.StringReader;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPFault;
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
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.PortInfo;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.ws.soap.SOAPFaultException;

public class SOAPFaultExceptionTest {
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

    @WebServiceProvider(serviceName = "svc", targetNamespace = "urn:t", portName = "p")
    @ServiceMode(value = Service.Mode.PAYLOAD)
    public static class FaultProvider implements Provider<Source> {
        public Source invoke(Source req) {
            throw new RuntimeException("boom");
        }
    }

    public static void main(String[] args) throws Exception {
        check(javax.xml.ws.soap.SOAPFaultException.class.getSuperclass().getName().equals("javax.xml.ws.ProtocolException"), "extends javax.xml.ws.ProtocolException");
        javax.xml.soap.MessageFactory mf = javax.xml.soap.MessageFactory.newInstance();
        javax.xml.soap.SOAPFault fault = mf.createMessage().getSOAPBody().addFault();
        javax.xml.ws.soap.SOAPFaultException fe = new javax.xml.ws.soap.SOAPFaultException(fault);
        check(fe.getFault() == fault, "fault");
        testHierarchy();
        testConstructorFace();
        testFaultCarry();
        testNullNegative();
        testServerFaultChannel();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // 层级：ProtocolException -> WebServiceException -> RuntimeException
    static void testHierarchy() {
        check(SOAPFaultException.class.getSuperclass() == javax.xml.ws.ProtocolException.class,
              "direct superclass ProtocolException");
        check(WebServiceException.class.isAssignableFrom(SOAPFaultException.class),
              "assignable to WebServiceException");
        check(RuntimeException.class.isAssignableFrom(SOAPFaultException.class),
              "assignable to RuntimeException");
        check(!javax.xml.ws.http.HTTPException.class.isAssignableFrom(SOAPFaultException.class),
              "not related to HTTPException (sibling)");
    }

    // 构造面：仅 (SOAPFault) 一个公共构造器
    static void testConstructorFace() throws Exception {
        java.lang.reflect.Constructor<?>[] ctors = SOAPFaultException.class.getConstructors();
        check(ctors.length == 1, "exactly 1 public constructor: " + ctors.length);
        check(SOAPFaultException.class.getConstructor(javax.xml.soap.SOAPFault.class) != null,
              "(SOAPFault) ctor exists");
        try {
            SOAPFaultException.class.getConstructor();
            check(false, "no no-arg ctor");
        } catch (NoSuchMethodException e) {
            check(true, "no no-arg ctor");
        }
        try {
            SOAPFaultException.class.getConstructor(String.class, SOAPFault.class);
            check(false, "no (String,SOAPFault) ctor");
        } catch (NoSuchMethodException e) {
            check(true, "no (String,SOAPFault) ctor");
        }
        // getFault 签名
        Method gf = SOAPFaultException.class.getMethod("getFault");
        check(gf.getReturnType() == SOAPFault.class, "getFault returns SOAPFault");
        check(gf.getParameterTypes().length == 0, "getFault no args");
    }

    // fault 携带：同一实例、code/reason 往返、构造后 fault 可再修改
    static void testFaultCarry() throws Exception {
        MessageFactory mf = MessageFactory.newInstance();
        SOAPFault f1 = mf.createMessage().getSOAPBody().addFault(
            new QName("urn:t", "MyCode"), "reason-text", Locale.ROOT);
        SOAPFaultException e1 = new SOAPFaultException(f1);
        check(e1.getFault() == f1, "fault identity preserved");
        check(e1.getFault() == e1.getFault(), "getFault stable (same instance)");
        check("reason-text".equals(e1.getFault().getFaultString()), "fault reason round-trip");
        check(e1.getFault().getFaultCodeAsQName().getLocalPart().equals("MyCode"),
              "fault code local part round-trip");
        check(e1.getFault().getFaultCodeAsQName().getNamespaceURI().equals("urn:t"),
              "fault code namespace round-trip");
        // 默认 addFault()：code 默认 Server、reason 缺省
        SOAPFault f2 = mf.createMessage().getSOAPBody().addFault();
        SOAPFaultException e2 = new SOAPFaultException(f2);
        check(e2.getFault().getFaultCodeAsName() != null, "default fault has code Name");
        check(e2.getFault().getFaultString() != null || e2.getMessage() != null,
              "default fault string or exception message present (recorded)");
        check(e2.getMessage() != null, "exception message derived from fault (non-null)");
        // 构造后对 fault 的修改可见
        e2.getFault().setFaultString("changed", Locale.ROOT);
        check("changed".equals(e2.getFault().getFaultString()), "fault mutable through getFault");
        // hasDetail 初始无 detail
        check(!e1.getFault().hasDetail(), "no detail by default");
        e1.getFault().addDetail();
        check(e1.getFault().hasDetail(), "detail addable");
        // fault actor/role 与 detail 条目写入回读（SOAP 1.1 actor）
        e1.getFault().setFaultActor("urn:t:actor");
        check("urn:t:actor".equals(e1.getFault().getFaultActor()), "fault actor round-trip");
        javax.xml.soap.Detail d = e1.getFault().getDetail();
        javax.xml.soap.DetailEntry de = d.addDetailEntry(new QName("urn:t", "entry"));
        de.addTextNode("payload");
        java.util.Iterator<?> it = d.getDetailEntries();
        check(it.hasNext(), "detail entry retrievable");
        check(e1.getFault().getFaultString().equals("reason-text"), "reason unchanged by detail ops");
        // 两个异常携带同一 fault 实例时 getFault 各自返回该实例
        SOAPFaultException e3 = new SOAPFaultException(f1);
        check(e3.getFault() == f1 && e1.getFault() == f1, "shared fault instance across exceptions");
    }

    // 负例：null 构造 -> NullPointerException（实测）
    static void testNullNegative() throws Exception {
        try {
            new SOAPFaultException(null);
            check(false, "null fault must throw");
        } catch (NullPointerException npe) {
            check(true, "null fault -> NullPointerException");
        }
        // getCause 默认 null（fault 本身即载荷，非 cause 链）
        check(new SOAPFaultException(MessageFactory.newInstance().createMessage()
              .getSOAPBody().addFault()).getCause() == null,
              "getCause null by default");
    }

    // 真实通道：服务端抛错经 SOAP 故障传回，客户端异常为 SOAPFaultException 携真实 fault
    static void testServerFaultChannel() throws Exception {
        com.sun.net.httpserver.HttpServer server =
            com.sun.net.httpserver.HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        int port = server.getAddress().getPort();
        Endpoint ep = Endpoint.create(new FaultProvider());
        server.start();
        ep.publish(server.createContext("/ws"));
        try {
            QName svcName = new QName("urn:t", "svc");
            QName portName = new QName("urn:t", "p");
            Service svc = Service.create(svcName);
            svc.setHandlerResolver(new HandlerResolver() {
                public List<Handler> getHandlerChain(PortInfo pi) {
                    return new ArrayList<Handler>();
                }
            });
            svc.addPort(portName, SOAPBinding.SOAP11HTTP_BINDING, "http://localhost:" + port + "/ws");
            Dispatch<Source> d = svc.createDispatch(portName, Source.class, Service.Mode.PAYLOAD);
            try {
                d.invoke(new StreamSource(new StringReader("<q xmlns=\"urn:t\">x</q>")));
                check(false, "server fault must reach client");
            } catch (SOAPFaultException sfe) {
                check(true, "server fault surfaces as SOAPFaultException");
                check(sfe.getFault() != null, "carries a real SOAPFault");
                check(sfe.getFault().getFaultString() != null
                      && sfe.getFault().getFaultString().contains("boom"),
                      "fault string contains server cause: " + sfe.getFault().getFaultString());
                check(sfe.getFault().getFaultCodeAsName() != null,
                      "fault code present: " + sfe.getFault().getFaultCodeAsName());
                check(sfe.getMessage() != null, "exception message non-null");
                check(sfe instanceof javax.xml.ws.ProtocolException,
                      "still a ProtocolException at runtime");
            }
        } finally {
            ep.stop();
            server.stop(0);
        }
    }
}
