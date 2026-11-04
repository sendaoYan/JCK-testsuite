# javax.management.MBeanServerBuilder 黑盒行为测试设计

## 1. API 功能
- 可被子类化的构造器：`newMBeanServerDelegate()`、`newMBeanServer(String, MBeanServer, MBeanServerDelegate)`、`newMBeanServer(String, MBeanServer, MBeanServerDelegate, boolean)`。

## 2. Java 用户如何使用
通过系统属性 `javax.management.builder.initial` 指定自定义 builder，定制 MBeanServer 的构造与 delegate。
```java
MBeanServerBuilder b = new MBeanServerBuilder();
MBeanServer mbs = b.newMBeanServer("dom", null, b.newMBeanServerDelegate());
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/management/MBeanServerFactory/ReleaseMBeanServerTest.java`：工厂与 builder。
- 实现权威：`src/java.management/share/classes/javax/management/MBeanServerBuilder.java`。

## 4. 测试用例设计 + 自动化实现
- 黑盒：newMBeanServerDelegate 返回 MBeanServerDelegate；newMBeanServer(dom,null,delegate) 返回可用的 MBeanServer，注册 MBean 后 getAttribute 往返；default domain 正确。
- 白盒：public 类、非 final、含三个 new 方法。

## 深化（2026-11）：子类覆写 + 构造语义

### 新增覆盖
1. **RecordingBuilder 子类**覆写 `newMBeanServerDelegate()`：记录调用计数与实例，返回值即记录实例；`newMBeanServer(dom,null,delegate)` 使用给定 delegate（server.getAttribute(DELEGATE_NAME,"MBeanServerId") == delegate.getMBeanServerId() 证同一实例）；4 参 newMBeanServer 不会再调 newMBeanServerDelegate。
2. **建出的 server 全功能**：注册/属性读写/isInstanceOf/注销。
3. **默认 builder 语义**：null defaultDomain → "DefaultDomain"；**null delegate 也能建可用 server**（内部自建，DELEGATE_NAME 已注册）——实测两侧一致。
4. 不改全局 `javax.management.builder.initial` 系统属性：只做子类直测（避免污染同 JVM 其它测试，符合批次规范）。

### 参考来源
- Oracle javadoc: MBeanServerBuilder。
- JMX Spec 1.4 第 "MBean Server Builder" 章。
