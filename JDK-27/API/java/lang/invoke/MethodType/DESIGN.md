# java.lang.invoke.MethodType 测试设计案（JDK-27）

> 覆盖目标：完整契约。6 个 methodType 工厂重载、genericMethodType 变体、工厂校验（null rtype/ptype/元素→NPE、
> void 参数→IAE、负泛型元数→IAE）、结构变异器（changeParameterType/insert/append/drop/changeReturnType 含 List
> 与下标边界）、hasPrimitives/hasWrappers、erase/generic/wrap/unwrap、访问器（parameterType/parameterCount/
> returnType/parameterList/parameterArray）、equals/hashCode/toString 精确格式、from/toMethodDescriptorString 往返
> 与畸形描述符→IAE/null→NPE。

## 1. API 功能

- 工厂：`methodType` 6 重载 + `genericMethodType(int[,boolean])`。
- 结构变异：changeParameterType/insert/append/drop/changeReturnType。
- 转换：erase/generic/wrap/unwrap；访问器 + 描述符 from/toMethodDescriptorString。

## 2. 用户如何使用

- `MethodType.methodType(ret, paramTypes...)` 描述方法签名；`genericMethodType(n)` 描述 Object 泛型签名。
- `toMethodDescriptorString()`/`fromMethodDescriptorString()` 与 JVM 描述符互转。

## 3. 参考来源

- Oracle Java SE 8 javadoc（java/lang/invoke/MethodType.html）。
- OpenJDK 8 `~/git/jdk8u-dev/jdk/src/share/classes/java/lang/invoke/MethodType.java`。
- JCK runtime-8b `tests/api/java_lang/invoke/MethodType/`。

## 4. 测试用例设计（映射到 MethodTypeTest.java）

| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | 6 工厂重载 + 跨形式等价 | testFactories |
| P0 | genericMethodType 变体 | testGenericMethodType |
| P0 | 工厂校验异常 | testFactoryValidation |
| P0 | 结构变异器 + 边界 | testStructuralMutators |
| P0 | erase/generic/wrap/unwrap | testEraseGenericWrapUnwrap |
| P0 | hasPrimitives/hasWrappers | testHasPrimitivesAndWrappers |
| P0 | 访问器 + parameterArray 副本语义 | testAccessors |
| P0 | equals/hashCode | testEqualsHashCode |
| P0 | toString 精确格式 | testToString |
| P0 | 描述符往返 + 畸形/null | testDescriptorString |

## 易错规范细节（已锁入断言）

- void 不能作为参数类型 → IAE；void 返回合法。
- `parameterList()` 不可变（add/set → UOE）；`parameterArray()` 返回副本。
- `dropParameterTypes(1,3)` 合法（end==count）；start>end 或越界 → IndexOutOfBoundsException。
- JDK-8 `fromMethodDescriptorString("(I",null)` → IAE（JDK-27 改为 StringIndexOutOfBoundsException）。

## JDK-27 差异（已锁入断言）

- `fromMethodDescriptorString("(I",null)` → `StringIndexOutOfBoundsException`（JDK-8 为 IAE）；
  `"()"`/`"(V)I"` → IAE。
- 新增 `lastParameterType()`（零参数返回 void）、`descriptorString()`、`describeConstable()`（testJdk27Accessors）。
