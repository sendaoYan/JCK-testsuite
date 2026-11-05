# AsyncHandler 设计案（JDK-8 契约测试）

## 1. API 功能
javax.xml.ws.AsyncHandler<T> 是 Dispatch.invokeAsync(T, AsyncHandler<T>) 回调式异步调用的回调接口（JSR 224 §5.4.2）：容器在响应就绪时回调 handleResponse(Response<T>)，回调内可通过 Response 拿结果与上下文，避免轮询。

## 2. 使用方式
典型路径：`dispatch.invokeAsync(payload, res -> result.set(res.get()))` 返回 Future<?>（可提前 cancel）。本测试：自造记录型实现锁定"被调用+同一 Response 引用"回调契约；executor 提交任务后 done.get() 确定性等待（不 sleep）；FixedResponse 提供可消费的 Response。

## 3. 参考来源
Oracle javadoc（docs.oracle.com/javase/8/docs/api/javax/xml/ws/AsyncHandler.html）、JSR 224 (JAX-WS 2.2)、本仓库 AsyncHandlerTest。

## 4. 测试用例设计 + 易错规范细节
- 结构面：接口、泛型 <T>、恰 1 声明方法 handleResponse(Response<T>)、void、无受检异常声明（lambda 可直写）。
- 回调契约（P0）：记录型实现收到调用计数==1 且 last==传入引用（恒等）；二次调用计数累计引用替换；多个 handler 广播各自收到同一引用。
- 消费语义：回调内可直接 res.get()/isDone()/getContext()（预完成 Response）。
- 异常透传：实现抛 RuntimeException（ISE("boom")）原样冒泡，容器不吞不换。
- lambda 形态可实现（单抽象方法）。
- 关联面：Dispatch.invokeAsync(Object,AsyncHandler)→Future<?> 签名锁定。
- 异步模拟：daemon 线程池 submit 回调任务，Future.get() 确定性同步（绝不 sleep 断言）。
- 坑：null Response 由实现自行决定接受与否（宽容实现记录 null）；回调线程归属容器，测试中必须预完成 Response 避免阻塞。
