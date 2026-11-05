# javax.sound.sampled.Port — DESIGN

## 1. API 功能
`Port` 是 `Line` 的子接口，代表混音器上的音频端口（麦克风、扬声器、线路输入/输出等）。本身不声明任何方法，全部能力来自 `Line`；其类型由 `Port.Info`（MICROPHONE/SPEAKER/LINE_IN/LINE_OUT/HEADPHONE/COMPACT_DISC）描述。

## 2. Java 用户如何使用
通过 `AudioSystem.getSourceLineInfo(new Line.Info(Port.class))` 或 mixer 的对应方法查询端口线路，`AudioSystem.getLine(portInfo)` 获取后 `open()/close()`；用 `Port.Info` 常量区分端口角色与方向（`isSource()`）。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码）
- `src/java.desktop/share/classes/javax/sound/sampled/Port.java`：`public interface Port extends Line`，无声明方法。
- `src/java.desktop/share/classes/javax/sound/sampled/Port.java` 内嵌 `Port.Info` 定义六个端口常量。

## 4. 测试用例设计 + 自动化实现（`PortTest.testBlackBox`）
- 白盒：接口、直接父接口 `Line`、无自身声明方法。
- 黑盒：通过 `AudioSystem.getSourceLineInfo/getTargetLineInfo(new Line.Info(Port.class))` 查询端口线路并断言非空；`isLineSupported(Port)` 与端口线路存在性一致；存在端口线路时 open/close。
