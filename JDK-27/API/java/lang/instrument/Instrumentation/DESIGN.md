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

## 5. 契约深化（本批）
- javaagent 子进程闭环：Manifest（Premain-Class/Can-Redefine-Classes/Can-Retransform-Classes/Can-Set-Native-Method-Prefix）+ 纯 Java JarOutputStream 打 jar；ProcessBuilder 拉起子 JVM；AGENT key=value 输出协议回传观测值。
- 闭环断言（子 JVM 真值）：isRedefineClassesSupported/isRetransformClassesSupported/isNativeMethodPrefixSupported == true；isModifiableClass(String) == true、(int[])/(int) == false；getAllLoadedClasses > 0；getInitiatedClasses(boot) 含 java.lang.Object；getObjectSize(Object) > 0；removeTransformer(已注册) == true、(未注册) == false。
- premain 注册的 counting transformer 观察到 >= 1 次后续类加载（弱断言下界；实证两侧 20/16 次）。
- 版本差异：JDK-27 有 redefineModule/isModifiableModule（6 参/1 参）与 UnmodifiableModuleException；JDK-8 均无——双侧各断自己。
- 工程坑（本批实证）：jtreg 会把测试树内带 main 的顶层 .java 当独立测试执行——agent 类必须作为嵌套类（InstrumentationTest$AgentLife）内联，且 Premain-Class/入口名/主类名三处都要用 $ 形态。
- 本侧（JDK-27）：模块面存在且签名精确（6 参 redefineModule、isModifiableModule(Module)→boolean）。
