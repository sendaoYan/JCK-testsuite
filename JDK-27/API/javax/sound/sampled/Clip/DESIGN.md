# javax.sound.sampled.Clip — DESIGN

## 1. API 功能
`Clip` 是 `DataLine` 的子接口，代表可预加载到内存的短音频剪辑。方法：
- 打开：`open(AudioFormat, byte[], int, int)`、`open(AudioInputStream)`。
- 长度：`getFrameLength()`、`getMicrosecondLength()`。
- 定位：`setFramePosition(int)`、`setMicrosecondPosition(long)`。
- 循环：`setLoopPoints(int, int)`、`loop(int)`，常量 `LOOP_CONTINUOUSLY = -1`。
- 继承自 `DataLine`/`Line` 的 `start/stop/close/isOpen/getFramePosition` 等。

## 2. Java 用户如何使用
`Clip c = AudioSystem.getClip(); c.open(fmt, data, 0, len); c.setLoopPoints(0, -1); c.start(); c.stop(); c.close();`。查询长度/定位链：`getFrameLength()` → `setFramePosition(100)` → `((DataLine)c).getFramePosition()`。

## 3. 开源代码实际用法调研（来源：jdk25u-dev test/jdk）
- `test/jdk/javax/sound/sampled/Clip/ClipSetPos.java`：`clip.setFramePosition(...)` / `setMicrosecondPosition(...)` 后校验位置。
- `test/jdk/javax/sound/sampled/Clip/ClipIsRunningAfterStop.java`：`clip.start()`/`stop()` 后检查 `isRunning()`。
- `test/jdk/javax/sound/sampled/Clip/OpenNonIntegralNumberOfSampleframes.java`：`clip.open(ais)` 非整数帧长行为。

## 4. 测试用例设计 + 自动化实现（`ClipTest.testBlackBox`）
- 白盒：接口、直接父接口 `DataLine`、`LOOP_CONTINUOUSLY` 字段与 7 个声明方法。
- 黑盒：`LOOP_CONTINUOUSLY == -1`；获取 Clip → open → isOpen → `getFrameLength==800`、`getMicrosecondLength==100000` → `setFramePosition(100)`/`setMicrosecondPosition(0)` 往返 → `setLoopPoints(0,-1)` → start/stop/close；无 Clip 线路时跳过并保持通过。
