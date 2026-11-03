# LiveStackFrame.PrimitiveSlot — 设计方案

## 1. API 功能
`java.lang.LiveStackFrame.PrimitiveSlot`（public abstract，JDK 内部扩展 StackWalker 用）表示栈帧局部变量表/操作数栈中的一个原始类型槽：`int size()`（抽象，槽宽 4 或 8 字节）、`int intValue()`/`long longValue()`（具体，按槽宽取值）。所属接口 `LiveStackFrame` 为包私有。

## 2. Java 用户如何使用
- 非导出内部 API：外部代码无法获得实例（构造器包私有、工厂在不可达接口上）；JDK 内部由带 `ExtendedOption.LOCALS_AND_OPERANDS` 的 StackWalker 产出，调试器/async-profiler 类工具经内部路径消费。

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/lang/StackWalker` 中 `LiveStackFrame` 系列仅以 `-XaddExports`/内部类访问；无第三方直接使用 `PrimitiveSlot` 的公开案例（javap 证实：`java.lang.LiveStackFrame` 为 `interface`（包私有），`PrimitiveSlot()` 构造器包私有）。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName("java.lang.LiveStackFrame$PrimitiveSlot")` 验证 public abstract、唯一包私有构造器、`size()` public abstract 返回 int、`intValue/longValue` public 具体方法及返回类型。
黑盒：受包私有限制无法实例化（无公开工厂/构造器），仅验证 bootstrap 加载、类型形态（非接口/枚举/注解）、包名为 java.lang，并在测试注释与本设计中说明原因。
对应 `PrimitiveSlotTest.java` 白盒 + 黑盒部分。
