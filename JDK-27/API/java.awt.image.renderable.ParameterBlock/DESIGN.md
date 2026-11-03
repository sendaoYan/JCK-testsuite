# java.awt.image.renderable.ParameterBlock 设计案

## 1. API 功能分组
- **类型**：ParameterBlock implements Cloneable、Serializable。
- **源**：`addSource/getSource/getRenderedSource/getRenderableSource/getNumSources/getSources/setSource/setSources/removeSources`。
- **参数**：`add*/set*`（Object/byte/char/short/int/long/float/double）、`get*Parameter`、`getNumParameters/getParameters/setParameters/removeParameters/getParamClasses`。
- **克隆**：`clone()/shallowClone()`。

## 2. Java 用户如何使用
封装渲染操作的输入源与参数；`addSource(image)` 与 `add(参数)` 组装操作链，交给 `RenderedImageFactory.create(ParameterBlock, hints)`。

## 3. 开源代码实际用法调研
- `java.awt.image.renderable.ParameterBlock` 是渲染链的源/参数容器（`src/java.desktop/share/classes/java/awt/image/renderable/ParameterBlock.java`）。
- `test/jdk/java/awt/image/renderable/` 用例以 ParameterBlock 组装操作（本用例源/参数管理参考）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`ParameterBlockTest.testBlackBox()`）：
1. 源管理：addSource/getSource/getRenderedSource/getSources/removeSources。
2. 参数管理：add(int/double/String)/getObjectParameter/getIntParameter/getDoubleParameter/set/removeParameters。
3. clone（浅拷贝源 + 拷贝参数）、shallowClone、getParamClasses。
白盒（`testWhiteBox()`）：public、implements Cloneable/Serializable。
