# jdk.management.HotSpotAOTCacheMXBean 设计案

## 1. API 功能
AOT 缓存 MXBean 接口：endRecording。

## 2. Java 用户如何使用
ManagementFactory.getPlatformMXBean 获取。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.management/share/classes/jdk/management/HotSpotAOTCacheMXBean.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。黑盒尝试获取+调用（预览特性，防御）。

## 5. 深化（本轮）
断言规模：基线 6 → 13。
### 行为闭环
- 注册面：ObjectName `jdk.management:type=HotSpotAOTCache`，platform MBeanServer isRegistered、queryMBeans 恰 1 实例。
- endRecording 语义：无 AOT 录制进行时返回 false 且幂等；MBeanServer invoke 双通道一致（FALSE）；MBeanInfo 暴露 endRecording 操作。
- 缺席容忍：bean 不可用时仅跑形状检查（不失败）。
### 实测负例异常表
无专门负例（幂等语义为主）。
### 8/27 差异
HotSpotAOTCacheMXBean 为 27 独有。
