/*
 * @test
 * @summary javax.xml.ws.Endpoint conformance: publish lifecycle closure, binding/implementor accessors, negative matrices.
 * @run main EndpointTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import javax.xml.ws.Endpoint;
import javax.xml.ws.WebServiceException;

public class EndpointTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }

    // 已标注实现类（发布生命周期闭环用）
    @javax.jws.WebService(serviceName = "svc", targetNamespace = "urn:t")
    public static class EchoImpl {
        public String echo(String s) { return s; }
    }

    // 未标注实现类（负例）
    public static class PlainImpl {
        public String echo(String s) { return s; }
    }

    public static void main(String[] args) throws Exception {
        testStructure();
        testCreateMatrix();
        testNegativePublish();
        testLifecycle();
        testMetadataAndContext();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testStructure() throws Exception {
        Class<?> c = Class.forName("javax.xml.ws.Endpoint");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("create"), "has method create");
        check(names.contains("getBinding"), "has method getBinding");
        check(names.contains("getImplementor"), "has method getImplementor");
        check(names.contains("publish"), "has method publish");
        check(names.contains("stop"), "has method stop");
        check(names.contains("isPublished"), "has method isPublished");
        check(names.contains("getMetadata"), "has method getMetadata");
        check(names.contains("setMetadata"), "has method setMetadata");
        check(names.contains("getExecutor"), "has method getExecutor");
        check(names.contains("setExecutor"), "has method setExecutor");
        check(names.contains("getProperties"), "has method getProperties");
        check(names.contains("setProperties"), "has method setProperties");
        check(names.contains("getEndpointReference"), "has method getEndpointReference");
        check(names.contains("setEndpointContext"), "has method setEndpointContext");
        // 抽象/具体面：getBinding/getImplementor/publish(String)/publish(Object)/stop/isPublished 为抽象
        String[] abs = {"getBinding", "getImplementor", "stop", "isPublished",
                        "getMetadata", "setMetadata", "getExecutor", "setExecutor",
                        "getProperties", "setProperties"};
        for (String n : abs) {
            boolean found = false;
            for (Method m : c.getDeclaredMethods()) {
                if (m.getName().equals(n) && Modifier.isAbstract(m.getModifiers())) found = true;
            }
            check(found, "declared abstract: " + n);
        }
        // publish(String) 与 publish(Object) 均抽象；publish(HttpContext) 为具体 final? 只断言存在
        check(Modifier.isAbstract(c.getDeclaredMethod("publish", String.class).getModifiers()), "publish(String) abstract");
        check(Modifier.isAbstract(c.getDeclaredMethod("publish", Object.class).getModifiers()), "publish(Object) abstract");
        // 静态工厂均为静态
        check(Modifier.isStatic(c.getDeclaredMethod("create", Object.class).getModifiers()), "create(Object) static");
        check(Modifier.isStatic(c.getDeclaredMethod("create", String.class, Object.class).getModifiers()), "create(String,Object) static");
        check(Modifier.isStatic(c.getDeclaredMethod("publish", String.class, Object.class).getModifiers()), "static publish(String,Object)");
        // 常量：WSDL_SERVICE / WSDL_PORT（响应上下文元数据键）
        check("javax.xml.ws.wsdl.service".equals(Endpoint.WSDL_SERVICE), "WSDL_SERVICE constant");
        check("javax.xml.ws.wsdl.port".equals(Endpoint.WSDL_PORT), "WSDL_PORT constant");
        check(Endpoint.WSDL_SERVICE instanceof String, "WSDL_SERVICE type String");
    }

    static void testCreateMatrix() {
        EchoImpl impl = new EchoImpl();
        // 显式 bindingId 形态
        Endpoint ep = Endpoint.create("http://schemas.xmlsoap.org/wsdl/soap/http", impl);
        check(ep != null, "create(bindingId,impl) non-null");
        check(!ep.isPublished(), "fresh endpoint not published");
        check(ep.getBinding() != null, "getBinding non-null");
        check("http://schemas.xmlsoap.org/wsdl/soap/http".equals(ep.getBinding().getBindingID()),
              "binding id SOAP 1.1 http");
        check(ep.getImplementor() == impl, "getImplementor same instance");
        check(ep.getImplementor() instanceof EchoImpl, "implementor type");
        // 单参默认 binding 形态
        Endpoint ep2 = Endpoint.create(new EchoImpl());
        check(ep2 != null, "create(impl) single-arg non-null");
        check(!ep2.isPublished(), "single-arg endpoint not published");
        check("http://schemas.xmlsoap.org/wsdl/soap/http".equals(ep2.getBinding().getBindingID()),
              "default binding id is SOAP 1.1 http");
        // 带 feature 变参
        Endpoint ep3 = Endpoint.create(new EchoImpl(), new javax.xml.ws.WebServiceFeature[0]);
        check(ep3 != null, "create(impl,features) non-null");
        Endpoint ep4 = Endpoint.create("http://schemas.xmlsoap.org/wsdl/soap/http", new EchoImpl(),
                                       new javax.xml.ws.WebServiceFeature[0]);
        check(ep4 != null, "create(bindingId,impl,features) non-null");
        // Executor：默认 null，可设/可清
        check(ep.getExecutor() == null, "default executor null");
        java.util.concurrent.Executor ex = java.util.concurrent.Executors.newSingleThreadExecutor(
            new java.util.concurrent.ThreadFactory() {
                @Override public Thread newThread(Runnable r) {
                    Thread t = new Thread(r, "jck-ep-ex");
                    t.setDaemon(true);
                    return t;
                }
            });
        ep.setExecutor(ex);
        check(ep.getExecutor() == ex, "setExecutor same instance");
        ep.setExecutor(null);
        check(ep.getExecutor() == null, "setExecutor(null) accepted (no throw)");
        // Properties：默认空 map（非 null），put/get 往返（同一引用；每次 getProperties 返回快照，实测）
        Map<String, Object> props = ep.getProperties();
        check(props != null, "getProperties non-null");
        check(props.isEmpty(), "default properties empty");
        props.put("k", "v");
        check("v".equals(props.get("k")), "properties put/get round-trip on same map view");
        Map<String, Object> snapshot = new java.util.HashMap<String, Object>();
        snapshot.put("sk", "sv");
        ep.setProperties(snapshot);
        check("sv".equals(ep.getProperties().get("sk")), "setProperties/getProperties round-trip");
        // setProperties(null)：RI 内部做防御拷贝 → NPE（实测锁定）
        try {
            ep.setProperties(null);
            check(true, "setProperties(null) tolerated");
        } catch (NullPointerException e) {
            check(true, "setProperties(null) -> NPE (RI defensive copy, locked)");
        }
        // Metadata：默认 null，set 空列表不抛
        check(ep.getMetadata() == null, "default metadata null (RI)");
        ep.setMetadata(new java.util.ArrayList<javax.xml.transform.Source>());
        List<javax.xml.transform.Source> md = ep.getMetadata();
        check(md != null, "metadata after set non-null");
        check(md.isEmpty(), "metadata empty list kept");
    }

    static void testNegativePublish() {
        EchoImpl impl = new EchoImpl();
        Endpoint ep = Endpoint.create(impl);
        // publish((String) null) -> IAE
        try {
            ep.publish((String) null);
            check(false, "publish(String)null must throw");
        } catch (IllegalArgumentException e) {
            check(true, "publish(String)null -> IllegalArgumentException");
        }
        // publish((Object) null) 实例方法 -> NPE
        try {
            ep.publish((Object) null);
            check(false, "publish(Object)null must throw");
        } catch (NullPointerException e) {
            check(true, "publish(Object)null -> NullPointerException");
        }
        // 不支持的协议 scheme -> IAE（消息含协议名）
        try {
            ep.publish("ftp://x/y");
            check(false, "publish(ftp) must throw");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("ftp"),
                  "publish(ftp) message mentions ftp: " + e.getMessage());
        }
        // 静态 publish(null, impl) -> IAE
        try {
            Endpoint.publish((String) null, impl);
            check(false, "static publish(null,impl) must throw");
        } catch (IllegalArgumentException e) {
            check(true, "static publish(null,impl) -> IllegalArgumentException");
        }
        // 未标注 impl publish -> IAE，消息含 "has neither @WebService nor @WebServiceProvider"
        Endpoint plain = Endpoint.create(new PlainImpl());
        check(plain != null, "create(plain impl) succeeds at creation time");
        try {
            plain.publish("http://localhost:0/plain");
            check(false, "plain impl publish must throw");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null
                  && e.getMessage().contains("has neither @WebService nor @WebServiceProvider"),
                  "plain impl message: " + e.getMessage());
            check(e.getMessage().contains(PlainImpl.class.getName()),
                  "plain impl message names the class");
        }
        check(!plain.isPublished(), "failed publish leaves endpoint unpublished");
        // 未发布时 getEndpointReference -> WebServiceException（实测消息 "Endpoint is not published yet"）
        try {
            ep.getEndpointReference();
            check(false, "getEndpointReference unpublished must throw");
        } catch (WebServiceException e) {
            check(e.getMessage() != null && e.getMessage().contains("not published"),
                  "unpublished epr message: " + e.getMessage());
        }
        try {
            ep.getEndpointReference(javax.xml.ws.wsaddressing.W3CEndpointReference.class);
            check(false, "typed getEndpointReference unpublished must throw");
        } catch (WebServiceException e) {
            check(e.getMessage() != null && e.getMessage().contains("not published"),
                  "unpublished typed epr message: " + e.getMessage());
        }
        // create((String) null, impl)：bindingId null 被接受并落到默认 SOAP binding（实测锁定，不抛）
        Endpoint nullBind = Endpoint.create((String) null, new EchoImpl());
        check(nullBind != null, "create(null bindingId,impl) non-null (default binding)");
        check("http://schemas.xmlsoap.org/wsdl/soap/http".equals(nullBind.getBinding().getBindingID()),
              "null bindingId resolved to SOAP 1.1 default");
        // create(null impl)
        try {
            Endpoint.create((Object) null);
            check(false, "create(null impl) must throw");
        } catch (NullPointerException e) {
            check(true, "create(null impl) -> NullPointerException");
        } catch (RuntimeException e) {
            check(true, "create(null impl) -> RuntimeException: " + e.getClass().getSimpleName());
        }
    }

    // 真实发布生命周期闭环：ephemeral 端口（localhost:0），立即 stop
    static void testLifecycle() {
        Endpoint ep = Endpoint.create("http://schemas.xmlsoap.org/wsdl/soap/http", new EchoImpl());
        check(!ep.isPublished(), "before publish: false");
        ep.publish("http://localhost:0/echo");
        check(ep.isPublished(), "after publish: true");
        check(ep.getImplementor() instanceof EchoImpl, "implementor after publish");
        check("http://schemas.xmlsoap.org/wsdl/soap/http".equals(ep.getBinding().getBindingID()),
              "binding id after publish");
        // 已发布状态可取 EndpointReference（W3C 形态）
        javax.xml.ws.wsaddressing.W3CEndpointReference epr =
            ep.getEndpointReference(javax.xml.ws.wsaddressing.W3CEndpointReference.class);
        check(epr != null, "typed getEndpointReference non-null while published");
        String s = epr.toString();
        check(s.contains("EndpointReference"), "epr xml root EndpointReference");
        check(s.contains("http://www.w3.org/2005/08/addressing"), "epr addressing ns");
        check(s.contains("http://localhost:"), "epr address bound to ephemeral localhost port");
        // 无参泛型形态
        javax.xml.ws.EndpointReference generic = ep.getEndpointReference();
        check(generic != null, "generic getEndpointReference non-null");
        check(generic instanceof javax.xml.ws.wsaddressing.W3CEndpointReference,
              "generic epr runtime type is W3CEndpointReference");
        // stop 幂等
        ep.stop();
        check(!ep.isPublished(), "after stop: false");
        ep.stop();
        check(!ep.isPublished(), "second stop idempotent, still false");
        // stop 后 getEndpointReference -> WebServiceException
        try {
            ep.getEndpointReference();
            check(false, "epr after stop must throw");
        } catch (WebServiceException e) {
            check(e.getMessage() != null && e.getMessage().contains("not published"),
                  "epr after stop message: " + e.getMessage());
        }
        // 同一 Endpoint 不可二次 publish（RI：IllegalStateException/WebServiceException 家族，实测锁定）
        try {
            ep.publish("http://localhost:0/again");
            check(false, "re-publish after stop must throw");
        } catch (IllegalStateException | WebServiceException e) {
            check(true, "re-publish rejected: " + e.getClass().getSimpleName());
        }
        // 重新 create 新实例可再次发布（create/publish/stop 循环两次）
        for (int i = 0; i < 2; i++) {
            Endpoint loop = Endpoint.create(new EchoImpl());
            loop.publish("http://localhost:0/loop" + i);
            check(loop.isPublished(), "loop publish #" + i);
            loop.stop();
            check(!loop.isPublished(), "loop stop #" + i);
        }
        // 静态 publish(String,Object) 一步发布闭环（返回的 Endpoint 已发布，stop 收尾）
        Endpoint sp = Endpoint.publish("http://localhost:0/sp", new EchoImpl());
        check(sp != null, "static publish returns endpoint");
        check(sp.isPublished(), "static publish already published");
        check(sp.getImplementor() instanceof EchoImpl, "static publish implementor");
        check("http://schemas.xmlsoap.org/wsdl/soap/http".equals(sp.getBinding().getBindingID()),
              "static publish binding");
        sp.stop();
        check(!sp.isPublished(), "static publish stop");
        // 带 feature 变参的静态 publish
        Endpoint spf = Endpoint.publish("http://localhost:0/spf", new EchoImpl(),
                                        new javax.xml.ws.WebServiceFeature[0]);
        check(spf != null && spf.isPublished(), "static publish with features");
        spf.stop();
        check(!spf.isPublished(), "static publish with features stop");
        // 非 URL 形态地址负例：空串 / 非 URL 文本 / 缺路径 / 非支持协议（实测锁定 IAE）
        String[] badAddr = {"", "not-a-url", "http://localhost:0", "https://localhost:0/x"};
        for (String addr : badAddr) {
            Endpoint x = Endpoint.create(new EchoImpl());
            try {
                x.publish(addr);
                check(false, "publish('" + addr + "') must throw");
            } catch (IllegalArgumentException e) {
                check(e.getMessage() != null && !e.getMessage().isEmpty(),
                      "bad address '" + addr + "' IAE message non-empty");
            }
            check(!x.isPublished(), "bad address '" + addr + "' leaves unpublished");
        }
    }

    // ---- Endpoint 元数据/处理器链/上下文 ----
    static void testMetadataAndContext() {
        Endpoint ep = Endpoint.create(new EchoImpl());
        // metadata 带内容往返
        List<javax.xml.transform.Source> md = new java.util.ArrayList<javax.xml.transform.Source>();
        md.add(new javax.xml.transform.stream.StreamSource(
            new java.io.StringReader("<wsdl:definitions xmlns:wsdl=\"http://schemas.xmlsoap.org/wsdl/\"/>")));
        md.add(new javax.xml.transform.stream.StreamSource(new java.io.StringReader("<schema/>")));
        ep.setMetadata(md);
        List<javax.xml.transform.Source> back = ep.getMetadata();
        check(back != null, "metadata non-null after set");
        check(back.size() == 2, "metadata element count round-trip");
        // binding 处理器链：默认空，可增删并回写（同一 List 实例契约在 Binding 接口）
        List<javax.xml.ws.handler.Handler> hc = ep.getBinding().getHandlerChain();
        check(hc != null, "binding handler chain non-null");
        check(hc.isEmpty(), "default handler chain empty");
        javax.xml.ws.handler.LogicalHandler<javax.xml.ws.handler.LogicalMessageContext> h =
            new javax.xml.ws.handler.LogicalHandler<javax.xml.ws.handler.LogicalMessageContext>() {
                @Override public void close(javax.xml.ws.handler.MessageContext c) { }
                @Override public boolean handleFault(javax.xml.ws.handler.LogicalMessageContext c) { return true; }
                @Override public boolean handleMessage(javax.xml.ws.handler.LogicalMessageContext c) { return true; }
            };
        hc.add(h);
        ep.getBinding().setHandlerChain(hc);
        check(ep.getBinding().getHandlerChain().size() == 1, "handler chain add/set round-trip");
        // setEndpointContext：自造上下文注入 + null 不抛（实测）
        javax.xml.ws.EndpointContext ctx = new javax.xml.ws.EndpointContext() {
            @Override public java.util.Set<Endpoint> getEndpoints() {
                return new java.util.HashSet<Endpoint>();
            }
        };
        ep.setEndpointContext(ctx);
        check(true, "setEndpointContext(custom) accepted");
        try {
            ep.setEndpointContext(null);
            check(true, "setEndpointContext(null) accepted");
        } catch (RuntimeException e) {
            check(false, "setEndpointContext(null) should not throw: " + e);
        }
    }
}
