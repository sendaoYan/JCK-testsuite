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
