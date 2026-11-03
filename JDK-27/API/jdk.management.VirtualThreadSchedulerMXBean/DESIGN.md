# jdk.management.VirtualThreadSchedulerMXBean 设计案

## 1. API 功能
虚拟线程调度器 MXBean 接口：parallelism/poolSize/mounted/queued。

## 2. Java 用户如何使用
ManagementFactory.getPlatformMXBean 获取。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.management/share/classes/jdk/management/VirtualThreadSchedulerMXBean.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。黑盒获取+各计数器范围断言。
