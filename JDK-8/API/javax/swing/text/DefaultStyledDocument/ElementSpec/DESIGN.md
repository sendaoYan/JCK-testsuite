# javax.swing.text.DefaultStyledDocument.ElementSpec — DESIGN

## 1. API 功能
描述构建元素结构的一条指令（start tag / end tag / content）。方法分组：
- 常量：类型 `StartTagType/EndTagType/ContentType`；方向 `JoinPreviousDirection/JoinNextDirection/OriginateDirection/JoinFractureDirection`。
- 构造：`ElementSpec(AttributeSet, short)`、`ElementSpec(AttributeSet, short, int)`、`ElementSpec(AttributeSet, short, char[], int, int)`。
- 读写：`setType/getType`、`setDirection/getDirection`、`getAttributes`、`getArray`、`getOffset`、`getLength`。
- 调试：`toString()`。

## 2. Java 用户如何使用
`new ElementSpec(attr, ElementSpec.ContentType, text.toCharArray(), 0, len)` 描述一段内容；`setDirection(ElementSpec.JoinNextDirection)` 控制与相邻元素的合并；`getType/getDirection/getLength` 读取指令，供批量构建文档元素结构时使用。

## 3. 开源代码实际用法调研（来源：jdk8u 源码 + jdk/test）
- `jdk/src/share/classes/javax/swing/text/DefaultStyledDocument.java:707`：`new ElementSpec(attr, ElementSpec.ContentType, breakOffset - lastOffset)`。
- `jdk/src/share/classes/javax/swing/text/DefaultStyledDocument.java:711`：`new ElementSpec(pattr, ElementSpec.StartTagType)`。
- `jdk/src/share/classes/javax/swing/text/DefaultStyledDocument.java:1261`：`ElementSpec(AttributeSet a, short type, int len)` 用于解析阶段描述长度。

## 4. 测试用例设计 + 自动化实现
`testConstructorsAndViews/testMutators` + 常量/形状（42 断言）：
- 常量精确值：StartTagType=1/EndTagType=2/ContentType=3/JoinPreviousDirection=4/JoinNextDirection=5/OriginateDirection=6/JoinFractureDirection=7。
- 构造器矩阵：两参（无数组、默认 Originate、属性 identity）、三参（显式长度仍无数组）、五参数组窗（offset/length 精确）。
- 版本差异锁定（JDK-8 侧）：数组构造器按引用保留调用者数组——getArray()==data 且为活视图（修改源数组后 getArray()[i] 随之变化）；JDK-27 改为 Arrays.copyOf 防御性拷贝。
- 可变性：setType/setDirection 往返；null 属性容忍。
