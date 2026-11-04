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

## 深化（2026-11）：重载/跟踪/释放生命周期

### 新增覆盖
1. **newMBeanServer 两重载**：() 与 (domain) 均不进 findMBeanServer(null)；(null) → "DefaultDomain"；两次调用实例独立；注册隔离（一个 server 注册的名在另一个不可见）、计数独立（delegate+N 精确）。
2. **createMBeanServer 跟踪**：find(null) contains；两个 tracked server 的 MBeanServerId 互异；find(ownId) 命中且排除他人；find(未知 id) 空列表（返回类型 ArrayList）；getClassLoaderRepository 非空。
3. **release 语义**：release 后 find(ownId) 为空；release(untracked)/double release/**release(null)** 均 `IllegalArgumentException`（null 也是 IAE 而非 NPE，实测）。

### 易错细节
- DefaultLoaderRepository 只看 tracked（createMBeanServer）server——见 DefaultLoaderRepository/DESIGN。
- finally 中安全释放，避免污染后续测试。

### 参考来源
- Oracle javadoc: MBeanServerFactory（release 语义章节）。
- OpenJDK `test/jdk/javax/management/MBeanServerFactory/ReleaseMBeanServerTest.java`。
