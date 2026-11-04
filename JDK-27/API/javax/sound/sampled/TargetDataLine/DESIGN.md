# javax.sound.sampled.TargetDataLine — DESIGN

## 1. API 功能
`TargetDataLine` 是 `DataLine` 的子接口，用于从混音器读取（录音）音频。方法：
- 打开：`open(AudioFormat, int)`、`open(AudioFormat)`。
- 读取：`read(byte[], int, int)`。
- 继承 `DataLine`/`Line` 的 `start/stop/drain/flush/close/getBufferSize` 等。

## 2. Java 用户如何使用
`TargetDataLine tdl = AudioSystem.getTargetDataLine(fmt); tdl.open(fmt, bufferSize); tdl.start(); int n = tdl.read(buf, 0, len); tdl.stop(); tdl.close();`。

## 3. 开源代码实际用法调研（来源：jdk25u-dev test/jdk）
- `test/jdk/javax/sound/sampled/Lines/GetLine.java`：通过 `getTargetLineInfo` 查询目标线路。
- `test/jdk/javax/sound/sampled/Mixers/UnexpectedIAE.java`：遍历 `mixer.getTargetLineInfo()` 并 `getLine`。

## 4. 测试用例设计 + 自动化实现（`TargetDataLineTest.testBlackBox`）
- 白盒：接口、直接父接口 `DataLine`、open/read 方法。
- 黑盒：获取线路 → open(format, 1024) → isOpen → getBufferSize==1024 → getFormat 匹配 → start → read 返回非负 → stop → close；无线路时跳过。
