# java.lang.invoke.MethodType 测试设计案（JDK-8）

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

## 深化批次（2026-11-05）

新增 testIndexBoundaryMessages/testDescriptorValidationMessages/testWrapUnwrapMatrix/
testEraseGenericDeep/testParameterListArrayIndependence/testDescriptorRoundTripMatrix/
testToStringExactForms（93→160 断言）。

| 新增测试点 | 断言 |
|---|---|
| insertParameterTypes(下标==arity) 合法（尾部追加）；越界/负数 → IIOBE 精确消息 `bad index: 4` / `bad index: -1` | testIndexBoundaryMessages |
| dropParameterTypes from>to / end>arity → IIOBE `start=2 end=1` / `start=0 end=4`；零宽区间恒等 | 同上 |
| `(I)` → IAE `bad return type`；`(V)I` → IAE `bad argument type`（与 27 公共子串） | testDescriptorValidationMessages |
| `()Lno/such/Cls;` → TypeNotPresentException（消息含 `no.such.Cls`，双侧一致） | 同上 |
| null loader 语义：解析 bootstrap 类 `Ljava/lang/String;` OK | 同上 |
| wrap/unwrap 全表：8 个原生 × 参数+返回互换恒等；void→Void→void；unwrap 主动把包装类转回原生；非包装引用恒等 | testWrapUnwrapMatrix |
| erase 只擦引用（含包装类），原生与 void 保持；generic 全部转 Object（含 void 返回）；erase/generic 幂等；wrap().erase()==generic() | testEraseGenericDeep |
| parameterList.equals(Arrays.asList(parameterArray()))；两次 parameterArray 独立；改返回数组不影响 type | testParameterListArrayIndependence |
| 描述符往返矩阵 `()V`/`(I)V`/`(IJLjava/lang/String;)Z`/`([[D)Ljava/lang/Object;`/`(Ljava/lang/String;[I)J`；genericMethodType(2,true) 描述符全等 `(Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;` | testDescriptorRoundTripMatrix |
| toString 全等锁定：`(int,String)Object`、`(Object[])void`、`(byte,double[],String[][])char`、`()boolean` | testToStringExactForms |

### 易错规范细节（本批实测锁定）
- asType 拓宽仅限返回值；dropParameterTypes(2,1) 抛 IIOBE 而非 IAE（任务书存疑处实测纠正）。
- `fromMethodDescriptorString("bad")` 消息两侧不同（8 `not a method descriptor: bad` / 27 `bad signature: bad: not a method type`），只断类不断词；`(I)`/`(V)I` 的 `bad return type`/`bad argument type` 两版一致可断子串。
- `wrap()` 把 void 返回包装为 Void.class（非恒等），`unwrap()` 反向还原。
