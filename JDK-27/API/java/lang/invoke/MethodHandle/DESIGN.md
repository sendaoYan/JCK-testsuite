# java.lang.invoke.MethodHandle 测试设计案（JDK-27）

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

## JDK-27 差异（已锁入断言）

- 新增 `withVarargs(boolean)`：可清除/恢复 varargs collector（testWithVarargs）。

## 深化批次（2026-11-05）

与 JDK-8 侧拉平 + JDK-9+ 组合子（55→119 断言）：
- 同步新增 testInvokeExactReturnContract / testAsTypeConversions / testInvokeCallSiteWidening /
  testInvokeWithArgumentsTypes / testFilterArgumentsMulti / testFoldArgumentsValue /
  testInsertDropArgumentsValue / testPermuteArgumentsValue / testGuardWithTestBranches /
  testCatchExceptionPrecise / testBindToAndToString / testCollectorSpreaderRoundTrip
  （内容与 8 侧一致，WMTE 消息子串 `but found`、`cannot convert ...` 两侧相同）。
- **testTryFinallyCombinator**（27 独有 API）：tryFinally 正常/异常两条路径各触发一次 cleanup
  （计数==2）、异常原样传播、cleanup 返回类型不符 → IAE `target and return types must match: void != int`。
- **testLoopCombinators**（27 独有 API）：whileLoop(constant(0), i<5, i+1) 类型 ()int 结果 5；
  countedLoop(5 次, 累加器 acc+i+1) 结果 15（1..5 求和）。

### 版本差异（本批实测）
- loop/whileLoop/countedLoop/tryFinally 为 JDK-9+ API，**JDK-8 无**（任务书称"双侧 9+ 都在"，实测 8 侧
  javap 确认不存在，只在 27 断言）。
- `(Object)invokeExact` WMTE 消息：8 `expected (Calc,String)String but found ...`；27 `handle's method
  type (Calc,String)String but found ...`——公共子串 `but found` 已两侧断言。
- 其余组合子值语义与负例消息两侧一致。
