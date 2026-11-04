# javax.sound.sampled.spi.MixerProvider — DESIGN

## 1. API 功能
`MixerProvider` 是混音器提供服务的 SPI 抽象基类。方法：
- 抽象：`getMixerInfo()`、`getMixer(Mixer.Info)`。
- 具体：`isMixerSupported(Mixer.Info)`（默认遍历 `getMixerInfo` 判断）。
构造器 `protected`。

## 2. Java 用户如何使用
最终用户通过 `AudioSystem.getMixerInfo()` 与 `AudioSystem.getMixer(info)` 间接使用已注册 provider；自定义 provider 实现两个抽象方法并通过 SPI 注册。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码）
- `src/java.desktop/share/classes/javax/sound/sampled/AudioSystem.java:194-210`：`getMixer` 遍历 `getMixerProviders()` 逐个 `provider.getMixer(info)`，忽略 `IllegalArgumentException/NPE`，全失败则抛 `IllegalArgumentException`。
- 内置 `DirectAudioDeviceProvider`/`PortMixerProvider` 实现该 SPI。

## 4. 测试用例设计 + 自动化实现（`MixerProviderTest.testBlackBox`）
- 白盒：抽象类、protected 构造器、2 个抽象方法、1 个具体方法。
- 黑盒：匿名子类返回空数组时 `isMixerSupported` 为 false、`getMixerInfo().length==0`；真实 provider 经 `AudioSystem.getMixerInfo()` 非空、有 mixer 时 `getMixer(first)` 非空且 `getMixerInfo` 非空。
