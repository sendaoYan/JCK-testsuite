# javax.sound.sampled.AudioFormat — DESIGN

## 1. API 功能
`AudioFormat` 指定音频流中数据的排列方式（编码、采样率、采样位数、声道数、帧大小、帧率、字节序，以及可选属性）。方法分组：
- 构造器：7 参 `AudioFormat(Encoding, sampleRate, sampleSizeInBits, channels, frameSize, frameRate, bigEndian)`、8 参（加 `Map` 属性，被复制）、5 参 PCM 便捷构造器（自动推导 `Encoding.PCM_SIGNED/UNSIGNED`、`frameSize`、`frameRate`）。
- 查询：`getEncoding()`、`getSampleRate()`、`getSampleSizeInBits()`、`getChannels()`、`getFrameSize()`、`getFrameRate()`、`isBigEndian()`。
- 属性：`properties()`、`getProperty(String)`。
- 匹配：`matches(AudioFormat)`（编码相等 + 各字段一致或对端为 `NOT_SPECIFIED`；字节序仅在采样位数 > 8 时参与比较；属性忽略）。
- 工具：`toString()`。

## 2. Java 用户如何使用
典型组合链：`new AudioFormat(44100f, 16, 2, true, false)`（PCM 便捷构造，自动得 frameSize=4）→ `getEncoding()/getSampleRate()/getFrameSize()` → `format.matches(anotherFormat)` 做能力匹配；带属性用 8 参构造器后 `getProperty("bitrate")`。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/sound/sampled/AudioFormat.java:246`：5 参构造器用 `(sampleSizeInBits + 7) / 8 * channels` 计算 frameSize。
- `src/java.desktop/share/classes/javax/sound/sampled/AudioFormat.java:431`：`matches` 中 `format.getChannels() == AudioSystem.NOT_SPECIFIED || == getChannels()` 的宽松匹配。
- `test/jdk/javax/sound/sampled/AudioFormat/Matches_NOT_SPECIFIED.java`：构造全 `NOT_SPECIFIED` 格式验证 `matches` 通配行为（bug 4937708）。
- `test/jdk/javax/sound/sampled/AudioFormat/EncodingEquals.java`：验证自定义 Encoding 与内置常量按名字相等（bug 4925483）。

## 4. 测试用例设计 + 自动化实现（`AudioFormatTest.testBlackBox`）
- 7 参构造器各字段精确往返；空属性默认。
- 5 参 PCM 便捷构造器：编码/frameSize/frameRate 推导；有符号与无符号两分支。
- 8 参构造器属性往返。
- `matches`：相同格式匹配、属性被忽略、`NOT_SPECIFIED` 通配匹配、采样率/编码/字节序不同不匹配。
- `toString()` 含编码名。
