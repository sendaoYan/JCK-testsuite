# jdk.management.VirtualThreadSchedulerMXBean 设计案

## 1. API 功能
虚拟线程调度器 MXBean 接口：parallelism/poolSize/mounted/queued。

## 2. Java 用户如何使用
ManagementFactory.getPlatformMXBean 获取。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.management/share/classes/jdk/management/VirtualThreadSchedulerMXBean.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。黑盒获取+各计数器范围断言。

## 5. 深化（本轮）
断言规模：基线 14 → 26。
### 行为闭环
- 注册面：ObjectName `jdk.management:type=VirtualThreadScheduler`、platform MBeanServer isRegistered。
- setParallelism 往返：+1 可见、恢复原值（实测默认 64 为机器相关，只断 >=1）；MBeanServer 通道 Parallelism/PoolSize 属性与 getter 一致。
- 计数器非负：poolSize/mounted/queued >= 0。
### 实测负例异常表
| 操作 | 异常 | 精确消息 |
|---|---|---|
| `setParallelism(0)` / `setParallelism(-1)` / `setParallelism(Integer.MAX_VALUE)` | IllegalArgumentException | 消息为 null（实测，只断言类型）；拒绝后 parallelism 不变 |
### 8/27 差异
VirtualThreadSchedulerMXBean 为 27 独有（8 无虚拟线程）。
