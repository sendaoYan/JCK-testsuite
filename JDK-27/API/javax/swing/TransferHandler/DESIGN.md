# javax.swing.TransferHandler 设计案

## 1. API 功能
`TransferHandler` 处理组件间拖拽/复制数据传递。方法分组：
- 常量：`NONE/COPY/MOVE/COPY_OR_MOVE/LINK`。
- 静态动作：`getCutAction/getCopyAction/getPasteAction`。
- 拖拽图：`setDragImage/getDragImage/setDragImageOffset/getDragImageOffset`。
- 导入判定：`canImport/getSourceActions`。

## 2. Java 用户如何使用
用户为组件设置 TransferHandler 启用复制/粘贴：
```java
component.setTransferHandler(new TransferHandler("text"));
Action copy = TransferHandler.getCopyAction();
```
典型链路：`new TransferHandler("text")` → `getSourceActions/canImport`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/TransferHandler.java` 是 JDK 自身实现（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/TransferHandler.java`）。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验 `Serializable`。
- 行为（黑盒）：`testBlackBox()` 覆盖 5 个动作常量、三个静态动作、`getSourceActions` 默认 `NONE`、拖拽图 get/set、`canImport` 默认 false。
