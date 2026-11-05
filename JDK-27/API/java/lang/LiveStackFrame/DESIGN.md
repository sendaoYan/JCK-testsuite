# LiveStackFrame — 设计方案

## 1. API 功能
`java.lang.LiveStackFrame` 是 **package-private 接口**（非 public，java.lang 包内可见），扩展
`java.lang.StackWalker.StackFrame`，补充活栈帧信息：
- 实例方法：`Object[] getMonitors()`、`Object[] getLocals()`、`Object[] getStack()`
- 静态工厂：`getStackWalker()`（及带 `Set<Option>`/`Continuation`/`ContinuationScope` 的重载）
- 嵌套类型 `LiveStackFrame$PrimitiveSlot`：public abstract 类（`size()` 返回 int、`intValue()`、`longValue()`）

## 2. Java 用户如何使用
普通应用代码（java.lang 之外/默认包）**无法直接引用**该类型（包私有）。JDK 内部（如 JVMTI
agent 支持与诊断工具）通过 `LiveStackFrame.getStackWalker(...)` 取得能暴露局部变量槽与
monitor 的 walker。用户侧等价能力 = `StackWalker` + `Option.SHOW_HIDDEN_FRAMES`。

## 3. 开源代码实际用法调研
- OpenJDK `test/jdk/java/lang/StackWalker` 部分诊断用例同包访问 LiveStackFrame；外部生态
  （ASM/JFR 工具）不依赖它。
- javap 实测（27.0.1）：类声明 `interface java.lang.LiveStackFrame extends java.lang.StackWalker$StackFrame`，无 public 修饰。

## 4. 测试用例设计 + 自动化实现（+ 契约深化 2026-11）
因类型不可达，按「结构反射 + 行为替代」两轨：
- **结构（反射）**：`Class.forName("java.lang.LiveStackFrame")` 非 null、isInterface、
  二进制名精确、`!Modifier.isPublic`（记录不可达原因）；`getInterfaces()` 含
  `StackWalker$StackFrame`、`StackFrame.isAssignableFrom`；`getDeclaredMethods()` 含
  getMonitors/getLocals/getStack（abstract）与 static getStackWalker；`getLocals()` 返回
  `Object[]`；嵌套 `PrimitiveSlot` 非 interface、abstract、`size/intValue/longValue` 存在、
  enclosingClass 正确。
- **行为替代（公共 API）**：`StackWalker.Option.SHOW_HIDDEN_FRAMES` walk 首帧
  （className/methodName/fileName/line>0/BCI>=0/非 native 全断）；RETAIN+HIDDEN 下
  `getDeclaringClass()==测试类`、descriptor `()V`；`StackFrame` 公共接口面
  （8 个核心方法存在性）；forEach/deep nesting/nested class frame/getCallerClass/
  与默认 walker 的首帧等价；SHOW_HIDDEN_FRAMES 枚举存在。
- 易错点：PrimitiveSlot 是 **abstract 类**不是接口（javap 实测）；LiveStackFrame 不可
  import，任何 `import java.lang.LiveStackFrame` 都编译失败；行为断言只用公共
  StackWalker/StackFrame 面，避免锁内部实现。

## 注意事项
- 纯 headless；不加 `@key`；断言全部确定性（无 GC/时序依赖）。
