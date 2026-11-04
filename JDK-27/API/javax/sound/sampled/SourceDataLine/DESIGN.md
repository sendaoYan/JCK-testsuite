# javax.sound.sampled.SourceDataLine — DESIGN

## 1. API 功能
`SourceDataLine` 是 `DataLine` 的子接口，用于向混音器写入（播放）音频。方法：
- 打开：`open(AudioFormat, int)`、`open(AudioFormat)`。
- 写入：`write(byte[], int, int)`。
- 继承 `DataLine`/`Line` 的 `start/stop/drain/flush/close/getBufferSize` 等。

## 2. Java 用户如何使用
`SourceDataLine sdl = AudioSystem.getSourceDataLine(fmt); sdl.open(fmt, bufferSize); sdl.start(); sdl.write(data, 0, len); sdl.drain(); sdl.stop(); sdl.close();`。

## 3. 开源代码实际用法调研（来源：jdk25u-dev test/jdk）
- `test/jdk/javax/sound/sampled/Lines/SDLwrite.java`：覆盖未打开/未启动时 `SourceDataLine.write()` 的行为。
- `test/jdk/javax/sound/sampled/Lines/SourceDataLineDefaultBufferSizeCrash.java`：`open(fmt)` 默认缓冲区大小。
- `test/jdk/javax/sound/sampled/Lines/BufferSizeCheck.java`：`getBufferSize()` 与请求值一致。

## 4. 测试用例设计 + 自动化实现（`SourceDataLineTest.testBlackBox`）
- 白盒：接口、直接父接口 `DataLine`、open/write 方法。
- 黑盒：获取线路 → open(format, 1024) → isOpen → getBufferSize==1024 → getFormat 匹配 → available>=0 → start → write 返回请求字节数 → flush → stop → close；无线路时跳过。
