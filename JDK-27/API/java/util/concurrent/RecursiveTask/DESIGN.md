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
场景：
1. 白盒：抽象类 + 继承 ForkJoinTask + 抽象 compute()。
2. 黑盒：平凡 compute 返回值；分治求和（fork/join）与串行和一致。

实现：`RecursiveTaskTest.java`（6 断言）。
