# javax.management.MBeanServerFactory 黑盒行为测试设计

## 1. API 功能
- 静态方法：`createMBeanServer()`、`createMBeanServer(String)`、`releaseMBeanServer(MBeanServer)`、`findMBeanServer(String)`、`getMBeanServerId(String)`。

## 2. Java 用户如何使用
创建/释放被跟踪的 MBeanServer，按 agentId 查找；未指定 agentId 时用 delegate 的 MBeanServerId 查询。
```java
MBeanServer mbs = MBeanServerFactory.createMBeanServer("my.domain");
MBeanServerFactory.releaseMBeanServer(mbs);
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/management/MBeanServerFactory/ReleaseMBeanServerTest.java`：创建/释放。
- 实现权威：`src/java.management/share/classes/javax/management/MBeanServerFactory.java`。

## 4. 测试用例设计 + 自动化实现
- 黑盒：createMBeanServer(domain) 返回的服务器 default domain 正确且被 findMBeanServer(null) 跟踪；findMBeanServer(null) 返回列表；通过 delegate 的 MBeanServerId 用 findMBeanServer(id) 找回同一服务器；getMBeanServerId(domain) 返回非空；finally releaseMBeanServer。
- 白盒：public 具体类、静态方法存在。
