# javax.sound.sampled.AudioFileFormat — DESIGN

## 1. API 功能
`AudioFileFormat` 描述一个音频文件：文件类型、字节长度、音频数据格式、采样帧长度，以及一组可选属性。方法分组：
- 构造器：`AudioFileFormat(Type, AudioFormat, int frameLength)`（字节长度取 `AudioSystem.NOT_SPECIFIED`）、`AudioFileFormat(Type, AudioFormat, int, Map)`（带属性，属性被复制）、受保护的 `AudioFileFormat(Type, int byteLength, AudioFormat, int frameLength)`。
- 查询：`getType()`、`getByteLength()`、`getFormat()`、`getFrameLength()`。
- 属性：`properties()`（不可修改映射副本）、`getProperty(String)`。
- 工具：`toString()`（含类型与扩展名）。

## 2. Java 用户如何使用
用户通过 `new AudioFileFormat(Type.WAVE, format, frameLength)` 描述一个期望的文件格式，随后查询：`getType()` → `getFormat()` → `getFrameLength()` → `getByteLength()` → `getProperty("author")`。服务提供者读取文件时用受保护的 4 参构造器给出精确字节长度。属性链：`new AudioFileFormat(Type.AU, format, 50, props)` → `getProperty("duration")` → `properties().size()`。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/sound/sampled/AudioFileFormat.java:154`：3 参公共构造器 `this(type, AudioSystem.NOT_SPECIFIED, format, frameLength)`，字节长度缺省为 -1。
- `src/java.desktop/share/classes/javax/sound/sampled/AudioFileFormat.java:269`：`toString()` 输出 `"WAVE (.wav) file, data format: ..."`。
- `test/jdk/javax/sound/sampled/FileTypeExtension/FileTypeExtensionTest.java`：遍历 `Type.AIFC/AIFF/AU/SND/WAVE` 断言 `getExtension()` 不含前导 `.`（bug 4300529）。

## 4. 测试用例设计 + 自动化实现（`AudioFileFormatTest.testBlackBox`）
- 3 参构造器往返：`getType/getFormat/getFrameLength`，`getByteLength == NOT_SPECIFIED`，空 `properties()`。
- 4 参构造器带属性：`getProperty("author"/"duration")`、`properties().size()`。
- 受保护构造器经子类：`getByteLength` 精确往返。
- `toString()` 含类型名与 `.wav/.au` 扩展名。

## 6. 版本差异（JDK-8 vs JDK-27，双侧各自断言）
- `Type.AIFF` 扩展名为 `aif`（非 aiff）、`AIFC` 为 `aifc`、`toString()` 为 `AIFF-C`——两版一致，常量语义锁定。
- 可写文件类型仅 3 个（WAVE/AU/AIFF）；AIFC/SND 为只读常量。
