# jdk.jfr.consumer.RecordedThreadGroup 设计案

## 1. API 功能
记录的线程组：getName。

## 2. Java 用户如何使用
RecordedThread.getThreadGroup() 获取。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jfr/share/classes/jdk/jfr/consumer/RecordedThreadGroup.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 关键方法；黑盒 线程组名（可空则跳过）。

## 5. 深化（本轮）
断言规模：基线 → 14。
### 行为闭环
- 组链逐 hop：提交线程组名 == 当前线程组快照名（jtreg 代理 MainThreadGroup），上溯 main 至 system 根（parent null）。
- 27 虚拟线程组：name == "VirtualThreads"，其 parent 链同样终止于 system。
### 实测负例异常表
无专门负例（结构语义为主）。
### 8/27 差异
组链形状一致；27 侧另有 VirtualThreads 组挂在 system 根下。
