# jdk.jfr.consumer.RecordedThread 设计案（JDK-8）

## 1. API 功能
线程引用值对象：`getJavaThreadId`/`getJavaName`/`getJavaThreadGroup`/`getId`/`osThreadId`/`osThread`（8 侧废弃名）。

## 2. Java 用户如何使用
从事件字段取线程身份：thread 字段、eventThread 隐式字段。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk8u-dev/jdk/src/jdk.jfr/share/classes/jdk/jfr/consumer/RecordedThread.java（8u262 backport）。

## 4. 测试用例设计 + 自动化实现
白盒 + 黑盒（提交事件的线程内读自身字段）。骨架 `static int passed; check(boolean,String)`。

## 5. 深化（本轮）
断言规模：基线 → 32。
### 行为闭环
- 在受检线程内（CountDownLatch 保活）commit 事件：`getJavaThreadId() == Thread.currentThread().getId()`（实测 24==24）；`getJavaName()` 等于线程名快照；`getJavaThreadGroup()` 名称等于线程组快照名。
- `getId() > 0`：JDK-8 的 getId 是 JFR 内部线程 id，jtreg 代理环境下不等于 javaThreadId（实测 69 != 24），故只断言正值。
- 已终止线程：线程 join 后回读，8 侧其 Thread 对象字段引用为 null（27 侧保留）——实证差异断言。
### 实测负例异常表
无专门负例（正值语义为主）；InterruptedException 仅用于 latch 协调。
### 8/27 差异
27 侧有 `isVirtual()`（虚拟线程 true、osThreadId=-1、组名 VirtualThreads）；8 侧无该方法且终止线程字段引用为 null。
