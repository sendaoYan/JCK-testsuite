# java.lang.invoke.ConstantCallSite — 设计方案

## 1. API 功能
目标永久冻结的调用点：public 构造 `ConstantCallSite(MethodHandle)`（null → NPE）；`getTarget()` 恒等于构造传入的句柄；`dynamicInvoker()` 就是 getTarget()；`setTarget(任意参数)` 恒抛 UnsupportedOperationException（无消息，连 null/同句柄/错型都一样，错型也不会先做 WMTE 检查）。另有 protected `ConstantCallSite(MethodType, MethodHandle createTargetHook)`：构造期间以 `(MethodHandle) hook.invoke(this)` 调用钩子一次产生目标，钩子返回类型必须与 targetType 精确一致。

## 2. Java 用户如何使用
```java
MethodHandle target = lookup.findStatic(Foo.class, "bar", MethodType.methodType(int.class));
ConstantCallSite site = new ConstantCallSite(target);
int v = (int) site.dynamicInvoker().invokeExact(); // 永久走 bar
```

### 参考来源
- Oracle javadoc（8）：https://docs.oracle.com/javase/8/docs/api/java/lang/invoke/ConstantCallSite.html
- Oracle javadoc（27）：https://docs.oracle.com/en/java/javase/27/docs/api/java.lang.invoke/java/lang/invoke/ConstantCallSite.html
- OpenJDK 8u 源码：/data/yansendao/git/jdk8u-dev/jdk/src/share/classes/java/lang/invoke/ConstantCallSite.java
- OpenJDK 27 源码：/data/yansendao/git/jdk/src/java.base/share/classes/java/lang/invoke/ConstantCallSite.java
- JCK runtime-8b：/data/yansendao/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_lang/invoke/ConstantCallSite
（只列路径，不拷内容；JCK 仅作覆盖面对照，代码全部自写。）

## 4. 测试用例设计（含易错规范细节）

| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | getTarget 多次同一对象；dynamicInvoker()==getTarget()==dynamicInvoker() | testPermanentTargetIdentity |
| P0 | setTarget 对 null/同句柄/错型参数一律 UOE 且消息为 null；目标不变 | testSetTargetAlwaysRejects |
| P0 | new ConstantCallSite(null) → NPE | testNullConstructor（27 侧断言消息含 "target" is null） |
| P0 | protected 钩子构造：钩子恰好调用一次、参数就是站点自身、返回句柄被冻结 | testProtectedHookConstructor |
| P0 | 钩子返回类型不匹配 → WMTE "should be of type"；返回 null → NPE；返回非句柄 → CCE "cannot be cast to"；钩子元数不符 → WMTE "cannot convert MethodHandle"；null 钩子 → NPE | testProtectedHookNegatives |
| P1 | type() 等于 target.type()，toString "()String" 精确 | testPermanentTargetIdentity |

易错点：
- UOE 无消息（双侧一致），断言 getMessage()==null 而不是消息片段。
- 钩子在 super() 期间执行：用 static 字段记录调用次数/接收者才能断言。
