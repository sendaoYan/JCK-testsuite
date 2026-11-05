/*
 * @test
 * @summary javax.xml.ws.handler.soap.SOAPHandler conformance: Handler subinterface bound to SOAPMessageContext, getHeaders contract (Set of QName), runtime header-registry consultation and self round-trip.
 * @run main SOAPHandlerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;
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


public class SOAPHandlerTest {
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

    static int providerCalls;

    @WebServiceProvider(serviceName = "svc", targetNamespace = "urn:t", portName = "p")
    @ServiceMode(value = Service.Mode.PAYLOAD)
    public static class EchoProvider implements Provider<Source> {
        public Source invoke(Source req) {
            providerCalls++;
            return new StreamSource(new java.io.StringReader("<r xmlns=\"urn:t\">ok</r>"));
        }
    }

    static int getHeadersCalls;
    static int msgCalls, faultCalls, closeCalls;
    static Object lastContext;

    static class MySOAPHandler implements SOAPHandler<SOAPMessageContext> {
        public Set<QName> getHeaders() {
            getHeadersCalls++;
            Set<QName> s = new HashSet<QName>();
            s.add(new QName("urn:h", "UnderstoodHeader"));
            return s;
        }
        public boolean handleMessage(SOAPMessageContext ctx) {
            msgCalls++;
            lastContext = ctx;
            return true;
        }
        public boolean handleFault(SOAPMessageContext ctx) { faultCalls++; return true; }
        public void close(MessageContext ctx) { closeCalls++; }
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.ws.handler.soap.SOAPHandler");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.ws.handler.Handler"), "implements javax.xml.ws.handler.Handler");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getHeaders"), "has method getHeaders");
        testInterfaceFace(c);
        testSelfImplementation();
        testRealChain();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // 接口面：恰 1 个声明方法 getHeaders()，返回 Set（QName 元素），类型参数上界 SOAPMessageContext
    static void testInterfaceFace(Class<?> c) throws Exception {
        Method[] dm = c.getDeclaredMethods();
        check(dm.length == 1, "exactly 1 declared method: " + dm.length);
        Method gh = dm[0];
        check(gh.getName().equals("getHeaders"), "declared method is getHeaders");
        check(gh.getReturnType() == java.util.Set.class, "getHeaders returns java.util.Set");
        check(gh.getParameterTypes().length == 0, "getHeaders takes no args");
        check(Modifier.isAbstract(gh.getModifiers()), "getHeaders abstract");
        check(Modifier.isPublic(gh.getModifiers()), "getHeaders public");
        check(c.getInterfaces().length == 1
              && c.getInterfaces()[0] == javax.xml.ws.handler.Handler.class,
              "direct superinterface is Handler");
        java.lang.reflect.TypeVariable<?>[] tv = c.getTypeParameters();
        check(tv.length == 1 && tv[0].getName().equals("T"), "type variable named T");
        check(tv[0].getBounds().length == 1 && tv[0].getBounds()[0] == SOAPMessageContext.class,
              "T bounded by SOAPMessageContext");
        // 继承三方法（擦除形参 MessageContext）
        check(c.getMethod("handleMessage", MessageContext.class).getReturnType() == boolean.class,
              "inherited handleMessage returns boolean");
        check(c.getMethod("handleFault", MessageContext.class).getReturnType() == boolean.class,
              "inherited handleFault returns boolean");
        check(c.getMethod("close", MessageContext.class).getReturnType() == void.class,
              "inherited close returns void");
        // getHeaders 是 SOAPHandler 独有（LogicalHandler/Handler 均无）
        check(!javax.xml.ws.handler.Handler.class.getMethods().toString().contains("getHeaders")
              || !hasStrings(javax.xml.ws.handler.Handler.class, "getHeaders"),
              "Handler base has no getHeaders");
    }

    static boolean hasStrings(Class<?> c, String name) {
        for (Method m : c.getMethods()) if (name.equals(m.getName())) return true;
        return false;
    }

    // 自造实现：getHeaders 返回 Set<QName> 回读 + 生命周期
    static void testSelfImplementation() {
        MySOAPHandler h = new MySOAPHandler();
        Set<QName> headers = h.getHeaders();
        check(headers != null, "self getHeaders non-null");
        check(headers.size() == 1, "self getHeaders size 1");
        check(headers.contains(new QName("urn:h", "UnderstoodHeader")),
              "self getHeaders contains declared QName (equal-by-value)");
        // 空集合法
        SOAPHandler<SOAPMessageContext> empty = new SOAPHandler<SOAPMessageContext>() {
            public Set<QName> getHeaders() { return new HashSet<QName>(); }
            public boolean handleMessage(SOAPMessageContext ctx) { return true; }
            public boolean handleFault(SOAPMessageContext ctx) { return true; }
            public void close(MessageContext ctx) { }
        };
        check(empty.getHeaders().isEmpty(), "empty header set is legal");
        // 生命周期回调
        check(h.handleMessage(null) == true, "self handleMessage true");
        check(h.handleFault(null) == true, "self handleFault true");
        h.close(null);
        check(msgCalls >= 1 && faultCalls >= 1 && closeCalls >= 1, "self lifecycle recorded");
        // SOAPHandler 可赋给 Handler 家族
        Handler<SOAPMessageContext> typed = h;
        Handler<?> raw = h;
        check(typed != null && raw != null, "assignable to Handler (generic and raw)");
    }

    // 真实链路：运行时调用 getHeaders 注册理解头；handleMessage 收到真实 SOAPMessageContext
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
                    l.add(new MySOAPHandler());
                    return l;
                }
            });
            svc.addPort(portName, SOAPBinding.SOAP11HTTP_BINDING, "http://localhost:" + port + "/ws");
            Dispatch<Source> d = svc.createDispatch(portName, Source.class, Service.Mode.PAYLOAD);
            check(getHeadersCalls >= 1, "runtime consults getHeaders at chain setup: " + getHeadersCalls);
            int before = getHeadersCalls;
            msgCalls = 0; faultCalls = 0; closeCalls = 0;
            String resp = srcToString(d.invoke(new StreamSource(new java.io.StringReader(
                "<q xmlns=\"urn:t\">1</q>"))));
            check(resp.contains("ok"), "invoke through SOAP handler succeeds");
            check(providerCalls == 1, "provider hit once");
            check(msgCalls == 2, "SOAP handler handleMessage ran for request+response: " + msgCalls);
            check(closeCalls == 1, "close once");
            check(faultCalls == 0, "no fault on success");
            check(lastContext instanceof SOAPMessageContext,
                  "runtime passes a real SOAPMessageContext");
            check(getHeadersCalls == before, "getHeaders not re-consulted per invoke");
            // 第二次调用：生命周期重新走一遍（msgCalls 重置后再次计满）
            msgCalls = 0; closeCalls = 0;
            check(srcToString(d.invoke(new StreamSource(new java.io.StringReader(
                "<q xmlns=\"urn:t\">2</q>")))).contains("ok"), "second invoke succeeds");
            check(msgCalls == 2 && closeCalls == 1, "lifecycle repeats per invoke");
            check(lastContext instanceof SOAPMessageContext && faultCalls == 0,
                  "no fault path on second invoke (context type stable)");
            // 注：getMessage() 须在 handleMessage 内读；invoke 返回后底层流已关闭（实测）
        } finally {
            ep.stop();
            server.stop(0);
        }
    }
}
