# javax.sound.midi.Track — DESIGN

## 1. API 功能
`Track` 是 `Sequence` 内的一条按 tick 排序的 MIDI 事件流（`final` 类）。方法分组：
- 编辑：`add(MidiEvent)`（重复事件不加入，返回 false）、`remove(MidiEvent)`。
- 查询：`get(int)`（越界抛 `ArrayIndexOutOfBoundsException`）、`size()`、`ticks()`（最后事件 tick）。
- 构造为包私有，只能通过 `Sequence.createTrack()` 创建；新轨道初始含一个 End-of-Track meta 事件。

## 2. Java 用户如何使用
`Track t = sequence.createTrack()` → `t.add(new MidiEvent(new ShortMessage(...), tick))` → `t.size()` → `t.get(0)` → `t.ticks()` → `t.remove(event)`。
`add` 会保持时间顺序插入，重复加入返回 false；`add(null)`/`remove(null)` 返回 false。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/sound/midi/Track.java`：`add` 维护排序且去重，维护不可变 EOT 事件并同步其 tick；`remove` 允许删除 EOT；`ticks()` 返回末事件 tick。
- `test/jdk/javax/sound/midi/Track/TrackAddSameTick.java`：乱序 `add` 10 个事件后按序 `get(i)` 验证排序。
- `test/jdk/javax/sound/midi/Track/bug6416024.java`：`while (track.size() > 0) track.remove(track.get(0))` 清空后再 `add`。

## 4. 测试用例设计 + 自动化实现（`TrackTest.testBlackBox`）
- 新轨道 `size()==1`、`ticks()==0`；`add` 后 size/顺序/tick 往返。
- 乱序 `add` 早事件被插入首位；重复 `add` 返回 false 不重复加入。
- `remove` true/重复 false；`add(null)`/`remove(null)` false。
- `get(-1)`/`get(size())` 抛 `ArrayIndexOutOfBoundsException`。
- 清空（含 EOT）后 `add` 重新建立 EOT，`size()==2`、`ticks()` 正确。
