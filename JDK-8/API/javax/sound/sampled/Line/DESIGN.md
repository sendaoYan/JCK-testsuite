# javax.sound.sampled.Line — DESIGN

## 1. API 功能
`Line` 是采样音频线路的根接口，直接继承 `java.lang.AutoCloseable`。方法：
- 信息：`getLineInfo()`。
- 生命周期：`open()`、`close()`、`isOpen()`。
- 控制：`getControls()`、`isControlSupported(Control.Type)`、`getControl(Control.Type)`。
- 监听：`addLineListener(LineListener)`、`removeLineListener(LineListener)`。

## 2. Java 用户如何使用
通过 `AudioSystem.getLine(info)` 或具体线路拿到 Line，`line.open(); line.getControls(); line.addLineListener(l); line.close();`，并可作为 `AutoCloseable` 用于 try-with-resources。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/sound/sampled/Line.java:52`：`public interface Line extends AutoCloseable`。
- `test/jdk/javax/sound/sampled/Lines/ToString.java`：`line.toString()` 覆盖。
- `test/jdk/javax/sound/sampled/Controls/ToString.java`：`Control.getType()`/`toString()` 覆盖。

## 4. 测试用例设计 + 自动化实现（`LineTest.testBlackBox`）
- 白盒：接口、直接父接口 `AutoCloseable`、9 个声明方法。
- 黑盒：以 SourceDataLine 为具体 Line，断言 `getLineInfo().getLineClass()`、`isOpen` 状态机、`getControls()` 非空且每个 control 的 type 非空、`isControlSupported` 一致、add/remove 监听器、close 后 `!isOpen`；无线路时跳过。
