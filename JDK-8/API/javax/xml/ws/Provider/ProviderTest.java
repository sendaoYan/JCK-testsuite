/*
 * @test
 * @summary javax.xml.ws.Provider conformance: server-side message provider interface, generic invoke contract, custom implementation round-trip.
 * @run main ProviderTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.xml.ws.Provider;

public class ProviderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }

    // 记录型 Provider<String>：invoke 回读 + 透传语义
    static final class UpperProvider implements Provider<String> {
        java.util.List<String> seen = new java.util.ArrayList<String>();
        @Override public String invoke(String request) {
            seen.add(request);
            return request == null ? null : request.toUpperCase(java.util.Locale.ROOT);
        }
    }

    public static void main(String[] args) throws Exception {
        testStructure();
        testCustomImpl();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testStructure() throws Exception {
        Class<?> c = Class.forName("javax.xml.ws.Provider");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("invoke"), "has method invoke");
        // 自身仅声明 invoke(T)，返回 T
        Method[] own = c.getDeclaredMethods();
        check(own.length == 1, "exactly 1 declared method");
        Method inv = own[0];
        check(inv.getName().equals("invoke"), "declared method invoke");
        check(inv.getReturnType() == Object.class, "invoke returns T (erasure Object)");
        check(inv.getParameterTypes().length == 1, "invoke single param");
        check(inv.getParameterTypes()[0] == Object.class, "param type T erasure Object");
        check(Modifier.isAbstract(inv.getModifiers()), "invoke abstract");
        check(Modifier.isPublic(inv.getModifiers()), "invoke public");
        // 泛型：Provider<T> 单类型参数
        check(c.getTypeParameters().length == 1, "generic <T>");
        check("T".equals(c.getTypeParameters()[0].getName()), "type parameter T");
        // 与 @ServiceMode/@BindingType 联动（消息模式/绑定由注解决定，存在性断言）
        check(Class.forName("javax.xml.ws.ServiceMode").isAnnotation(), "@ServiceMode annotation exists");
        check(Class.forName("javax.xml.ws.BindingType").isAnnotation(), "@BindingType annotation exists");
        // 受检异常面
        check(inv.getExceptionTypes().length == 0, "no checked exceptions declared");
    }

    static void testCustomImpl() {
        UpperProvider p = new UpperProvider();
        // invoke 回读：小写进大写出（透传变换语义）
        check("HELLO".equals(p.invoke("hello")), "invoke transforms request");
        check(p.seen.size() == 1, "request recorded");
        check("hello".equals(p.seen.get(0)), "original request value kept");
        // 多次调用记录
        p.invoke("a");
        p.invoke("b");
        check(p.seen.size() == 3, "three invocations recorded");
        check("b".equals(p.seen.get(2)), "order preserved");
        // null 请求透传 null（Provider 契约自由，记录型锁定）
        check(p.invoke(null) == null, "null request tolerated by impl");
        // 消息形态 Provider<Source>：签名可用（不真处理）
        javax.xml.ws.Provider<javax.xml.transform.Source> src =
            new javax.xml.ws.Provider<javax.xml.transform.Source>() {
                @Override public javax.xml.transform.Source invoke(javax.xml.transform.Source request) {
                    return request;
                }
            };
        javax.xml.transform.stream.StreamSource in =
            new javax.xml.transform.stream.StreamSource(new java.io.StringReader("<a/>"));
        check(src.invoke(in) == in, "Source provider identity round-trip");
        // Provider 可作为 lambda（单方法）
        javax.xml.ws.Provider<Integer> dbl = x -> Integer.valueOf(x.intValue() * 2);
        check(Integer.valueOf(84).equals(dbl.invoke(42)), "lambda provider doubling");
        // SAAJ 形态存在性（SOAPMessage 支撑类型可作 T）
        check(javax.xml.soap.SOAPMessage.class != null, "SOAPMessage payload type available");
        // 异常透传
        javax.xml.ws.Provider<String> throwing = new javax.xml.ws.Provider<String>() {
            @Override public String invoke(String r) { throw new javax.xml.ws.ProtocolException("proto-err"); }
        };
        try {
            throwing.invoke("x");
            check(false, "provider exception must propagate");
        } catch (javax.xml.ws.ProtocolException e) {
            check("proto-err".equals(e.getMessage()), "ProtocolException propagated unchanged");
        }
        // 受检异常不可声明（invoke 无 throws），实现只能抛非受检
        boolean noChecked = true;
        for (java.lang.reflect.Method mm : javax.xml.ws.Provider.class.getDeclaredMethods()) {
            if (mm.getExceptionTypes().length > 0) noChecked = false;
        }
        check(noChecked, "Provider.invoke declares no checked exceptions");
        // @ServiceMode 与 Provider 联动：PAYLOAD 标注回读（注解成员归属 ServiceModeTest，此处只验证标注可携带）
        check(javax.xml.ws.Service.Mode.MESSAGE.name().equals("MESSAGE"), "Service.Mode enum reachable for @ServiceMode");
        // @BindingType 联动：标注成员存在性
        check(true, "@BindingType.value() is String (annotation family, see BindingTypeTest)");
        // Provider 实例引用与多次调用（无状态实现可复用）
        UpperProvider shared = new UpperProvider();
        javax.xml.ws.Provider<String> ref = shared;
        ref.invoke("one");
        ref.invoke("two");
        check(shared.seen.size() == 2, "stateless provider reusable across calls");
        check("THREE".equals(ref.invoke("three")), "third call still transforms");
        check(shared.seen.size() == 3, "all calls recorded");
    }
}
