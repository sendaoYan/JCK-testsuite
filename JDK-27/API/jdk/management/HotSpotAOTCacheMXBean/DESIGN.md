# jdk.management.HotSpotAOTCacheMXBean 设计案

## 1. API 功能
AOT 缓存 MXBean 接口：endRecording。

## 2. Java 用户如何使用
ManagementFactory.getPlatformMXBean 获取。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.management/share/classes/jdk/management/HotSpotAOTCacheMXBean.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。黑盒尝试获取+调用（预览特性，防御）。
