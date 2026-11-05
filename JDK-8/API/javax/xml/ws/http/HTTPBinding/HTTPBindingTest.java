/*
 * @test
 * @summary javax.xml.ws.http.HTTPBinding conformance: HTTP_BINDING URI constant, Binding inheritance face, live XML/HTTP dispatch over loopback echoing raw payload.
 * @run main HTTPBindingTest
 */

import java.io.OutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.InetSocketAddress;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.http.HTTPBinding;


public class HTTPBindingTest {
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

    static String serverSawBody;

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.ws.http.HTTPBinding");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.ws.Binding"), "implements javax.xml.ws.Binding");
        testConstants(c);
        testInterfaceFace(c);
        testLiveDispatch();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // 常量：唯一绑定 ID 串全等（2004/08 WSDL 2.0 HTTP 命名空间）
    static void testConstants(Class<?> c) throws Exception {
        check(HTTPBinding.HTTP_BINDING.equals("http://www.w3.org/2004/08/wsdl/http"),
              "HTTP_BINDING URI exact: " + HTTPBinding.HTTP_BINDING);
        java.lang.reflect.Field f = c.getField("HTTP_BINDING");
        check(Modifier.isPublic(f.getModifiers()) && Modifier.isStatic(f.getModifiers())
              && Modifier.isFinal(f.getModifiers()), "HTTP_BINDING public static final");
        check(f.getType() == String.class, "HTTP_BINDING is String");
        // 恰 1 个字段
        check(c.getDeclaredFields().length == 1, "exactly 1 declared field");
        // 与 SOAP 绑定 ID 互异
        check(!HTTPBinding.HTTP_BINDING.equals(javax.xml.ws.soap.SOAPBinding.SOAP11HTTP_BINDING),
              "distinct from SOAP 1.1 binding id");
        check(!HTTPBinding.HTTP_BINDING.equals(javax.xml.ws.soap.SOAPBinding.SOAP12HTTP_BINDING),
              "distinct from SOAP 1.2 binding id");
        check(!HTTPBinding.HTTP_BINDING.equals(
                  javax.xml.ws.soap.SOAPBinding.SOAP11HTTP_MTOM_BINDING),
              "distinct from SOAP 1.1 MTOM binding id");
        // 命名空间形态：2004/08（WSDL 2.0 期 HTTP 绑定），非 2003/05 SOAP HTTP 形态
        check(HTTPBinding.HTTP_BINDING.startsWith("http://www.w3.org/2004/08/wsdl/"),
              "2004/08 wsdl namespace prefix");
    }

    // 接口面：自身零方法，继承 Binding 三方法
    static void testInterfaceFace(Class<?> c) throws Exception {
        check(c.getDeclaredMethods().length == 0, "no own methods (marker binding interface)");
        Method gh = Binding.class.getMethod("getHandlerChain");
        check(gh.getReturnType() == java.util.List.class, "inherited getHandlerChain returns List");
        check(Modifier.isAbstract(gh.getModifiers()), "getHandlerChain abstract on Binding");
        check(Binding.class.getMethod("setHandlerChain", java.util.List.class).getReturnType() == void.class,
              "inherited setHandlerChain void");
        check(Binding.class.getMethod("getBindingID").getReturnType() == String.class,
              "inherited getBindingID returns String");
        // 三方法在 HTTPBinding 上可见
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getHandlerChain") && names.contains("setHandlerChain")
              && names.contains("getBindingID"), "all Binding methods visible on HTTPBinding");
        // Binding 可赋值
        check(Binding.class.isAssignableFrom(c), "assignable to Binding");
    }

    // 真实通道：XML/HTTP 绑定 Dispatch 在回环上裸传 XML（无 SOAP 信封）
    static void testLiveDispatch() throws Exception {
        com.sun.net.httpserver.HttpServer server =
            com.sun.net.httpserver.HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        final byte[] reply = "<r xmlns=\"urn:t\">ok</r>".getBytes("UTF-8");
        server.createContext("/xml", new com.sun.net.httpserver.HttpHandler() {
            public void handle(com.sun.net.httpserver.HttpExchange ex) throws java.io.IOException {
                byte[] req = new byte[8192];
                int n = ex.getRequestBody().read(req);
                serverSawBody = new String(req, 0, n, "UTF-8");
                ex.getResponseHeaders().set("Content-Type", "application/xml");
                ex.sendResponseHeaders(200, reply.length);
                OutputStream os = ex.getResponseBody();
                os.write(reply);
                os.close();
            }
        });
        server.start();
        int port = server.getAddress().getPort();
        try {
            javax.xml.namespace.QName svcName = new javax.xml.namespace.QName("urn:x", "xsvc");
            javax.xml.namespace.QName portName = new javax.xml.namespace.QName("urn:x", "xp");
            Service svc = Service.create(svcName);
            svc.addPort(portName, HTTPBinding.HTTP_BINDING, "http://localhost:" + port + "/xml");
            Dispatch<Source> d = svc.createDispatch(portName, Source.class, Service.Mode.PAYLOAD);
            Binding b = d.getBinding();
            check(b instanceof HTTPBinding, "dispatch binding is HTTPBinding: " + b.getClass().getName());
            check(HTTPBinding.HTTP_BINDING.equals(b.getBindingID()),
                  "live binding id equals HTTP_BINDING: " + b.getBindingID());
            check(b.getHandlerChain() != null, "handler chain list available (empty by default)");
            String resp = srcToString(d.invoke(new StreamSource(new java.io.StringReader(
                "<q xmlns=\"urn:x\">payload</q>"))));
            check(resp.contains("ok"), "raw XML response round-trips: " + resp);
            check(serverSawBody != null && serverSawBody.contains("<q xmlns=\"urn:x\">payload</q>"),
                  "server received unwrapped XML payload: " + serverSawBody);
            check(!serverSawBody.contains("Envelope"), "no SOAP envelope on the wire for HTTP binding");
            // Dispatch 亦是 BindingProvider
            check(d instanceof BindingProvider, "dispatch is a BindingProvider");
            d.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                "http://localhost:" + port + "/xml");
            check(d.getRequestContext().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY) != null,
                  "request context address configurable");
            // HTTP 绑定上自定义 handler 链可装配且可回读
            javax.xml.ws.handler.LogicalHandler<javax.xml.ws.handler.LogicalMessageContext> lh =
                new javax.xml.ws.handler.LogicalHandler<javax.xml.ws.handler.LogicalMessageContext>() {
                    public boolean handleMessage(javax.xml.ws.handler.LogicalMessageContext ctx) { return true; }
                    public boolean handleFault(javax.xml.ws.handler.LogicalMessageContext ctx) { return true; }
                    public void close(javax.xml.ws.handler.MessageContext ctx) { }
                };
            java.util.List<javax.xml.ws.handler.Handler> chain =
                new java.util.ArrayList<javax.xml.ws.handler.Handler>();
            chain.add(lh);
            b.setHandlerChain(chain);
            check(b.getHandlerChain().size() == 1, "custom handler chain installed");
            check(b.getHandlerChain().get(0) == lh, "handler identity round-trip");
            // 装配后仍可正常裸传
            check(srcToString(d.invoke(new StreamSource(new java.io.StringReader(
                "<q xmlns=\"urn:x\">again</q>")))).contains("ok"), "invoke after chain install");
            // setHandlerChain(null) 清空（实测容忍）
            b.setHandlerChain(new java.util.ArrayList<javax.xml.ws.handler.Handler>());
            check(b.getHandlerChain().isEmpty(), "chain clearable");
            check(srcToString(d.invoke(new StreamSource(new java.io.StringReader(
                "<q xmlns=\"urn:x\">third</q>")))).contains("ok"), "invoke after chain clear");
            check(serverSawBody.contains("third"), "server saw latest body: " + serverSawBody);
            // 第二端口同绑定 ID 并存
            javax.xml.namespace.QName port2 = new javax.xml.namespace.QName("urn:x", "xp2");
            svc.addPort(port2, HTTPBinding.HTTP_BINDING, "http://localhost:" + port + "/xml");
            Dispatch<Source> d2 = svc.createDispatch(port2, Source.class, Service.Mode.PAYLOAD);
            check(d2.getBinding() instanceof HTTPBinding, "second port same HTTP binding");
            check(HTTPBinding.HTTP_BINDING.equals(d2.getBinding().getBindingID()),
                  "second port binding id");
            check(srcToString(d2.invoke(new StreamSource(new java.io.StringReader(
                "<q2 xmlns=\"urn:x\">via2</q2>")))).contains("ok"), "second port invoke succeeds");
            // 响应上下文可读（HTTP 语义在 MessageContext 上暴露）
            java.util.Map<String, Object> rc = d.getResponseContext();
            check(rc != null, "response context available on dispatch");
            Object code = rc.get(javax.xml.ws.handler.MessageContext.HTTP_RESPONSE_CODE);
            check(code != null && Integer.valueOf(200).equals(code),
                  "HTTP_RESPONSE_CODE 200 exposed: " + code);
        } finally {
            server.stop(0);
        }
    }
}
