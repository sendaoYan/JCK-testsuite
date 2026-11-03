# Instrumentation — 设计方案

## 1. API 功能
`java.lang.instrument.Instrumentation`（接口）是 agent 与 JVM 交互的核心：`addTransformer`/`removeTransformer`、`retransformClasses`/`redefineClasses`、`isModifiableClass`/`isModifiableModule`、`getAllLoadedClasses`/`getInitiatedClasses`/`getObjectSize`、`redefineModule`、`setNativeMethodPrefix`、bootstrap/system classloader search。

## 2. Java 用户如何使用
```java
public static void premain(String args, Instrumentation inst) {
    inst.addTransformer(t, true);
    long size = inst.getObjectSize(obj);
    Class<?>[] loaded = inst.getAllLoadedClasses();
}
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/lang/instrument` 的 premain agent 是各方法的权威调用示例。
- 大量 APM/agent 框架（SkyWalking、ByteBuddy agent）用 `addTransformer`/`getAllLoadedClasses` 做插桩。

## 4. 测试用例设计 + 自动化实现
白盒：接口 + 关键方法签名/返回类型。
黑盒：匿名实现各方法返回固定值，验证布尔/数组/长整型查询的往返与 void 方法可调用（无真实 agent，纯接口行为）。
对应 `InstrumentationTest.java` 白盒 + 黑盒部分。
