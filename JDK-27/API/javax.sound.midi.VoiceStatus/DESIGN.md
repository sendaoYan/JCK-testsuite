# javax.sound.midi.VoiceStatus — DESIGN

## 1. API 功能
`VoiceStatus` 描述合成器单个发声单元（voice）的状态，纯数据类。成员：
- 构造：`VoiceStatus()`（public 无参）。
- 公有字段（可读写）：`boolean active`、`int channel`、`int bank`、`int program`、`int note`、`int volume`。

## 2. Java 用户如何使用
通过 `Synthesizer.getVoiceStatus()` 获得数组，读取字段：
`VoiceStatus[] vs = synth.getVoiceStatus()` → `vs[0].active` → `vs[0].note` → `vs[0].channel`。
字段为 public，直接读写；`active` 表示该 voice 当前是否在发声。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/sound/midi/VoiceStatus.java`：定义六个 public 字段，无构造逻辑，默认值均为 Java 默认（`false`/`0`）。
- `src/java.desktop/share/classes/javax/sound/midi/Synthesizer.java`：`getVoiceStatus()` 返回 `VoiceStatus[]`，是 `Synthesizer` 的组合类。

## 4. 测试用例设计 + 自动化实现（`VoiceStatusTest.testBlackBox`）
- 新建实例断言六个字段默认值（active=false，其余 0）。
- 赋值后往返断言六个字段；再新建实例确认字段默认值隔离。
- 白盒补齐：非接口非抽象、public 无参构造、六个 public 字段齐全。
