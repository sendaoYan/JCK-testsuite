# java.lang.invoke.VolatileCallSite — 设计方案

## 1. API 功能
易变调用点（CallSite 子类）：target 读写均 volatile，换 target 立即对所有线程可见（无需 syncAll）。

## 2. Java 用户如何使用
```java
VolatileCallSite cs = new VolatileCallSite(mt);
cs.setTarget(a); // 任意线程即刻看到
```

## 3. 开源代码实际用法调研
- JDK `VolatileCallSite.java` Javadoc（与 Mutable 的可见性对比表）。
- 高频被多线程调用的 invokedynamic 点（如 Groovy MOP）用它避免缓存陈旧。

## 4. 测试用例设计 + 自动化实现
白盒：public、构造两个重载、继承 CallSite。
黑盒：多实例互相独立；换 target 后 dynamicInvoker 直接生效；getTarget 与 setTarget 的值往返；错类型 setTarget→ClassCastException。
对应 `VolatileCallSiteTest.java` 的 `testBlackBox()`。