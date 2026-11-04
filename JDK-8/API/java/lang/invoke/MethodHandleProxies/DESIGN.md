# java.lang.invoke.MethodHandleProxies — 设计方案

## 1. API 功能
把句柄适配成 SAM 接口实例：`asInterfaceInstance(intfc, target)` 要求 intfc 是 public 单抽象方法接口（否则 IAE "not a public interface"/"not a single-method interface"；null → NPE），句柄类型经 asType 适配到 SAM 签名（不可适配 → WMTE "cannot convert MethodHandle..."）。`isWrapperInstance/wrapperInstanceTarget/wrapperInstanceType` 自省（target 恒等还原原句柄；非 wrapper → IAE "not a wrapper instance"）。default 方法执行自身实现，仅 SAM 路由到句柄。

## 2. Java 用户如何使用
```java
Runnable r = MethodHandleProxies.asInterfaceInstance(Runnable.class, sideEffectHandle);
r.run();                              // 触发句柄
MethodHandle back = MethodHandleProxies.wrapperInstanceTarget(r);
```

### 参考来源
- Oracle javadoc（8）：https://docs.oracle.com/javase/8/docs/api/java/lang/invoke/MethodHandleProxies.html
- Oracle javadoc（27）：https://docs.oracle.com/en/java/javase/27/docs/api/java.lang.invoke/java/lang/invoke/MethodHandleProxies.html
- OpenJDK 8u 源码：/data/yansendao/git/jdk8u-dev/jdk/src/share/classes/java/lang/invoke/MethodHandleProxies.java
- OpenJDK 27 源码：/data/yansendao/git/jdk/src/java.base/share/classes/java/lang/invoke/MethodHandleProxies.java
- JCK runtime-8b：/data/yansendao/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_lang/invoke/MethodHandleProxies
（只列路径，不拷内容；JCK 仅作覆盖面对照，代码全部自写。）

## 4. 测试用例设计（含易错规范细节）

| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | Runnable 副作用计数精确（run 两次=2） | testRunnableSideEffect |
| P0 | 带返回 SAM：值精确、可按接口类型传参 | testValueReturningSam |
| P0 | 装箱流：Integer 形参落到 int 句柄（2+3=5）、null 装箱 → NPE（27 侧消息 "because "x" is null"）、Object 形参加宽 | testBoxingFlow |
| P0 | wrapperInstanceTarget 恒等原句柄；两个同句柄代理还原同一 target 对象；isWrapperInstance 真/假；非 wrapper 自省 IAE "not a wrapper instance" | testWrapperIntrospection |
| P1 | equals 恒等语义（同句柄两代理互不相等、hashCode 每实例稳定）、toString 非空、JDK-8 代理是 java.lang.reflect.Proxy / JDK-27 是 hidden class（分侧断言） | testProxyObjectSemantics |
| P1 | default 方法保留自身实现，SAM 路由句柄 | testDefaultMethods |
| P1 | 负例矩阵：非接口/多抽象方法 IAE 消息、null 接口/句柄 NPE、不兼容句柄 WMTE 消息 | testNegatives |

易错点：
- asInterfaceInstance 的句柄类型必须能 asType 到 SAM 签名（返回类型不兼容在创建时就 WMTE，不是调用时）。
- 代理 equals/hashCode 是恒等语义，不是值语义（同句柄两个代理不相等）。
