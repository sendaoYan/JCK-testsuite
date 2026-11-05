# javax.sound.sampled.FloatControl — DESIGN

## 1. API 功能
`FloatControl` 是抽象类，继承 `Control`，表示在一个浮点区间上连续调节的控件（如增益、声像）。方法分组：
- 构造器（受保护）：8 参 `FloatControl(Type, min, max, precision, updatePeriod, initialValue, units, minLabel, midLabel, maxLabel)`、6 参（标签默认为空串）。构造时校验 `min <= max` 且 `initialValue` 在范围内。
- 值操作：`setValue(float)`（越界抛 `IllegalArgumentException`）、`getValue()`、`shift(float from, float to, int microseconds)`（校验 `from`、`setValue(to)`）。
- 查询：`getMaximum()`、`getMinimum()`、`getUnits()`、`getMinLabel()`、`getMidLabel()`、`getMaxLabel()`、`getPrecision()`、`getUpdatePeriod()`。
- 工具：`toString()`。

## 2. Java 用户如何使用
用户匿名子类化：`new FloatControl(FloatControl.Type.MASTER_GAIN, -80f, 6f, 0.1f, 1000, 0f, "dB", "Min", "Mid", "Max") {}`，然后 `getMinimum()/getMaximum()` → `setValue(3.5f)` → `shift(0f, -20f, 5000)` → 越界抛异常。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码）
- `src/java.desktop/share/classes/javax/sound/sampled/FloatControl.java:131`：构造器校验 `minimum > maximum`、`initialValue < minimum`、`initialValue > maximum` 均抛 `IllegalArgumentException`。
- `src/java.desktop/share/classes/javax/sound/sampled/FloatControl.java:196`：`setValue` 越界抛 `IllegalArgumentException`。
- `src/java.desktop/share/classes/javax/sound/sampled/FloatControl.java:319`：`shift` 先校验 `from`，`to` 交由 `setValue` 校验，最终置为 `to`。

## 4. 测试用例设计 + 自动化实现（`FloatControlTest.testBlackBox`）
- 8 参构造器各字段精确往返（含浮点常量相等）。
- `setValue` 往返、在最小/最大边界；越界上/下抛 `IllegalArgumentException`。
- `shift`：合法时置 `to`；`from` 越下界、`to` 越上界分别抛异常。
- 6 参构造器：空标签、`updatePeriod -1`、初始值往返。
- 构造器校验：`min > max`、`initialValue` 越界均抛异常。
- `toString()` 含单位。
