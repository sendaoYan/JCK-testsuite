# java.lang.invoke.CallSite — 设计方案

## 1. API 功能
invokedynamic 调用点的抽象基类：`type()` 返回站点的永久 MethodType；`getTarget()/setTarget()` 是子类必须实现的核心契约（抽象方法）；`dynamicInvoker()` 返回一个"每次调用都重新分派到当前 target"的多态入口句柄。构造器全部非 public（包私有），只能经 ConstantCallSite/MutableCallSite/VolatileCallSite 或子类使用。

## 2. Java 用户如何使用
```java
MutableCallSite site = new MutableCallSite(MethodType.methodType(int.class));
site.setTarget(MethodHandles.constant(int.class, 42));
int v = (int) site.dynamicInvoker().invokeExact(); // 42
```

### 参考来源
- Oracle javadoc（8）：https://docs.oracle.com/javase/8/docs/api/java/lang/invoke/CallSite.html
- Oracle javadoc（27）：https://docs.oracle.com/en/java/javase/27/docs/api/java.lang.invoke/java/lang/invoke/CallSite.html
- OpenJDK 8u 源码：/data/yansendao/git/jdk8u-dev/jdk/src/share/classes/java/lang/invoke/CallSite.java
- OpenJDK 27 源码：/data/yansendao/git/jdk/src/java.base/share/classes/java/lang/invoke/CallSite.java
- JCK runtime-8b：/data/yansendao/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_lang/invoke/CallSite
（只列路径，不拷内容；JCK 仅作覆盖面对照，代码全部自写。）

## 4. 测试用例设计（含易错规范细节）

| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | type() 与构造 MethodType 精确同一（==） | testCallSite / testTypeConstructorPlaceholder |
| P0 | setTarget(null) → NPE；setTarget 类型不匹配 → WMTE（消息含 "should be of type"），失败后站点不变 | testSetTargetNegativeMatrix |
| P0 | dynamicInvoker() 每次产生的句柄都跟随 setTarget 翻转（多态间接性：缓存的旧句柄也走新分支） | testDynamicInvokerContract / testTargetRoundTrip |
| P1 | dynamicInvoker 错型 invokeExact → WMTE（消息含 "but found ()Object"） | testDynamicInvokerContract |
| P1 | 抽象性：getTarget/setTarget/dynamicInvoker 为 abstract、type() 具体、无 public 构造器 | testAbstractContract |
| P1 | ConstantCallSite 存在 protected (MethodType,MethodHandle) 构造（子类自绑定钩子） | testAbstractContract |
| P1 | 三子类互为兄弟、均为 CallSite 直接子类 | testSubclassHierarchy |
| P2 | MethodType 构造的占位 target：类型精确、调用抛 IllegalStateException "uninitialized call site"（注意占位句柄有参数时 invoke 需带实参，否则先触发 WMTE） | testTypeConstructorPlaceholder |

易错点：
- 占位 target 用 `invoke()` 不带参调用 `(int)String` 型句柄会先在适配阶段抛 WMTE，测 ISE 必须带实参。
- MutableCallSite.dynamicInvoker() 每次返回新句柄对象（不 ==），但 ConstantCallSite.dynamicInvoker()==getTarget()（同一对象）。
