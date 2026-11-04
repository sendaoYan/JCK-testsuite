# javax.sound.sampled.DataLine — DESIGN

## 1. API 功能
`DataLine` 是 `Line` 的子接口，为带数字音频数据的线路（SourceDataLine/TargetDataLine/Clip 的公共契约）。方法：
- 传输：`drain()`、`flush()`、`start()`、`stop()`。
- 状态：`isRunning()`、`isActive()`。
- 查询：`getFormat()`、`getBufferSize()`、`available()`、`getFramePosition()`、`getLongFramePosition()`、`getMicrosecondPosition()`、`getLevel()`。

## 2. Java 用户如何使用
通过具体实现（如 `AudioSystem.getSourceDataLine(fmt)`）拿到 `DataLine`，然后 `open → start → write/read → drain → stop → close`，期间用 `getBufferSize/getFormat/available/getFramePosition/getLevel` 观察。

## 3. 开源代码实际用法调研（来源：jdk25u-dev test/jdk）
- `test/jdk/javax/sound/sampled/DataLine/LongFramePosition.java`：`SourceDataLine sdl = AudioSystem.getSourceDataLine(format); sdl.getFramePosition(); sdl.getLongFramePosition()`。
- `test/jdk/javax/sound/sampled/DataLine/DataLineInfoNegBufferSize.java`：构造 `DataLine.Info(Clip.class, f, bufferSize)` 并检查负 bufferSize 归一为 `NOT_SPECIFIED`。
- `test/jdk/javax/sound/sampled/DataLine/LineDefFormat.java`：`DataLine.Info` 默认格式构造。

## 4. 测试用例设计 + 自动化实现（`DataLineTest.testBlackBox`）
- 白盒：接口、直接父接口 `Line`、13 个声明方法。
- 黑盒：以 SourceDataLine 为具体 DataLine，断言 `getFormat().matches`、`getBufferSize==1024`、`available>=0`、`getFramePosition/getLongFramePosition/getMicrosecondPosition>=0`、`getLevel` 在 [-1,1]；执行 start/flush/stop/drain/close 状态机；无线路时跳过。
