# javax.sound.sampled.AudioFileFormat.Type — DESIGN

## 1. API 功能
`AudioFileFormat.Type` 是 `AudioFileFormat` 的静态嵌套类，代表一种标准音频文件类型。方法分组：
- 常量：`WAVE`、`AU`、`AIFF`、`AIFC`、`SND`。
- 构造器：`Type(String name, String extension)`（扩展名不含前导点）。
- 查询：`getExtension()`、`toString()`（返回 name）。
- 相等：`equals`/`hashCode` 按 name 比较（非身份比较）。

## 2. Java 用户如何使用
用户引用内置常量 `AudioFileFormat.Type.WAVE` 传给 `AudioFileFormat` 构造器，或通过 `getExtension()` 得到文件扩展名（`"wav"`）。相等性基于名字，因此 `new Type("WAVE", "x").equals(Type.WAVE)` 为 true。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/sound/sampled/AudioFileFormat.java:297`：`WAVE = new Type("WAVE", "wav")`、`AIFC = new Type("AIFF-C", "aifc")` 等常量定义。
- `src/java.desktop/share/classes/javax/sound/sampled/AudioFileFormat.java:350`：`equals` 用 `Objects.equals(name, ((Type) obj).name)` 实现名字比较。
- `test/jdk/javax/sound/sampled/FileTypeExtension/FileTypeExtensionTest.java`：断言各 `Type.getExtension()` 不以 `.` 开头。

## 4. 测试用例设计 + 自动化实现（`TypeTest.testBlackBox`）
- 5 个常量非空且互异；`toString()` 分别为 `WAVE/AU/AIFF/AIFF-C/SND`。
- `getExtension()` 分别为 `wav/au/aif/aifc/snd`，且不含前导 `.`。
- `equals/hashCode` 按名字：`new Type("WAVE","xyz").equals(Type.WAVE)` 为 true、与 null/Object/其他类型不等、`hashCode` 相等、自定义扩展名保留。
