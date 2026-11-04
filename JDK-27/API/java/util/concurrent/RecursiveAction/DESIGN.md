# java.util.concurrent.RecursiveAction 设计案

## 1. API 功能
抽象类，继承 `ForkJoinTask<Void>`（无返回结果）。用户实现 `protected abstract void compute()`；`getRawResult`/`setRawResult`/`exec` 已实现，结果恒为 null。

## 2. Java 用户如何使用
- 子类化：`class SumAction extends RecursiveAction { protected void compute() { ...左.fork(); 右.compute(); 左.join();... } }`，通过共享 `AtomicInteger` 等累积副作用。
- 链路：`pool.invoke(action)` 执行；适合无需返回值的并行遍历/累积。

## 3. 开源代码实际用法调研
- JDK 自用：并行排序、并行遍历中「无返回值」的分治动作。
- JDK 自带测试：`tck/RecursiveActionTest.java`、`test/jdk/java/util/concurrent/forkjoin/`。

## 4. 测试用例设计 + 自动化实现
场景（40 断言）：
1. 白盒：抽象类 + 继承 ForkJoinTask + 抽象 compute() + public 无参构造 + compute 返回 void + getRawResult 声明返回 Void。
2. 黑盒：平凡 compute 执行并改变共享状态；分治 SumAction 对 0..99 累加到 AtomicInteger 与公式一致。
3. getRawResult 生命周期：执行前后/join/get/invoke 恒 null。
4. invokeAll(t1,t2) 计数 2 且返回前 isDone；空变体无异常。
5. Doubler 分治：1..100 原地翻倍 → 2..200；MaxFinder CAS AtomicLong 收敛固定最大 1000。
6. 异常传播：同线程 == 原对象；跨线程 rebuilt+cause。
7. fork+reinitialize 复用（恰 2 次）；quietlyComplete+join。
8. invokeAll 集合形式：5 个 action 全执行、返回集大小 5。

易错规范细节：固定数组固定结果；CAS 收敛循环；共用池 fork 小任务且 join 收尾。

实现：`RecursiveActionTest.java`（40 断言）。优先级：分治副作用正确性 > rawResult 恒 null > 异常/复用。
