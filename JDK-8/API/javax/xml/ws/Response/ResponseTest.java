/*
 * @test
 * @summary javax.xml.ws.Response conformance: Future-based async result with response context, minimal custom implementation closure.
 * @run main ResponseTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.FutureTask;
import javax.xml.ws.Response;

public class ResponseTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }

    // 最小实现：包装 FutureTask（不真异步、不阻塞；autoRun=true 立即完成）
    static final class SimpleResponse implements Response<String> {
        final FutureTask<String> fut;
        final Map<String, Object> ctx = new HashMap<String, Object>();
        SimpleResponse(String value) { this(value, true); }
        SimpleResponse(String value, boolean autoRun) {
            fut = new FutureTask<String>(new java.util.concurrent.Callable<String>() {
                @Override public String call() { return value; }
            });
            if (autoRun) fut.run();
        }
        @Override public boolean cancel(boolean mayInterruptIfRunning) { return fut.cancel(mayInterruptIfRunning); }
        @Override public boolean isCancelled() { return fut.isCancelled(); }
        @Override public boolean isDone() { return fut.isDone(); }
        @Override public String get() throws InterruptedException, java.util.concurrent.ExecutionException {
            return fut.get();
        }
        @Override public String get(long timeout, java.util.concurrent.TimeUnit unit)
                throws InterruptedException, java.util.concurrent.ExecutionException, java.util.concurrent.TimeoutException {
            return fut.get(timeout, unit);
        }
        @Override public Map<String, Object> getContext() { return ctx; }
    }

    public static void main(String[] args) throws Exception {
        testStructure();
        testCustomImpl();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testStructure() throws Exception {
        Class<?> c = Class.forName("javax.xml.ws.Response");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "java.util.concurrent.Future"), "implements java.util.concurrent.Future");
        check(c.getInterfaces().length == 1, "single super-interface Future<T>");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getContext"), "has method getContext");
        // 自身仅声明 getContext
        Method[] own = c.getDeclaredMethods();
        check(own.length == 1, "exactly 1 declared method");
        check(own[0].getName().equals("getContext"), "declared method is getContext");
        check(own[0].getReturnType() == Map.class, "getContext returns Map<String,Object>");
        check(Modifier.isAbstract(own[0].getModifiers()), "getContext abstract");
        // Future 契约面全部继承可用
        String[] futureOps = {"get", "cancel", "isDone", "isCancelled"};
        for (String op : futureOps) check(names.contains(op), "inherits Future." + op);
        // 泛型参数
        check(c.getTypeParameters().length == 1, "generic <T>");
        // Response 可赋值给 Future
        check(java.util.concurrent.Future.class.isAssignableFrom(c), "Response assignable to Future");
    }

    static void testCustomImpl() throws Exception {
        SimpleResponse r = new SimpleResponse("fixed");
        // get() 返回固定值（已完成任务）
        check("fixed".equals(r.get()), "get() fixed value");
        check("fixed".equals(r.get(1, java.util.concurrent.TimeUnit.SECONDS)), "timed get() fixed value");
        check(r.isDone(), "isDone true (pre-completed)");
        check(!r.isCancelled(), "isCancelled false");
        // getContext 非 null，put/get 回读
        Map<String, Object> ctx = r.getContext();
        check(ctx != null, "getContext non-null");
        check(ctx.isEmpty(), "context initially empty");
        ctx.put(javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY, "http://localhost:60123/echo");
        check("http://localhost:60123/echo".equals(r.getContext().get(
                  javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY)),
              "context put/get round-trip");
        ctx.put("int.key", Integer.valueOf(5));
        check(Integer.valueOf(5).equals(r.getContext().get("int.key")), "non-String value round-trip");
        // cancel 已完成任务返回 false 且不影响已完成状态
        check(!r.cancel(true), "cancel of done task returns false");
        check(r.isDone(), "still done after failed cancel");
        check(!r.isCancelled(), "not cancelled");
        // 第二个实例独立性
        SimpleResponse r2 = new SimpleResponse("other");
        check("other".equals(r2.get()), "second instance independent value");
        check(r2.getContext().isEmpty(), "second instance context empty");
        // 类型层次：自定义实现 instanceof Response 与 Future
        check(r instanceof Response, "impl instanceof Response");
        check(r instanceof java.util.concurrent.Future, "impl instanceof Future");
        // 异常场景：get() 抛出的中断/执行异常由底层 Future 透传（Cancelled 抛 CancellationException）
        SimpleResponse rc = new SimpleResponse("x", false); // 不自动完成，可取消
        check(rc.cancel(false), "cancel of not-started runnable returns true");
        check(rc.isCancelled(), "isCancelled after cancel");
        try {
            rc.get();
            check(false, "get after cancel must throw CancellationException");
        } catch (java.util.concurrent.CancellationException e) {
            check(true, "get after cancel -> CancellationException");
        }
    }
}
