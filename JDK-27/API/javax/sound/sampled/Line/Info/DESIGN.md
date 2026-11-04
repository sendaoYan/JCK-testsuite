# javax.sound.sampled.Line.Info — DESIGN

## 1. API 功能
`Line.Info` 描述线路类型。方法：
- 构造：`Line.Info(Class<?>)`。
- 查询：`getLineClass()`。
- 匹配/表示：`matches(Line.Info)`、`toString()`。

## 2. Java 用户如何使用
`new Line.Info(SourceDataLine.class)` 后调用 `getLineClass()`；传给 `AudioSystem.getSourceLineInfo(...)`/`Mixer.getSourceLineInfo(...)` 做线路匹配过滤；`matches` 用于判断一个 info 是否满足另一个。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/sound/sampled/Line.java:269-307`：`matches` 先判 `this.getClass().isInstance(info)` 再判 `getLineClass().isAssignableFrom(info.getLineClass())`。
- `test/jdk/javax/sound/sampled/LineEvent/LineInfoNPE.java`：Line.Info 相关空指针边界。

## 4. 测试用例设计 + 自动化实现（`InfoTest.testBlackBox`）
- 白盒：公开类、继承 Object、`(Class)` 构造器与 getLineClass/matches/toString。
- 黑盒：`getLineClass` 往返；`matches` 自匹配、null 返回 false、不同/相同 lineClass；`toString` 非空且包含线路类名。
