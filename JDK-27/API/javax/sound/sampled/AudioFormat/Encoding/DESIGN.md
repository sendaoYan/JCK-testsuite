# javax.sound.sampled.AudioFormat.Encoding — DESIGN

## 1. API 功能
`AudioFormat.Encoding` 是 `AudioFormat` 的静态嵌套类，命名音频数据的编码方式。方法分组：
- 常量：`PCM_SIGNED`、`PCM_UNSIGNED`、`PCM_FLOAT`、`ULAW`、`ALAW`。
- 构造器：`Encoding(String name)`（公开，服务提供者可自定义编码）。
- 查询：`toString()`（返回 name）。
- 相等：`equals`/`hashCode` 按 name 比较。

## 2. Java 用户如何使用
用户引用内置常量 `AudioFormat.Encoding.PCM_SIGNED` 构造 `AudioFormat`，或 `new Encoding("MY_ENC")` 定义自定义编码。相等性按名字，因此两个名字相同的 Encoding 相等，便于 `AudioFormat.matches` 通过 `getEncoding().equals` 判断。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/sound/sampled/AudioFormat.java:529`：`PCM_SIGNED = new Encoding("PCM_SIGNED")` 等常量定义。
- `src/java.desktop/share/classes/javax/sound/sampled/AudioFormat.java:576`：`equals` 用 `Objects.equals(name, ((Encoding) obj).name)`。
- `test/jdk/javax/sound/sampled/AudioFormat/EncodingEquals.java`：`new AudioFormat.Encoding("PCM_SIGNED").equals(AudioFormat.Encoding.PCM_SIGNED)` 为 true（bug 4925483）。

## 4. 测试用例设计 + 自动化实现（`EncodingTest.testBlackBox`）
- 5 个常量非空且互异；`toString()` 为 `PCM_SIGNED/PCM_UNSIGNED/PCM_FLOAT/ULAW/ALAW`。
- `equals/hashCode` 按名字：同名相等、与 null/Object/其他编码不等、`hashCode` 相等。
- 自定义 `new Encoding("MY_ENC")` 名字往返。
