# javax.sound.sampled.Port.Info — DESIGN

## 1. API 功能
`Port.Info` 是 `Line.Info` 的子类，描述一个端口。方法：
- 常量：`MICROPHONE`、`LINE_IN`、`COMPACT_DISC`（源），`SPEAKER`、`HEADPHONE`、`LINE_OUT`（目标）。
- 构造：`Port.Info(Class<?>, String, boolean)`。
- 查询：`getName()`、`isSource()`。
- 覆盖：`matches(Line.Info)`、`equals(Object)`、`hashCode()`、`toString()`（final）；继承 `getLineClass()`。

## 2. Java 用户如何使用
`Port.Info.MICROPHONE.isSource()`、`Port.Info.SPEAKER.getName()`；自定义端口 `new Port.Info(Port.class, "Custom", true)`；用于 `Mixer.getSourceLineInfo(...)`/`getTargetLineInfo(...)` 过滤。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码）
- `src/java.desktop/share/classes/javax/sound/sampled/Port.java` 内嵌 `Port.Info`：静态常量通过 `new Info(Port.class, "MICROPHONE", true)` 等初始化，`matches`/`equals` 比较 name 与 source 标志。

## 4. 测试用例设计 + 自动化实现（`InfoTest.testBlackBox`）
- 白盒：公开类、继承 `Line$Info`、六个常量、`(Class,String,boolean)` 构造器与方法。
- 黑盒：六常量非空及 isSource 方向；`getName`/`getLineClass==Port.class`；自定义端口往返；`equals`/`hashCode`/`matches`/`toString` 行为。
