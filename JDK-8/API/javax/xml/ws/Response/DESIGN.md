# Response 设计案（JDK-8 契约测试）

## 1. API 功能
javax.xml.ws.Response<T> 是 Dispatch.invokeAsync(T) 轮询式异步调用的返回（JSR 224 §5.4.2）：扩展 java.util.concurrent.Future<T>，附加 getContext() 返回随响应填充的消息上下文（与 BindingProvider.getResponseContext 同构）。容器在完成时设置结果与上下文。

## 2. 使用方式
典型路径：`Response<String> r = dispatch.invokeAsync(payload); while (!r.isDone()) ...; String v = r.get(); Map ctx = r.getContext();`。本测试：自造最小实现（包装预完成/未启动 FutureTask + HashMap 上下文）锁定 Future 全操作语义与上下文往返，不真异步。

## 3. 参考来源
Oracle javadoc（docs.oracle.com/javase/8/docs/api/javax/xml/ws/Response.html）、JSR 224 (JAX-WS 2.2)、JDK Future 语义（java.util.concurrent）、本仓库 ResponseTest。

## 4. 测试用例设计 + 易错规范细节
- 结构面：接口、单父接口 Future<T>、恰 1 个自身声明方法 getContext()→Map（abstract/public）；get/cancel/isDone/isCancelled 全部继承。
- 自造实现闭环（P0）：get()==固定值（含限时形态）、isDone true、isCancelled false、getContext 非 null 且 put/get 回读（标准属性键+自定义键+非 String 值）。
- Future 语义矩阵：已完成任务 cancel 返回 false 且不影响 isDone；未启动任务 cancel(false) 返回 true → isCancelled true → get() 抛 CancellationException（真实 Future 契约透传）。
- 实例独立性：两个实现互不串值。
- 坑：测试用 FutureTask 必须 run() 预完成否则 get() 阻塞死锁；需要可取消场景时构造不 run 的实例。
