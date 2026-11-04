# javax.sound.sampled.DataLine.Info — DESIGN

## 1. API 功能
`DataLine.Info` 是 `Line.Info` 的子类，描述一条数字音频线路的格式与缓冲区能力。方法：
- 构造：`Info(Class, AudioFormat[], int, int)`、`Info(Class, AudioFormat, int)`、`Info(Class, AudioFormat)`。
- 查询：`getFormats()`、`isFormatSupported(AudioFormat)`、`getMinBufferSize()`、`getMaxBufferSize()`。
- 继承/覆盖：`getLineClass()`、`matches(Line.Info)`、`toString()`。

## 2. Java 用户如何使用
`DataLine.Info info = new DataLine.Info(SourceDataLine.class, fmt); info.isFormatSupported(fmt); info.getMinBufferSize();`。也用于查询系统：`AudioSystem.getSourceLineInfo(new DataLine.Info(SourceDataLine.class, fmt))`。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/sound/sampled/DataLine.java:319-360`：`Info` 三构造器把 `formats` 做防御性 `Arrays.copyOf`，单格式构造器把 min/max bufferSize 都设为 `AudioSystem.NOT_SPECIFIED`。
- `test/jdk/javax/sound/sampled/DataLine/DataLineInfoNegBufferSize.java`：负 bufferSize 场景。
- `test/jdk/javax/sound/sampled/Lines/GetLine.java`：`new DataLine.Info(SourceDataLine.class, null)` 查询系统线路。

## 4. 测试用例设计 + 自动化实现（`InfoTest.testBlackBox`）
- 白盒：公开类、继承 `Line$Info`、三构造器与六方法。
- 黑盒：三种构造器的 min/max bufferSize 语义（含 `NOT_SPECIFIED`、null 格式 → 空格式）；`isFormatSupported` 真假；`matches` 对 Line.Info/不同 lineClass 的区分；`getFormats` 返回防御性副本；`toString` 非空。
