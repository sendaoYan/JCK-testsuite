# java.util.concurrent.Callable 设计案

## 1. API 功能
泛型函数式接口 `<V>`：单方法 `V call() throws Exception`——有返回值、**允许抛出受检异常**（与 Runnable 的两点本质差异）。带 @FunctionalInterface 注解，可 lambda/方法引用。

## 2. Java 用户如何使用（含开源实际用法调研）
- 典型用法：`executorService.submit(callable)` 拿 Future；`FutureTask` 包装后在任意线程执行。
- 开源用法：Guava `Callables`/`ListeningExecutorService.submit`；Spring `TaskDecorator` + `submit(Callable)`；各大框架的"有返回值的异步单元"都以本接口为基准。
- 关键契约：call() 抛出的异常（受检/非受检/Error）**原样**传播到直接调用方；经 submit 后包装为 ExecutionException(cause=原对象)。

## 3. 参考来源
- Oracle javadoc：Callable（@FunctionalInterface、throws Exception 说明）。
- OpenJDK：`~/git/jdk8u-dev/jdk/test/java/util/concurrent/Executors/Throws.java`（受检异常透传）。
- JCK runtime-8b：`~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_util/concurrent/Executors/`（Callable 适配路径）。

## 4. 测试用例设计 + 易错规范细节
实现：`CallableTest.java`（PASS 23 断言）。
| 场景 | 断言要点 | 优先级 |
|---|---|---|
| 白盒结构 | isInterface；单一声明方法；类型参数 V；call() 返回 Object 擦除、throws Exception；@FunctionalInterface 存在 | P0 |
| lambda/方法引用 | 值计算、方法引用、返回数组恒等（同一实例） | P0 |
| 受检异常透传 | IOException/InterruptedException **对象恒等**（==）传播 | P0 |
| 非受检/Error 透传 | IllegalStateException/LinkageError 恒等传播、不包装 | P0 |
| null/Void | Callable<Void> 返回 null 合法 | P1 |
| 重复调用 | 普通方法语义（计数递增） | P2 |
| 易错细节 | Executors.callable(...) 适配属 Executors 目录，此处不测；call() 的 throws 子句使得 lambda 可直接 throw 受检异常 | P1 |
