# java.util.concurrent.RecursiveTask 设计案

## 1. API 功能
抽象类，继承 `ForkJoinTask<V>`。用户实现 `protected abstract V compute()`；`getRawResult()`/`setRawResult`/`exec` 已 final 实现，封装结果存储。

## 2. Java 用户如何使用
- 子类化：`class Sum extends RecursiveTask<Long> { protected Long compute() { ...左.fork(); 右.compute(); 左.join();... } }`。
- 链路：`pool.invoke(new Sum(...))` 分治计算；阈值以下直接返回、阈值以上 fork 左右子树。

## 3. 开源代码实际用法调研
- JDK 自用：`java.util.stream` 归约、`Arrays.parallelSort` 等以 RecursiveTask 形态实现并行分治。
- JDK 自带测试：`tck/RecursiveTaskTest.java`、`test/jdk/java/util/concurrent/forkjoin/`。

## 4. 测试用例设计 + 自动化实现
场景（32 断言）：
1. 白盒：抽象类 + 继承 ForkJoinTask + 抽象 compute() + public 无参构造 + getRawResult 具体（非抽象）。
2. 黑盒：平凡 compute 返回值；分治求和（fork/join）与串行和一致。
3. rawResult 生命周期：null → invoke → 42 → join/get/getRawResult 一致。
4. 分治求和 1..100==5050（invokeAll + 外部 fork/join 双路径）；递归最大值收敛固定最大 500；Fib(10)==55。
5. 异常语义：同线程 == 原对象；跨线程 rebuilt+cause；子异常经父 join 传播。
6. timed get(150ms) → TimeoutException；释放后 777。
7. reinitialize：getRawResult()==10（旧值保留，文档 "unaffected"），再执行 20。

易错规范细节：固定数组固定结果（5050）；阻塞任务 latch 且 finally 释放；异常 == 断言仅同线程。

实现：`RecursiveTaskTest.java`（32 断言）。优先级：分治值正确性 > rawResult/reinit 契约 > 异常/超时。
