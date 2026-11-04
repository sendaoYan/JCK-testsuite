# javax.swing.SwingWorker 设计案

## 1. API 功能
`SwingWorker` 抽象类在后台线程执行任务。方法分组：
- 任务：`doInBackground`（抽象）、`execute`。
- 结果/状态：`get`、`getProgress/isDone/isCancelled/getState`。
- 发布/进度：`publish/process/done/setProgress`。

## 2. Java 用户如何使用
用户继承它执行耗时任务并在 EDT 处理结果：
```java
SwingWorker<Integer, Void> worker = new SwingWorker<>() {
    protected Integer doInBackground() { return compute(); }
};
worker.execute();
Integer result = worker.get();
```
典型链路：`execute` → `get` → `isDone/getState`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/SwingWorker.java` 是 JDK 自身实现（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/SwingWorker.java`）。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验 abstract + `RunnableFuture`。
- 行为（黑盒）：`testBlackBox()` 匿名实现 `doInBackground` 返回 42，验证初始进度、`execute` 后 `get(timeout)` 结果、`isDone`、状态 `DONE`。
