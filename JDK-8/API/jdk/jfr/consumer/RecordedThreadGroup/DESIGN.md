# jdk.jfr.consumer.RecordedThreadGroup 设计案（JDK-8）

## 1. API 功能
线程组值对象：`getName`/`getParent`。

## 2. Java 用户如何使用
从 RecordedThread.getJavaThreadGroup() 取组名与组链。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk8u-dev/jdk/src/jdk.jfr/share/classes/jdk/jfr/consumer/RecordedThreadGroup.java（8u262 backport）。

## 4. 测试用例设计 + 自动化实现
白盒 + 黑盒（快照线程组链回读）。骨架 `static int passed; check(boolean,String)`。

## 5. 深化（本轮）
断言规模：基线 → 18。
### 行为闭环
- 提交事件线程的组链逐 hop 断言：组名等于 `Thread.currentThread().getThreadGroup().getName()` 快照（jtreg 代理下为 MainThreadGroup），沿 getParent() 上溯经过 main，终止于 system 根（其 parent 为 null）。
- toString 非空且含组名。
### 实测负例异常表
无专门负例（结构语义为主）。
### 8/27 差异
组链形状一致（jtreg MainWrapper 代理：MainThread → main → system → null）；27 侧另有虚拟线程的 VirtualThreads 组。
