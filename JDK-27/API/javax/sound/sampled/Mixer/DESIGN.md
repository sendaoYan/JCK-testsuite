# javax.sound.sampled.Mixer — DESIGN

## 1. API 功能
`Mixer` 是 `Line` 的子接口，代表音频混音器（一组输入/输出线路的集合）。方法：
- 信息：`getMixerInfo()`。
- 线路信息：`getSourceLineInfo()`、`getTargetLineInfo()` 及带 `Line.Info` 过滤的重载。
- 支持查询：`isLineSupported(Line.Info)`、`getMaxLines(Line.Info)`。
- 获取线路：`getLine(Line.Info)`、`getSourceLines()`、`getTargetLines()`。
- 同步：`synchronize(Line[], boolean)`、`unsynchronize(Line[])`、`isSynchronizationSupported(Line[], boolean)`。

## 2. Java 用户如何使用
`Mixer.Info[] infos = AudioSystem.getMixerInfo(); Mixer m = AudioSystem.getMixer(infos[0]); m.getSourceLineInfo(); m.isLineSupported(new DataLine.Info(SourceDataLine.class, fmt)); m.getLine(info); m.getMaxLines(...)`。

## 3. 开源代码实际用法调研（来源：jdk25u-dev test/jdk）
- `test/jdk/javax/sound/sampled/Mixers/UnexpectedIAE.java`：`mixer.getTargetLineInfo()` 后逐个 `mixer.getLine(...)`。
- `test/jdk/javax/sound/sampled/Mixers/BogusMixers.java`：非法 Mixer.Info 的 `getMixer` 行为。
- `test/jdk/javax/sound/sampled/Mixers/PhantomMixers.java`：`getMixerInfo` 枚举一致性。

## 4. 测试用例设计 + 自动化实现（`MixerTest.testBlackBox`）
- 白盒：接口、直接父接口 `Line`、11 个声明方法。
- 黑盒：枚举 mixer → `getMixerInfo` 四字段非空 → 源/目标线路信息非空 → `isLineSupported` 与 `getSourceLineInfo(filter)` 一致性 → `getMaxLines`/`getSourceLines`/`getTargetLines` 非空 → 有线路时 synchronize/unsynchronize；无 mixer 时仅枚举。
