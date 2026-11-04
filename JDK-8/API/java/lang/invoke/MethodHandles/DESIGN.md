# java.lang.invoke.MethodHandles 测试设计案（JDK-8）

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

## 深化批次（2026-11-05）

新增 9 个 test 方法（69→115 断言）：

| 测试点 | 方法 |
|---|---|
| identity 同一引用/跨调用稳定/null 直传/type 精确/null 类型 NPE | testIdentityReferenceSemantics |
| constant 同一对象两次 invoke 相等；(Object,null) 合法、(int,null) NPE（原有）、(void,null) → IAE `void type` 精确消息、错类型 → CCE `Cannot cast java.lang.Integer to java.lang.String` | testConstantEdges |
| String[] getter/setter 往返；越界/负下标 → AIOOBE（消息格式两侧不同只断类）；两工厂 handle type 全等 | testArrayElementDeep |
| publicLookup 可达 String.length；unreflect 私有 → IAE `member is private`；包私有嵌套类符号引用 → IAE `symbolic reference class is not`（8 `is not public`/27 `is not accessible` 公共子串） | testPublicLookupAccess |
| lookup/publicLookup/in(String) toString 形态（含类名与 public 标记；27 publicLookup 为 `publicLookup` 后缀） | testLookupToStringExact |
| collectArguments 两位置：类型全等 `(String,int)int`、值 13、null target NPE | testCollectArguments |
| explicitCastArguments 真收窄 long→int（1e12 截断为 -727379968）vs asType 同收窄 WMTE；引用收窄运行时 CCE | testExplicitCastNarrowing |
| 组合子校验矩阵：guardWithTest 非谓词 IAE/catchException 坏 handler IAE/foldArguments 类型不匹配 IAE 消息/filterArguments 过滤器过多 IAE/permuteArguments 长度与越界 IAE | testCombinatorValidation |
| insertArguments 绑定值精确（单位置/多值）；fold 前置语义 negate(a)+a==0 | testInsertArgumentsValue / testFoldPrependSemantics |

### 版本差异（本批实测）
- JDK-8 无 zero/empty/arrayLength/arrayConstructor/dropArgumentsToMatch/dropReturn/loop 家族/tryFinally/
  privateLookupIn（javap 双侧确认，仅 27 断言）。
- publicLookup 模式：8 = PUBLIC；27 = UNCONDITIONAL（无 PUBLIC 位）。
