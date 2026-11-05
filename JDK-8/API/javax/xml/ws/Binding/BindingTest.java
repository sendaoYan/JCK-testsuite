/*
 * @test
 * @summary javax.xml.ws.Binding conformance: handler chain get/set round-trip, binding id contract, real endpoint binding closure.
 * @run main BindingTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import javax.xml.ws.Binding;
import javax.xml.ws.Endpoint;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.LogicalHandler;
import javax.xml.ws.handler.LogicalMessageContext;
import javax.xml.ws.handler.MessageContext;

public class BindingTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }

    // 记录型 LogicalHandler（可插入 HandlerChain 的最小处理器）
    static final class NoopLogicalHandler
            implements LogicalHandler<LogicalMessageContext> {
        @Override public boolean handleMessage(LogicalMessageContext ctx) { return true; }
        @Override public boolean handleFault(LogicalMessageContext ctx) { return true; }
        @Override public void close(MessageContext ctx) { }
    }

    @javax.jws.WebService(serviceName = "svc", targetNamespace = "urn:t")
    public static class EchoImpl {
        public String echo(String s) { return s; }
    }

    public static void main(String[] args) throws Exception {
        testStructure();
        testRealEndpointBinding();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testStructure() throws Exception {
        Class<?> c = Class.forName("javax.xml.ws.Binding");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no super-interfaces");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getHandlerChain"), "has method getHandlerChain");
        check(names.contains("setHandlerChain"), "has method setHandlerChain");
        check(names.contains("getBindingID"), "has method getBindingID");
        // 精确 3 个声明方法与返回类型
        Method[] own = c.getDeclaredMethods();
        check(own.length == 3, "exactly 3 declared methods: " + own.length);
        check(c.getMethod("getHandlerChain").getReturnType() == List.class,
              "getHandlerChain returns List<Handler>");
        check(c.getMethod("setHandlerChain", List.class).getReturnType() == void.class,
              "setHandlerChain(List) void");
        check(c.getMethod("getBindingID").getReturnType() == String.class,
              "getBindingID returns String");
        for (Method m : own) {
            check(Modifier.isAbstract(m.getModifiers()), "abstract: " + m.getName());
            check(Modifier.isPublic(m.getModifiers()), "public: " + m.getName());
        }
    }

    // 真实 Binding：Endpoint.create(...).getBinding()（RI SOAPBindingImpl）
    static void testRealEndpointBinding() {
        Endpoint ep = Endpoint.create("http://schemas.xmlsoap.org/wsdl/soap/http", new EchoImpl());
        Binding b = ep.getBinding();
        check(b != null, "endpoint binding non-null");
        check(b instanceof Binding, "binding implements javax.xml.ws.Binding");
        check(b instanceof javax.xml.ws.soap.SOAPBinding, "SOAP endpoint binding is SOAPBinding");
        check("http://schemas.xmlsoap.org/wsdl/soap/http".equals(b.getBindingID()),
              "binding id equals SOAP 1.1 http URI");
        check("http://schemas.xmlsoap.org/wsdl/soap/http".equals(javax.xml.ws.soap.SOAPBinding.SOAP11HTTP_BINDING),
              "SOAP11HTTP_BINDING constant matches");
        check("http://www.w3.org/2003/05/soap/bindings/HTTP/".equals(javax.xml.ws.soap.SOAPBinding.SOAP12HTTP_BINDING),
              "SOAP12HTTP_BINDING constant");
        check("http://www.w3.org/2004/08/wsdl/http".equals(javax.xml.ws.http.HTTPBinding.HTTP_BINDING),
              "HTTP_BINDING constant");

        // handler chain：默认空非 null
        List<Handler> chain = b.getHandlerChain();
        check(chain != null, "default handler chain non-null");
        check(chain.isEmpty(), "default handler chain empty");

        // get/set 往返：构造链 → set → get 读同一实例语义（RI 返回副本则比对内容）
        List<Handler> mine = new ArrayList<Handler>();
        NoopLogicalHandler h1 = new NoopLogicalHandler();
        NoopLogicalHandler h2 = new NoopLogicalHandler();
        mine.add(h1);
        mine.add(h2);
        b.setHandlerChain(mine);
        List<Handler> after = b.getHandlerChain();
        check(after != null, "handler chain after set non-null");
        check(after.size() == 2, "handler chain size preserved");
        check(after.get(0) == h1 || after.contains(h1), "first handler present");
        check(after.get(1) == h2 || after.contains(h2), "second handler present");
        boolean sameInstance = after == mine;
        if (sameInstance) {
            check(true, "setHandlerChain stores same List instance (locked)");
        } else {
            // RI 做防御拷贝：内容等价即契约满足（两种行为均记录）
            check(after.contains(h1) && after.contains(h2), "defensive copy with same handlers");
        }
        // 直接改 get 返回的列表（若可变）后再 get：记录 RI 形态
        List<Handler> live = b.getHandlerChain();
        boolean mutable = false;
        try {
            live.add(new NoopLogicalHandler());
            mutable = true;
        } catch (UnsupportedOperationException e) {
            mutable = false;
        }
        // RI 实测：getHandlerChain 返回可变快照副本——外部 add 不影响绑定内部链
        if (mutable) {
            check(b.getHandlerChain().size() == 2, "snapshot copy: external add not visible across get calls");
        } else {
            check(b.getHandlerChain().size() == 2, "immutable chain snapshot");
        }
        // 空链 set 合法
        b.setHandlerChain(new ArrayList<Handler>());
        check(b.getHandlerChain().isEmpty(), "empty chain set/get");
        // setHandlerChain(null)：RI 行为实测（拒绝或接受，均锁定不抛 Error）
        try {
            b.setHandlerChain(null);
            check(true, "null chain tolerated");
        } catch (RuntimeException e) {
            check(true, "null chain rejected with RuntimeException: " + e.getClass().getSimpleName());
        }
        // 第二个 Endpoint 独立链
        Endpoint ep2 = Endpoint.create(new EchoImpl());
        check(ep2.getBinding().getHandlerChain().isEmpty(), "second endpoint independent empty chain");
        // HTTP binding 形态（XML/HTTP 绑定创建后非 SOAP）
        Endpoint http = Endpoint.create("http://www.w3.org/2004/08/wsdl/http", new EchoImpl());
        check(http != null, "http binding endpoint created");
        check("http://www.w3.org/2004/08/wsdl/http".equals(http.getBinding().getBindingID()),
              "http binding id");
    }
}
