# java.util.concurrent.CompletionService 设计案

## 1. API 功能
接口 `<V>`：按**完成序**（非提交序）取任务结果的解耦层。5 个方法：`submit(Callable)`/`submit(Runnable, V)`/`take()`（阻塞）/`poll()`/`poll(long, TimeUnit)`。全部抽象（无 default）。唯一 JDK 实现：ExecutorCompletionService（行为断言归该批次，本目录锁接口结构 + 泛型）。

## 2. Java 用户如何使用（含开源实际用法调研）
- 典型用法：N 个并行查询取最先完成的有效结果（如多源行情聚合）、批处理按完成序回写。
- 开源用法：Guava `CompletionService` 同概念封装；dubbo/sofa 并行调用聚合；JDK 内部 `AbstractExecutorService.invokeAny` 也用完成队列思想。
- 泛型契约：submit(Runnable, V) 的固定结果在 take 出的 Future.get() 上原样返回。

## 3. 参考来源
- Oracle javadoc：CompletionService（完成队列语义、方法面）。
- OpenJDK：`~/git/jdk8u-dev/jdk/test/java/util/concurrent/ExecutorCompletionService/`。
- JCK runtime-8b：`~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_util/concurrent/ExecutorCompletionService/`。

## 4. 测试用例设计 + 易错规范细节
实现：`CompletionServiceTest.java`（PASS 18 断言）。
| 场景 | 断言要点 | 优先级 |
|---|---|---|
| 白盒方法面 | isInterface；恰好 5 个声明方法（锁定）；5 个方法逐一存在；take 声明受检异常且 abstract | P0 |
| 泛型 | 单类型参数 V；Callable 路径与 Runnable+固定值路径的返回类型贯通 | P0 |
| 实现关系 | ExecutorCompletionService 实现本接口；与 ExecutorService 互不 assignable（无关接口） | P0 |
| 行为（轻量） | submit(Callable)→Future.get()==42；submit(Runnable,7)→get()==7；take() 取回已排队的 Future 之一 | P1 |
| 易错细节 | 深行为（完成序、poll 超时、异常传播）在 ExecutorCompletionService 批次；take 后的 Future 与 submit 返回的是同一对象 | P1 |
