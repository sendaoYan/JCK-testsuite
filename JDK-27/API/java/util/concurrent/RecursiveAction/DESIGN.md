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
场景：
1. 白盒：抽象类 + 继承 ForkJoinTask + 抽象 compute()。
2. 黑盒：平凡 compute 执行并改变共享状态；分治 SumAction 对 0..99 累加到 AtomicInteger 与公式一致。

实现：`RecursiveActionTest.java`（6 断言）。
