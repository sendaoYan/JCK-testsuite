/*
 * @test
 * @summary javax.xml.ws.AsyncHandler conformance: async callback contract, custom implementation records invocation and response reference identity.
 * @run main AsyncHandlerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;

public class AsyncHandlerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }

    // 记录型实现：回调契约（被调用 + 收到同一 Response 引用）
    static final class RecordingHandler implements AsyncHandler<String> {
        int calls;
        Response<String> last;
        @Override public void handleResponse(Response<String> res) {
            calls++;
            last = res;
        }
    }

    // 固定值 Response（复用 ResponseTest 的最小实现模式）
    static final class FixedResponse implements Response<String> {
        final java.util.concurrent.FutureTask<String> fut;
        final Map<String, Object> ctx = new java.util.HashMap<String, Object>();
        FixedResponse() {
            fut = new java.util.concurrent.FutureTask<String>(
                new java.util.concurrent.Callable<String>() {
                    @Override public String call() { return "resp"; }
                });
            fut.run();
        }
        @Override public boolean cancel(boolean m) { return fut.cancel(m); }
        @Override public boolean isCancelled() { return fut.isCancelled(); }
        @Override public boolean isDone() { return fut.isDone(); }
        @Override public String get() throws InterruptedException, java.util.concurrent.ExecutionException { return fut.get(); }
        @Override public String get(long t, java.util.concurrent.TimeUnit u)
                throws InterruptedException, java.util.concurrent.ExecutionException, java.util.concurrent.TimeoutException {
            return fut.get(t, u);
        }
        @Override public Map<String, Object> getContext() { return ctx; }
    }

    public static void main(String[] args) throws Exception {
        testStructure();
        testCallbackContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testStructure() throws Exception {
        Class<?> c = Class.forName("javax.xml.ws.AsyncHandler");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("handleResponse"), "has method handleResponse");
        // 自身仅声明 handleResponse(Response<T>)，返回 void
        Method[] own = c.getDeclaredMethods();
        check(own.length == 1, "exactly 1 declared method");
        check(own[0].getName().equals("handleResponse"), "declared method handleResponse");
        check(own[0].getReturnType() == void.class, "handleResponse returns void");
        check(own[0].getParameterTypes().length == 1, "handleResponse single param");
        check(own[0].getParameterTypes()[0] == Response.class, "param type Response<T>");
        check(Modifier.isAbstract(own[0].getModifiers()), "handleResponse abstract");
        check(Modifier.isPublic(own[0].getModifiers()), "handleResponse public");
        check(c.getTypeParameters().length == 1, "generic <T>");
        check("T".equals(c.getTypeParameters()[0].getName()), "type parameter T");
        // 受检异常面：handleResponse 不声明受检异常（可直接在无 throws 的 lambda/匿名类实现）
        check(own[0].getExceptionTypes().length == 0, "no checked exceptions declared");
    }

    static void testCallbackContract() throws Exception {
        RecordingHandler h = new RecordingHandler();
        check(h.calls == 0, "initially not called");
        check(h.last == null, "no response recorded yet");
        Response<String> r = new FixedResponse();
        // 回调：同一 Response 引用传递
        h.handleResponse(r);
        check(h.calls == 1, "handler invoked exactly once");
        check(h.last == r, "same Response reference received (identity)");
        // 再次回调：计数累计，引用替换
        Response<String> r2 = new FixedResponse();
        h.handleResponse(r2);
        check(h.calls == 2, "second invocation counted");
        check(h.last == r2, "latest response reference kept");
        check(h.last != r, "reference replaced");
        // 回调内可读取 Response 语义（get/isDone/context）
        final StringBuilder seen = new StringBuilder();
        AsyncHandler<String> reading = new AsyncHandler<String>() {
            @Override public void handleResponse(Response<String> res) {
                try {
                    seen.append(res.get()).append("|").append(res.isDone()).append("|")
                        .append(res.getContext() != null);
                } catch (Exception e) {
                    seen.append("ERR");
                }
            }
        };
        reading.handleResponse(r);
        check("resp|true|true".equals(seen.toString()), "callback can consume Response: " + seen);
        // 异常透传契约：实现抛 RuntimeException 直接冒泡（容器不吞）
        AsyncHandler<String> throwing = new AsyncHandler<String>() {
            @Override public void handleResponse(Response<String> res) {
                throw new IllegalStateException("boom");
            }
        };
        try {
            throwing.handleResponse(r);
            check(false, "handler exception must propagate");
        } catch (IllegalStateException e) {
            check("boom".equals(e.getMessage()), "handler exception propagated unchanged");
        }
        // lambda 形态可实现（函数式接口形状：单方法）
        final int[] lambdaCalls = new int[1];
        AsyncHandler<String> lambda = res -> lambdaCalls[0]++;
        lambda.handleResponse(r);
        check(lambdaCalls[0] == 1, "lambda implementation invoked");
        // null Response 传入：实现自行决定（不抛为契约自由），记录型接受
        RecordingHandler acc = new RecordingHandler();
        acc.handleResponse(null);
        check(acc.calls == 1 && acc.last == null, "null response accepted by lenient impl");
        // 多个 handler 广播：每个都收到同一引用
        RecordingHandler b1 = new RecordingHandler(), b2 = new RecordingHandler();
        Response<String> rr = new FixedResponse();
        b1.handleResponse(rr);
        b2.handleResponse(rr);
        check(b1.last == rr && b2.last == rr, "both handlers receive same reference");
        // Dispatch.invokeAsync(T, AsyncHandler<T>) 签名关联（调用方契约面）
        try {
            java.lang.reflect.Method m = Class.forName("javax.xml.ws.Dispatch")
                .getMethod("invokeAsync", Object.class, AsyncHandler.class);
            check(m != null, "Dispatch.invokeAsync(T,AsyncHandler) signature present");
            check(m.getReturnType() == java.util.concurrent.Future.class, "returns Future<?>");
        } catch (NoSuchMethodException e) {
            check(false, "Dispatch.invokeAsync signature missing");
        }
        // 泛型族：AsyncHandler<Object> 形态（同一 FixedResponse 以原始类型借道）
        final Response<String> objResp = new FixedResponse();
        @SuppressWarnings("unchecked")
        final Response<Object> widened = (Response<Object>) (Response<?>) objResp;
        AsyncHandler<Object> objH = new AsyncHandler<Object>() {
            int n;
            @Override public void handleResponse(Response<Object> res) { n++; check(res == widened, "object handler ref"); }
        };
        objH.handleResponse(widened);
        check(true, "AsyncHandler<Object> callback accepted");
        // 异步完成即回调的确定性模拟：后台线程 run 完成后回调（不 sleep：run 立即执行）
        final Response<String> fr = new FixedResponse();
        java.util.concurrent.ExecutorService pool = java.util.concurrent.Executors.newSingleThreadExecutor(
            new java.util.concurrent.ThreadFactory() {
                @Override public Thread newThread(Runnable r) {
                    Thread t = new Thread(r, "jck-ah");
                    t.setDaemon(true);
                    return t;
                }
            });
        try {
            final RecordingHandler rh = new RecordingHandler();
            java.util.concurrent.Future<?> done = pool.submit(new Runnable() {
                @Override public void run() { rh.handleResponse(fr); }
            });
            done.get(); // 确定性等待任务完成
            check(rh.calls == 1, "async-style callback executed exactly once");
            check(rh.last == fr, "async-style callback carries same reference");
        } finally {
            pool.shutdown();
        }
    }
}
