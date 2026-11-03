# javax.sound.sampled.spi.FormatConversionProvider — DESIGN

## 1. API 功能
`FormatConversionProvider` 是音频格式转换（编解码）服务的 SPI 抽象基类。方法：
- 抽象：`getSourceEncodings()`、`getTargetEncodings()`、`getTargetEncodings(AudioFormat)`、`getTargetFormats(Encoding, AudioFormat)`、`getAudioInputStream(Encoding, AudioInputStream)`、`getAudioInputStream(AudioFormat, AudioInputStream)`。
- 具体：`isSourceEncodingSupported(Encoding)`、`isTargetEncodingSupported(Encoding)`、`isConversionSupported(Encoding, AudioFormat)`、`isConversionSupported(AudioFormat, AudioFormat)`。
构造器 `protected`。

## 2. Java 用户如何使用
通过 `AudioSystem.getTargetEncodings(...)`/`isConversionSupported(...)`/`getTargetFormats(...)`/`getAudioInputStream(encoding, stream)` 间接使用已注册转换器。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码）
- `src/java.desktop/share/classes/javax/sound/sampled/AudioSystem.java:656-864`：聚合 `getFormatConversionProviders()` 实现转换查询与 `getAudioInputStream` 转换。
- 内置 `PCMtoPCMCodec`、`ULAWtoPCMCodec` 等实现该 SPI。

## 4. 测试用例设计 + 自动化实现（`FormatConversionProviderTest.testBlackBox`）
- 白盒：抽象类、protected 构造器、6 个抽象方法、4 个具体方法。
- 黑盒：匿名子类返回空数组时四个 `isXxxSupported/isConversionSupported` 均为 false；真实转换器经 `AudioSystem`：`getTargetEncodings`/`getTargetFormats` 非空、`isConversionSupported(Encoding,AudioFormat)` 与 `getTargetFormats` 长度一致性。
