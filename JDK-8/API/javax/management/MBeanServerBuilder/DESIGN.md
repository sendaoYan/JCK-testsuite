# javax.management.MBeanServerBuilder 契约测试设计（2026-11 新建）

## 1. API 功能
- 工厂钩子：newMBeanServerDelegate()/newMBeanServer(defaultDomain,outer,delegate)，可子类替换 delegate/server 实现。

## 2. Java 用户典型用法
见 JDK-27 侧同目录设计与下节深化用例（双侧同代码）。

## 3. 参考来源
- Oracle javadoc: javax.management.MBeanServerBuilder。
- OpenJDK `test/jdk/javax/management/` 对应目录（思路借鉴，代码自写）。
## 4. 测试用例设计
见下节深化清单。

## 深化（2026-11）：子类覆写 + 构造语义

### 新增覆盖
1. **RecordingBuilder 子类**覆写 `newMBeanServerDelegate()`：记录调用计数与实例，返回值即记录实例；`newMBeanServer(dom,null,delegate)` 使用给定 delegate（server.getAttribute(DELEGATE_NAME,"MBeanServerId") == delegate.getMBeanServerId() 证同一实例）；4 参 newMBeanServer 不会再调 newMBeanServerDelegate。
2. **建出的 server 全功能**：注册/属性读写/isInstanceOf/注销。
3. **默认 builder 语义**：null defaultDomain → "DefaultDomain"；**null delegate 也能建可用 server**（内部自建，DELEGATE_NAME 已注册）——实测两侧一致。
4. 不改全局 `javax.management.builder.initial` 系统属性：只做子类直测（避免污染同 JVM 其它测试，符合批次规范）。

### 参考来源
- Oracle javadoc: MBeanServerBuilder。
- JMX Spec 1.4 第 "MBean Server Builder" 章。
