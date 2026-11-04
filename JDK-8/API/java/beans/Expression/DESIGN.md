# java.beans.Expression — 设计案（JDK-8）

## 一、API 功能
`Expression extends Statement`：表示"对 target 调用 methodName(args) 并产生结果"的原语表达式。
- 构造器：`(Object target, String methodName, Object[] arguments)`、`(Object value, Object target, String methodName, Object[] arguments)`（预置 value，getValue 不再求值）。
- `getValue()`：value 未绑定时动态分派方法求值并缓存；`setValue(Object)` 绑定/覆盖；`execute()` 求值并把结果写入 value（即使已有预置值也会重算）。
- 继承 Statement 的 getTarget/getMethodName/getArguments（getArguments 每次返回防御性克隆）。

## 二、使用方式
`new Expression("hello", "toUpperCase", new Object[0]).getValue()` 求值；XMLEncoder 持久化用它表示对象构造（methodName="new"）与取值；IDE 连线工具配合 EventHandler 使用。

## 三、参考来源
- Oracle Java SE 8 javadoc：java.beans.Expression / Statement（"new"/"get"/"set"/Class.forName 特例、缓存语义"value 已绑定则不再求值"）。
- JavaBeans 规范 1.01-A（第 5、8 章：方法派发与持久化原语）。
- OpenJDK 回归思路：`test/jdk/java/beans/Statement/ClassForName`（forName 特例）、`Test4530962`（方法查找歧义）、`XMLEncoder/java_beans_Expression.java`（编码往返）。本文件测试为自写，未拷贝任何代码。
- JDK-8 实现（temurin 8u372 src.zip）逐行核实：`arguments == null ? emptyArray : arguments.clone()`；`value == unbound` 判定；`unbound` 为包私有哨兵对象，null 亦视为"已绑定"。

## 四、测试用例设计 + 易错规范细节
测试方法与覆盖（87 断言）：
1. **testWhiteBox**：extends Statement、public、公开方法集合含 getValue/setValue/execute。
2. **testAccessorsAndCtor**：target/methodName/args 恒等；构造器克隆参数数组（外部改动不影响）；getArguments 每次返回新克隆且可被安全改动；null args 归一为空数组并与空数组等价；构造器不做任何校验（null target/methodName/args 均可构造，求值时才抛）；预置 value 构造器原样返回且稳定。
3. **testDispatchByArityAndSpecificity**：substring(int) vs substring(int,int) 按实参个数选择；concat；`pick(String)` vs `pick(Object)` 最特定者胜；**null 实参 + 两个可匹配重载 → NoSuchMethodException（歧义）**；null 实参匹配唯一签名方法（takeNull 收到 null）。
4. **testCachingSemantics**（易错）：首次 getValue 后 target 变更 → 仍返回旧值（缓存）；setValue 覆盖缓存且免疫 target 变更；`setValue(null)` 后 getValue 恒为 null（null 也是"已绑定"，哨兵机制不再求值）；execute() 求值一次、之后 getValue 不重算（副作用计数器证明）；execute() 会覆盖预置值重算。
5. **testStaticAndClassTargets**：Math.max/Math.abs（Integer 实参自动装箱匹配 int 形参）；Integer.parseInt；Class 作 target 时可调 Class.class 的实例方法（String.class getSuperclass → Object.class）；`Class.class, "forName"` 特例。
6. **testNewForms**（易错）：methodName="new" → 构造器派发（StringBuilder("abc")）；数组类 + "new" → 按实参个数建数组（含 int[] 装箱逐元素写入）；Character+String 特例取首字符；Integer("123")；无参 "new" 走 Class.newInstance 回退（public 无参构造 OK）；接口 → InstantiationException；错误元数 → NoSuchMethodException；包私有构造器 → IllegalAccessException（原样透传，非包装 Exception）。
7. **testArrayAccess**：数组 target + "get" 读元素（String[]/int[] 装箱/boolean[]）；越界 → ArrayIndexOutOfBoundsException；`"length"` 不是方法 → NoSuchMethodException。
8. **testBoxingAndWidening**（易错）：只做拆装箱不做拓宽 —— Long 匹配 long、Integer 不匹配 long（NoSuchMethodException）；null 给 primitive 形参 → IllegalArgumentException（invoke 阶段）；Integer 匹配 Object。
9. **testExceptionMatrix**：被调方法抛出的异常**同实例原样透传**（ISE token 恒等断言）；未知方法 → NoSuchMethodException（直接抛、不包装）；null target / null methodName → NPE 在**求值时**（"target/methodName should not be null"）；"class" 非保留名；同元数但类型不匹配 → NoSuchMethodException。
10. **testToString**：未求值前缀 `<unbound>=`；String target 加引号；求值后值按类型名（Integer=）；setValue("V") 后值加引号。

### 易错规范细节（实证记录）
- 异常类型表（JDK-8 与 JDK-27 一致）：未知方法/属性名/字段名 → `NoSuchMethodException`；null target/methodName → `NullPointerException`（求值时）；方法体异常 → **原始异常同实例透传**；null→primitive 形参 → `IllegalArgumentException`；数组越界 → `ArrayIndexOutOfBoundsException`；私有构造器 → `IllegalAccessException`；接口实例化 → `InstantiationException`。
- **版本差异（实证）**：`new Expression(Class.class, "forName", {"java.lang.String", Boolean.TRUE})` —— JDK-8 无元数守卫，仍按 args[0] 解析返回 String.class；JDK-27 有 `arguments.length == 1` 守卫，落入常规查找 → NoSuchMethodException。两侧文件各自断言本侧行为。
- NPE/异常 message 双侧措辞不同（JDK-14+ helpful NPE），一律只断言类型不断言消息。
- varargs 目标方法的"展开"形式（如 sum(1,2,3)）匹配失败以 IllegalArgumentException 收场（JDK-8/27 同）——未纳入断言，记录备查。
