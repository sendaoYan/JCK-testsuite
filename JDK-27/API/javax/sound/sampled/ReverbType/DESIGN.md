# javax.sound.sampled.ReverbType — DESIGN

## 1. API 功能
`ReverbType` 描述一组混响参数（名称、早反射延迟/强度、晚反射延迟/强度、衰减时间）。方法分组：
- 构造器（受保护）：`ReverbType(String name, int earlyReflectionDelay, float earlyReflectionIntensity, int lateReflectionDelay, float lateReflectionIntensity, int decayTime)`。
- 查询：`getName()`、`getEarlyReflectionDelay()`、`getEarlyReflectionIntensity()`、`getLateReflectionDelay()`、`getLateReflectionIntensity()`、`getDecayTime()`。
- 相等：`equals`/`hashCode` 为 final、身份语义。
- 工具：`toString()`（含名字与各参数）。

## 2. Java 用户如何使用
用户匿名子类化（构造器受保护）：`new ReverbType("Cavern", 10000, -1.4f, 40000, -2.0f, 2250000) {}`，然后逐个 getter 读取参数。混响枚举控件的 `EnumControl.getValues()` 返回 `ReverbType` 数组。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/sound/sampled/ReverbType.java:175`：受保护构造器直接赋值各字段。
- `src/java.desktop/share/classes/javax/sound/sampled/ReverbType.java:279`：`toString()` 以 name 开头并包含 `early reflection delay ... ns ... decay time ...`。
- `test/jdk/javax/sound/sampled/ReverbType/ToString.java`：`new ReverbType("nameToTest", 0, 0, 0, 0, 0) {}` 后断言 `toString().startsWith("nameToTest")`（bug 4385060/8236980）。

## 4. 测试用例设计 + 自动化实现（`ReverbTypeTest.testBlackBox`）
- 匿名子类实例化：6 个 getter 精确往返（含 float 常量相等）。
- `toString()` 以名字开头且包含参数文本。
- 身份相等：参数相同但不同实例不相等、自反、与 null 不等、`hashCode` 稳定。
- 第二个实例验证不同参数独立保留。
