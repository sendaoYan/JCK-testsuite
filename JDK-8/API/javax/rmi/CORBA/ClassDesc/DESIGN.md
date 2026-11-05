# javax.rmi.CORBA.ClassDesc 测试设计（JDK-8 侧）

## 1. API 功能
`java.lang.Class` 的 RMI-IIOP 线上表示值类型：SUID `-3477057297839810709`，字段
`repid/codebase`（私有 String），identity equals。

## 2. Java 用户如何使用
```java
// CORBA 流上传输 Class 时由 ValueHandler 自动构造；用户不直接实例化（无公共构造器）。
```
## 3. 开源代码实际用法调研
- JDK-8 源码 `jdk/src/share/classes/javax/rmi/CORBA/*.java` 与实现 `com/sun/corba/se/impl/javax/rmi/CORBA/`（委托机制与 ORB 常量的权威定义）。
- 委托族共享同一装配模式：首次门面调用把 `*Delegate` 字段装配为 `com.sun.corba.se.impl...` 实现。
## 4. 测试用例设计 + 自动化实现
- 白盒：SUID 精确值、两字段类型/可见性、无公共构造器。
- 黑盒：新实例两字段为 null；equals/hashCode 同一性语义（同一对象自等，不同实例不等）。
- 实现见 `ClassDescTest.java`。
