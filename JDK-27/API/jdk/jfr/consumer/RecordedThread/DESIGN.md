# jdk.jfr.consumer.RecordedThread 设计案

## 1. API 功能
记录的线程：getJavaThreadId/getJavaName/getId/isVirtual/getThreadGroup。

## 2. Java 用户如何使用
RecordedEvent.getThread() 获取。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jfr/share/classes/jdk/jfr/consumer/RecordedThread.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 关键方法；黑盒 线程 id/name 非空。

## 5. 深化（本轮）
断言规模：基线 → 31。
### 行为闭环
- 平台线程：getJavaThreadId == Thread.currentThread().getId()、getJavaName == 名字快照、组链名 == Thread.currentThread().getThreadGroup() 快照（jtreg 代理下 MainThreadGroup→main→system→null）。
- 27 独有 `isVirtual()`：平台线程 false；虚拟线程 true 且 getJavaThreadGroup().getName()=="VirtualThreads"、osThreadId 为 -1。
- getId()>0（JFR 内部 id，不等于 javaThreadId）。
### 实测负例异常表
无专门负例（正值语义为主）。
### 8/27 差异
8 无 isVirtual；8 已终止线程字段引用回读为 null，27 保留。
