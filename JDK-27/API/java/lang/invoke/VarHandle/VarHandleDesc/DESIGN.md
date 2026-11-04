# java.lang.invoke.VarHandle.VarHandleDesc — 设计方案

## 1. API 功能
VarHandle 名义描述（VarHandle 嵌套 final 类，实现 ConstantDesc）：`ofField/ofStaticField/ofArray` 工厂、`varType/coordinateTypes`、`equals/hashCode/toString`、`resolveConstantDesc`。

## 2. Java 用户如何使用
```java
VarHandleDesc d = VarHandleDesc.ofField(ClassDesc.of("Point"), "x", CD_int);
VarHandle vh = d.resolveConstantDesc(MethodHandles.lookup());
vh.set(p, 8);
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/lang/invoke/VarHandleDescTest.java` 与 condy 常量池序列化。
- javac 把 `ldc VarHandle`（字段/数组常量）编码为 VarHandleDesc 常量。

## 4. 测试用例设计 + 自动化实现
白盒：final、实现 ConstantDesc、三工厂静态。
黑盒：ofField 与现场 describeConstable equals；resolve 得到可写句柄（set x=8）；ofStaticField get/set；ofArray 读写；varType/coordinateTypes 精确；值语义 equals/hashCode；ofField null 检查。
对应 `VarHandleDescTest.java` 的 `testBlackBox()`。

## 深化批次（2026-11-05）

旧 23 断言 → 71 断言（jtreg 全绿）。追加契约维度（`testToStringAndVarType/testNestedArrayDesc/testEqualitySemantics/testDescribeConstableRoundTrip/testNegative`）：

### 新覆盖
- **toString 精确格式（实测）**：ofField → "VarHandleDesc[VarHandleDescTest$Holder.x:int]"；ofStaticField → "VarHandleDesc[static VarHandleDescTest.stat:int]"；ofArray(String[]) → "VarHandleDesc[String[][]]"（注意数组描述双括号：组件名+[] 再由 Desc 加 []）；int[] → "VarHandleDesc[int[][]]"；int[][] 嵌套 → "VarHandleDesc[int[][][]]"。
- **varType 语义**：ofArray 的 varType 是**元素类型**（String[] 句柄 varType 描述符 "Ljava/lang/String;"；int[][] 句柄 varType 描述符 "[I" 即 int[]）；displayName（CD_int → "int"）。
- **嵌套数组闭环**：ofArray(CD_int.arrayType().arrayType()) 解析后 coordinateTypes=(int[][].class,int)、varType==int[].class，set 传 int[] 值、get 返回 int[]，读写精确。
- **equals/hashCode 值语义**：同参相等且对称、hashCode 一致；字段名/varType/声明类/工厂种类（字段 vs 静态 vs 数组）任一不同即不等；equals(null)/异类 false；instanceof ConstantDesc。
- **describeConstable 双向闭环**：findVarHandle 的 desc == ofField 构造；findStaticVarHandle 的 desc == ofStaticField 构造；arrayElementVarHandle 的 desc == ofArray 构造；同一 desc 两次 resolve 产生两个不同实例，且 **VarHandle 无 equals 覆盖**（r1.equals(r2)==false）——行为等价用 toString/accessModeType/varType/coordinateTypes 断言，两个 resolved 句柄观察同一字段（r1.set 后 r2.get 可见）。
- **null 与解析失败契约（实测）**：三工厂 null 参数 → NPE；ofField 未知类 → ClassNotFoundException（消息即二进制名）；未知字段 → NoSuchFieldException "no such field: <类>.<字段>/<类型>/getField"；varType 不匹配同样 NSFE（"Holder.x/long"）；ofArray(非数组 CD) → IAE "Array class argument not an array"；publicLookup 解析应用类 owner → ClassNotFoundException（publicLookup 经 bootstrap 加载器视角看不到类路径上的类，实测机制记录）。

### 易错规范细节（实测真值）
- **VarHandleDesc 无 compareTo**：ConstantDesc/DynamicConstantDesc 均不实现 Comparable（javap 核实），不可照搬 Integer.compareTo 式断言。
- toString 中嵌套类用二进制名（外类$嵌套），静态字段带 "static " 前缀。
- ofArray 参数是**数组类型**的 ClassDesc（CD_int.arrayType()），非组件类型；传非数组解析时抛 IAE。
- describeConstable().get() 的相等性是 VarHandleDesc 值语义（equals 覆写 final），而 VarHandle 本体无 equals——两者不可混用同一断言方式。

### 参考来源补充
- Oracle javadoc java.lang.invoke.VarHandle.VarHandleDesc 与 java.lang.constant.DynamicConstantDesc；OpenJDK `src/java.base/share/classes/java/lang/invoke/VarHandle.java`（VarHandleDesc.toString/resolveConstantDesc 实现）；OpenJDK 回归 `test/jdk/java/lang/invoke/constant/`（condy 名义描述动态解析思路）；JCK runtime-8b `tests/api/java_langinvoke`（负例分段思路），代码自写。