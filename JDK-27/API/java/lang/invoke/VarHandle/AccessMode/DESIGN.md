# java.lang.invoke.VarHandle.AccessMode — 设计方案

## 1. API 功能
访问模式枚举（VarHandle 嵌套 final 枚举）：31 个常量（GET/SET/GET_VOLATILE/.../WEAK_COMPARE_AND_SET_ACQUIRE/GET_AND_BITWISE_XOR_RELEASE 等），`methodName()/valueFromMethodName()` 互转、`valueOf`。

## 2. Java 用户如何使用
```java
VarHandle.AccessMode am = VarHandle.AccessMode.valueFromMethodName("compareAndSet");
if (vh.isAccessModeSupported(am)) ...
```

## 3. 开源代码实际用法调研
- JDK `VarHandle.AccessMode` 枚举定义与 `test/jdk/java/lang/invoke/VarHandles/VarHandleTestAccessMode...`。
- AtomicXxx 的 fullFence/release 序原语按 AccessMode 选择内部路径。

## 4. 测试用例设计 + 自动化实现
白盒：枚举、31 常量、values 长度。
黑盒：methodName↔valueFromMethodName 全量往返；bogus 方法名→IAE、null→NPE；valueOf 往返与 ordinal 稳定；accessModeType(GET)==([II)I、(SET) 返回 void；不支持的原子模式 isAccessModeSupported==false（只读数组视图等）。
对应 `AccessModeTest.java` 的 `testBlackBox()`。

## 深化批次（2026-11-05）

旧 29 断言 → 100 断言（jtreg 全绿）。追加契约维度（`testEnumContract/testMethodNameMatrix/testSupportMatrix/testAccessModeTypeMatrix`）：

### 新覆盖
- **声明序全锁**：31 常量名+ordinal 与 javap/探针实测表逐位锁定（GET, SET, GET_VOLATILE, ..., GET_AND_BITWISE_XOR_ACQUIRE）；toString==name（AccessMode 未覆写 Enum.toString）；compareTo 与声明序严格递增、自身为 0；values() 每次返回防御性拷贝（改副本不影响后续调用）；Enum.valueOf(AccessMode.class, "GET") 查找。
- **methodName() 推导规则**：名字按 '_' 切分，首段全小写、其余段首字母大写——对全部 31 常量按规则推导并断言相等（比逐个硬编码更强）；31 个方法名两两互异（可逆性基础）。
- **valueFromMethodName 负例消息实测**：空串 → IAE "No AccessMode value for method name "（注意尾随空格）；null → NPE（helpful NPE 含 "is null"）。
- **isAccessModeSupported 计数矩阵（与 VarHandleTest 互补）**：int[]/long[] = 31、String[] = 19（31−3 add−9 bitwise）、boolean[] = 28（31−3 add）+ 精确单元格（String[] add/bitwise false、boolean[] bitwise true）。
- **accessModeType 精确 MethodType（数组版）**：GET/GET_VOLATILE=(int[],int)int；SET/SET_OPAQUE=(int[],int,int)void；CAS/WEAK_CAS 族=(int[],int,int,int)boolean；CAE=(int[],int,int,int)int；GET_AND_SET/GET_AND_ADD/BITWISE=(int[],int,int)int；weak CAS 四变体同型；**不支持模式仍返回假想类型**（String[] bitwise/add → (String[],int,String)String）；accessModeType(null) NPE 含 "accessMode"。

### 易错规范细节（实测真值）
- 枚举常量声明序≠javadoc 展示序：以 `values()`/ordinal 实测为准（SET 排第 2、GET_ACQUIRE 在 SET_RELEASE 之前）。
- methodName 与 name 的映射是"驼峰化"而非 toLowerCase：GET_AND_BITWISE_XOR_ACQUIRE → "getAndBitwiseXorAcquire"。
- AccessMode 无 toDisplayName() 方法（javap 核实）；显示名即 Enum.toString()==name，方法名走 methodName()。

### 参考来源补充
- Oracle javadoc java.lang.invoke.VarHandle.AccessMode；OpenJDK `src/java.base/share/classes/java/lang/invoke/VarHandle.java`（AccessMode 枚举声明序、methodName 实现）；JCK runtime-8b `tests/api/java_langinvoke`（枚举全集锁定思路），代码自写。