# java.lang.invoke.MethodHandle 测试设计案（JDK-8）

> 覆盖目标：完整契约。type() 签名、invokeExact 精确类型 + 错类型/元数→WrongMethodTypeException、invoke 的
> 宽松适配（装箱/拆箱/拓宽）、invokeWithArguments（varargs + List 形式）、asType、bindTo 接收者绑定、
> asCollector/asSpreader 数组适配、asVarargsCollector/isVarargsCollector/asFixedArity、void 返回、toString。

## 1. API 功能

- `type()`；`invokeExact`/`invoke`/`invokeWithArguments`（varargs + List）。
- 适配：`asType`/`bindTo`/`asCollector`/`asSpreader`/`asVarargsCollector`/`asFixedArity`。

## 2. 用户如何使用

- `LOOKUP.findVirtual(C,"m",mt)` 得到 handle；`invokeExact` 要求参数精确匹配，`invoke` 允许适配。
- `bindTo(x)` 绑定首参；`asCollector`/`asSpreader` 在数组与多参数间转换。

## 3. 参考来源

- Oracle Java SE 8 javadoc（java/lang/invoke/MethodHandle.html）。
- OpenJDK 8 `~/git/jdk8u-dev/jdk/src/share/classes/java/lang/invoke/MethodHandle.java`。
- JCK runtime-8b `tests/api/java_lang/invoke/MethodHandle/`。

## 4. 测试用例设计（映射到 MethodHandleTest.java）

| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | type() 签名（findVirtual 前置接收者）| testType |
| P0 | invokeExact 精确 + WMTE | testInvokeExact |
| P0 | invoke 宽松适配 + WMTE | testInvoke |
| P0 | invokeWithArguments varargs/List/null | testInvokeWithArguments |
| P0 | asType | testAsType |
| P0 | bindTo | testBindTo |
| P0 | asCollector | testAsCollector |
| P0 | asSpreader | testAsSpreader |
| P0 | varargs collector | testVarargsCollector |
| P1 | void 返回 + toString | testVoidInvoke |

## 易错规范细节（已锁入断言）

- findVirtual 的 handle 类型有 3 参数（接收者前置）。
- `invokeExact` 装箱参数/错元数 → WMTE（非 CCE）；`invoke` 可适配装箱/拓宽，但不可适配 String→int（WMTE）。
- `invokeWithArguments((Object[])null)` → WMTE；null List → NPE。
- asCollector 要求数组作为尾参；asSpreader 要求 arrayLength<=paramCount，负长→IAE。
- findStatic 于 varargs 方法默认得 varargs collector。

## 深化批次（2026-11-05）

新增 12 个 test 方法（52→107 断言）：

| 测试点 | 方法 |
|---|---|
| (Object)invokeExact 于 (String)String 句柄 → WMTE（消息含 `but found`；asType 适配后 OK）；(long) 于 int 返回 → WMTE | testInvokeExactReturnContract |
| asType 矩阵：返回拓宽 int→long OK；参数拓宽 int→long → WMTE `cannot convert MethodHandle(int)int to (long)int`；返回收窄/元数不符 → WMTE；asType(原类型) 返回同一句柄 | testAsTypeConversions |
| invoke 调用点拓宽（int 实参喂 long 形参）、byte/short 拓宽 | testInvokeCallSiteWidening |
| invokeWithArguments：散参==List 等价；参数过多 → WMTE 精确消息；String 喂 int → CCE `cannot be cast to`；null 原生实参 → NPE | testInvokeWithArgumentsTypes |
| filterArguments 双过滤器/单位置/类型变换过滤器（(String,String)int）；过滤器过多 → IAE `too many filters` | testFilterArgumentsMulti |
| foldArguments 组合器结果作首参：concat(tag(s),s)=="a!a"；类型不符 → IAE `target and combiner types must match` | testFoldArgumentsValue |
| insertArguments 绑定值精确/多值绑定/越界 IAE/值类型错 CCE；dropArguments 前后丢参透传精确 | testInsertDropArgumentsValue |
| permuteArguments 轮转 (a,b,c)→"c|a|b"、索引重复 "a|a|a"、数组长度/越界 IAE | testPermuteArgumentsValue |
| guardWithTest 真假分支各走一次（计数器）、type==target type、非 boolean 谓词 → IAE `guard type is not a predicate` | testGuardWithTestBranches |
| catchException 正常路径值/受检异常进 handler 值/类型不符原样直传（消息全等）/handler 不收异常 → IAE | testCatchExceptionPrecise |
| bindTo 错类型 → CCE `Cannot cast java.lang.String`；bindTo(null) 构造 OK、invoke 时 NPE；toString 全等 `MethodHandle(Calc,int,int)int`（**不含方法名**，任务书预期修正） | testBindToAndToString |
| asCollector+asSpreader 往复原类型同值；负长 → IAE `array length is not legal: -1` | testCollectorSpreaderRoundTrip |

### 易错规范细节（本批实测锁定）
- **asType 不允许参数拓宽**（仅返回可拓宽、参数可装箱/转型）；invoke 调用点转换比 asType 宽（byte→int、int→long 均可）——两者契约差异已锁定。
- invokeWithArguments 参数不可转换抛 **CCE**（转换路径经 Number），非 WMTE；参数过多才是 WMTE。
- `int r = mh.invokeExact(...)` 在 try 块中 javac 报 Object→int，须用显式 `(int)` 强转写法。
