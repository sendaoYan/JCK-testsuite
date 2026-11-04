# java.lang.invoke.MethodHandles 测试设计案（JDK-27）

> 覆盖目标：完整契约。Lookup（lookup/publicLookup/lookupClass/lookupModes/in/toString）、find（static/virtual/
> constructor/getter/setter/special/bind）、unreflect（method/constructor/getter/setter）、revealDirect/reflectAs、
> identity/constant、arrayElementGetter/Setter + 校验矩阵、insertArguments/dropArguments/filterArguments/
> filterReturnValue/foldArguments/guardWithTest/catchException/throwException/permuteArguments/explicitCastArguments、
> exactInvoker/invoker/spreadInvoker。

## 1. API 功能

- Lookup 创建与模式（PUBLIC/PRIVATE/PROTECTED/PACKAGE）、find*/unreflect*/bind。
- 组合器：insert/drop/filter/fold/guard/catch/throw/permute/explicitCastArguments + invokers。

## 2. 用户如何使用

- `MethodHandles.lookup()` 得到当前类 Lookup；`findStatic/findVirtual/findConstructor` 解析成员。
- 组合器把简单 handle 组合成复杂行为（参数插拔、过滤、守卫、异常捕获）。

## 3. 参考来源

- Oracle Java SE 8 javadoc（java/lang/invoke/MethodHandles.html + Lookup）。
- OpenJDK 8 `~/git/jdk8u-dev/jdk/src/share/classes/java/lang/invoke/MethodHandles.java`。
- JCK runtime-8b `tests/api/java_lang/invoke/MethodHandles/`。

## 4. 测试用例设计（映射到 MethodHandlesTest.java）

| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | Lookup 模式/in/publicLookup | testLookup |
| P0 | find 全家族 | testFind |
| P0 | unreflect 全家族 | testUnreflect |
| P0 | revealDirect/reflectAs | testRevealDirectReflectAs |
| P0 | identity/constant | testIdentityConstant |
| P0 | arrayElementGetter/Setter + 校验 | testArrayElementAccessors |
| P0 | insert/drop/filter/fold | testInsertDropArguments / testFilterArguments / testFilterReturnValue / testFoldArguments |
| P0 | guardWithTest/catchException/throwException | testGuardWithTest / testCatchException / testThrowException |
| P0 | permuteArguments/explicitCastArguments | testPermuteArguments / testExplicitCastArguments |
| P1 | exactInvoker/invoker/spreadInvoker | testInvokers |

## 易错规范细节（已锁入断言）

- `in(String.class)` 丢弃 PRIVATE/PACKAGE 模式；findSpecial 需 `LOOKUP.in(Child.class)`。
- `bind()` 需方法名（非字段）；`throwException` handle 类型为 `(exType)returnType`。
- `permuteArguments` reorder.length==旧参数个数；`revealDirect` 对非 direct handle → IAE。
- JDK-8 publicLookup 仅 PUBLIC 模式（JDK-27 改为 UNCONDITIONAL 0x20）。

## JDK-27 差异（已锁入断言）

- `publicLookup()` 返回 UNCONDITIONAL 模式（0x20），无 PUBLIC 位（JDK-8 为 PUBLIC）。
- 新增 `zero`/`empty`/`arrayConstructor`、Lookup `MODULE`/`UNCONDITIONAL` 常量、`hasFullPrivilegeAccess`（testJdk27Methods）。

## 深化批次（2026-11-05）

与 JDK-8 侧拉平 + JDK-9+ 工厂深化（77→145 断言）：
- 同步新增 identity 引用语义、constant 边（void IAE `void type`/错类型 CCE 精确消息）、
  String[] 数组往返 + AIOOBE、publicLookup 拒绝矩阵（`member is private`/`symbolic reference class is not`）、
  toString 形态、collectArguments 两位置、explicitCastArguments 真收窄 -727379968 vs asType WMTE、
  组合子校验矩阵、insertArguments 绑定、fold 前置（与 8 侧同内容）。
- **testJdk27FactoryDeep**（27 独有）：arrayLength（(int[])int 类型、长度 3/1/0、非数组 IAE
  `not an array: int`、null NPE）；arrayConstructor（类型+分配长度）；zero（int 0/boolean false/
  String null/void 类型 ()void/null NPE）；empty（(int,int)int invokeExact 返 0、void 正常返回、
  引用返 null、null NPE）；dropArgumentsToMatch（类型+值 `a-b-c`+不匹配 IAE `argument lists do not match`）；
  dropReturn（类型 ()void + 正常调用）。

### 版本差异（本批实测）
- 上述工厂与 dropArgumentsToMatch/dropReturn 均为 JDK-9+ API；JDK-8 无（8 侧 DESIGN 已记录）。
- publicLookup 模式位与 toString 后缀差异见上。
