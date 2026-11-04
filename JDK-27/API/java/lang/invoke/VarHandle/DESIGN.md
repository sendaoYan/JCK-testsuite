# java.lang.invoke.VarHandle — 设计方案

## 1. API 功能
变量句柄（abstract 类）：数组元素/实例字段/静态字段访问，访问模式 31 种（get/set/volatile/acquire/release/opaque/compareAndSet/compareAndExchange/getAndSet/getAndAdd/getAndBitwise*/weak*）；`get/toMethodHandle/accessModeType/isAccessModeSupported/describeConstable/coordinateTypes/varType`。

## 2. Java 用户如何使用
```java
VarHandle vh = MethodHandles.arrayElementVarHandle(int[].class);
int old = (int) vh.getAndAdd(arr, 1, 10);  // 原子加
vh.setRelease(arr, 0, 5);
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/lang/invoke/VarHandles/*`（instance/static/array 三大目录，千行级）。
- java.util.concurrent 原子类（AtomicXxx/StampedLock）内部全部改用 VarHandle 实现内存序原语。

## 4. 测试用例设计 + 自动化实现
白盒：abstract、方法存在、accessModeType 返回 MethodType。
黑盒：数组/实例字段/静态字段三路 get/set/volatile/acquire/release/opaque；compareAndSet 成败两态；compareAndExchange 返回旧值；getAndSet/Add/BitwiseOr 返回旧值；weak CAS；toMethodHandle(GET/SET) 实调；describeConstable 解析可写；coordinateTypes 精确；错坐标类型抛 IAE/CCE。
对应 `VarHandleTest.java` 的 `testBlackBox()`。

## 深化批次（2026-11-05）

旧 38 断言 → 222 断言（jtreg 全绿）。追加契约维度（`testArrayAtomicMatrix/testOtherArrayTypes/testArrayBoundsAndTypeErrors/testFieldHandleContract/testAccessModeSupportMatrix/testInvokeExactBehavior/testStaticFences/testToMethodHandleMatrix/testDescribeConstableDeep`）：

### 新覆盖
- **访问模式全矩阵**：compareAndExchange/Acquire/Release 成败两分支（成功返回 expected 且更新；失败返回当前值不动）；getAndSet(Acquire/Release)、getAndAdd(plain/Acquire/Release) 返回前值+新值精确；getAndBitwiseOr/And/Xor 各 plain/Acquire/Release 三变体旧值+新值逐位精确；weak CAS 4 变体（plain/acquire/release/set）匹配更新值、失配不动值（弱语义不断言 boolean 返回）。
- **API 纠偏（javap 实测）**：VarHandle 无 getAndIncrement/getAndDecrement（区别于 AtomicXxxFieldUpdater），用 getAndAdd(±1) 等价语义覆盖并注释；toMethodHandle 返回 MethodHandle 而非 Optional。
- **数组类型矩阵**：long[]（getAndAdd+位运算）、String[]（volatile/CAS/CAE/getAndSet；位运算 → UnsupportedOperationException，消息即方法名 "getAndBitwiseOr"）、boolean[]（getAndBitwiseOr/And/Xor 真值运算；getAndAdd → UOE "getAndAdd"）；各 varType/coordinateTypes/toString(varType=...) 断言。
- **isAccessModeSupported 全 31 模式矩阵（实测）**：int[]/long[]/int 字段=31 全真；String[]/String 字段=19（缺 GET_AND_ADD×3 + BITWISE×9）；boolean[]/boolean 字段=28（缺 GET_AND_ADD×3）；引用类型保留完整 get/set/CAS/CAE/weak 族。
- **accessModeType 精确 MethodType**：字段版 (Owner)int / (Owner,int)void / (Owner,int,int,int)boolean / (Owner,int,int)int（CAE/GET_AND_ADD/BITWISE_OR 同形）；不支持模式仍返回假想类型（如 String[] 的 (String[],int,String)String）。
- **invoke/exact 行为切换**：hasInvokeExactBehavior 默认 false；withInvokeExactBehavior() 产生新句柄（身份不同）且拒绝装箱坐标/null 值（WMTE "handle's method type ... but found ..."）；withInvokeBehavior() 还原；装箱坐标在 invoke 行为下可用；静态 fullFence/acquireFence/releaseFence/loadLoadFence/storeStoreFence 调用不炸。
- **toMethodHandle 8 模式**：GET/SET/CAS(成败)/CAE/GET_AND_SET/GET_AND_ADD/BITWISE_OR 经 invokeExact 精确值；不支持模式的句柄调用时才抛 UOE；toMethodHandle(null) NPE。
- **describeConstable 深化**：数组句柄 desc == ofArray(CD_int.arrayType())；字段句柄 desc == ofField 构造且 toString 精确 "VarHandleDesc[VarHandleTest$Owner.intField:int]"；resolve 结果与原句柄行为等价（toString/accessModeType 相等）但身份不等——VarHandle 无 equals 覆盖。

### 易错规范细节（实测真值）
| 场景 | 异常 | 消息（片段） |
|---|---|---|
| get/set/CAS/getAndSet 越界 | ArrayIndexOutOfBoundsException | "Index 5 out of bounds for length 3"（含负下标 -1 精确） |
| 调用点形态错（返回 cast/元数/值类型/null 值） | WrongMethodTypeException | "cannot convert MethodHandle(VarHandle,int[],int)int to ..." |
| 数组坐标类型错（invoke 行为） | ClassCastException | "Cannot cast java.lang.String to [I" |
| 字段接收者类型错 | ClassCastException | "Cannot cast java.lang.String to VarHandleTest$Owner" |
| 字段 null 接收者 | NullPointerException | "null" |
| 数组 null | NullPointerException | "Cannot load from int array because \"array\" is null" |
| 不支持的模式调用 | UnsupportedOperationException | 消息即方法名（"getAndAdd"/"getAndBitwiseOr"） |
| isAccessModeSupported/accessModeType/toMethodHandle 传 null | NullPointerException | 含 "accessMode" |

- 字段句柄 toString 实测**不含字段名**："VarHandle[varType=int, coord=[class VarHandleTest$Owner]]"（曾误以为含字段名——已按实测断言其不含）。

### 参考来源补充
- Oracle javadoc java.lang.invoke.VarHandle（内存序表、访问模式兼容性矩阵、WMTE/CCE 判定规则）。
- OpenJDK 源码 `src/java.base/share/classes/java/lang/invoke/VarHandle.java`、`VarHandleGuards`、`MethodHandle.java`（asType 消息格式）。
- OpenJDK 回归 `test/jdk/java/lang/invoke/VarHandles/`（access-mode 矩阵分段思路）；JCK runtime-8b `tests/api/java_langinvoke/VarHandle` 伞形结构借鉴，代码自写。