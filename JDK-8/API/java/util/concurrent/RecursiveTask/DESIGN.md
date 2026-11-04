# java.util.concurrent.RecursiveTask 设计案（JDK-8）

## 1. API 功能
带返回值的 `ForkJoinTask<V>` 子类：抽象 `protected abstract V compute()`；public 无参构造；`getRawResult()` 具体实现（返回 compute 结果）。值语义分治任务基类。

## 2. Java 用户如何使用
- 继承实现 compute：阈值内直接求值返回；否则 `invokeAll(l, r); return l.join() + r.join();` 或 `l.fork(); return l.join() + r.compute();`。
- Fib/Sum/Max 为三大经典模式。
- `reinitialize()` 复用后 rawResult 保留旧值（文档 "unaffected"，实测确认）。

## 3. 开源代码实际用法调研
- `java.util.stream` 的 reduce/collect 并行树、`Arrays.parallelSort` 归并段、教程级 Fib(10)==55。
- JDK 自带测试：`tck/ForkJoinTaskTest.java` 的 RecursiveTask 场景与 `forkjoin/` 冒烟。

## 4. 测试用例设计 + 自动化实现
场景（30 断言）：
1. 白盒：abstract/extends ForkJoinTask/public 无参构造/compute abstract/getRawResult 具体。
2. rawResult 生命周期：null → invoke() → 42 → join/get/getRawResult 一致。
3. 分治求和：阈值 8 invokeAll 分治 1..100 == 5050；外部 fork+join 同样 5050。
4. 递归最大值：fork/join 配对收敛固定最大 500L。
5. Fib(10) == 55（fork + 内联 invoke 混合模式）。
6. 异常语义：同线程 == 原对象；跨线程 rebuilt+cause；子异常经父 join 传播。
7. timed get：阻塞任务 get(150ms) → TimeoutException，释放后返回 777。
8. reinitialize：`getRawResult() == 10`（旧值保留），再执行得 20。

易错规范细节：固定数组固定结果（1..100=5050）；阻塞任务用 latch 且 finally 释放；异常 == 断言仅同线程。

实现：`RecursiveTaskTest.java`（30 断言）。优先级：分治值正确性 > rawResult/reinit 契约 > 异常/超时。
