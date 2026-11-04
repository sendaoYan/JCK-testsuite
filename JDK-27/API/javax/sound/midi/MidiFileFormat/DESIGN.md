# javax.sound.midi.MidiFileFormat — DESIGN

## 1. API 功能
`MidiFileFormat` 封装 MIDI 文件格式信息（类型、时序划分类型与分辨率、字节长度、微秒长度、可选属性）。方法分组：
- 构造器：`MidiFileFormat(int type, float divisionType, int resolution, int bytes, long microseconds)`、`MidiFileFormat(..., Map<String,Object> properties)`。
- 查询：`getType()`、`getDivisionType()`、`getResolution()`、`getByteLength()`、`getMicrosecondLength()`、`properties()`（不可变 map，无属性时返回空 map）、`getProperty(String)`。
- 常量：`UNKNOWN_LENGTH = -1`。

## 2. Java 用户如何使用
用户通常从 `MidiSystem.getMidiFileFormat(File/InputStream/URL)` 获得实例，再读 `getType()/getResolution()/getMicrosecondLength()`；也可用 `new MidiFileFormat(...)` 自行构造（多用于 SPI 实现）。典型组合链：`new MidiFileFormat(type, Sequence.PPQ, res, bytes, micros)` → `getType()/getDivisionType()/getResolution()/getByteLength()/getMicrosecondLength()` → `properties()/getProperty(...)`。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/sound/midi/MidiFileFormat.java`：6 参构造器 `this.properties = new HashMap<>(properties)` 做防御拷贝；`properties()` 返回 `Collections.unmodifiableMap(clone)`。
- `src/java.desktop/share/classes/javax/sound/midi/spi/MidiFileReader.java`：`getMidiFileFormat` 的返回类型即 `MidiFileFormat`。
- `src/java.desktop/share/classes/com/sun/media/sound/StandardMidiFileReader.java`：解析标准 MIDI 文件后 `new MidiFileFormat(type, divisionType, resolution, byteLength, microsecondLength)` 构造返回。
- `test/jdk/javax/sound/midi/Sequence/GetMicrosecondLength.java`、`MidiSystem/DefaultProperties` 等覆盖 `MidiFileFormat` 的读取与属性。

## 4. 测试用例设计 + 自动化实现（`MidiFileFormatTest.testBlackBox`）
- 5 参构造器往返：type/divisionType/resolution/byteLength/microsecondLength 精确断言。
- 无属性：`properties()` 非空且为空、`getProperty("absent")==null`。
- `UNKNOWN_LENGTH`：构造后 `getByteLength()==-1`、`getMicrosecondLength()==-1`。
- 属性构造器：`getProperty` 往返、`properties()` 内容、`properties()` 不可修改（`put` 抛 `UnsupportedOperationException`）、构造时防御拷贝源 map。
