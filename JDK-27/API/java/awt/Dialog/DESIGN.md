# java.awt.Dialog 黑盒行为测试设计

## ① API 功能分组
- 构造：`Dialog(Frame[,title][,modal])`、`Dialog(Dialog[...])`、`Dialog(Window, String, ModalityType[,gc])`。
- 模态：`isModal/setModal/getModalityType/setModalityType`、常量 `DEFAULT_MODALITY_TYPE`。
- 标题/外观：`setTitle/getTitle`、`setResizable/isResizable`、`setUndecorated/isUndecorated`。
- 生命周期：`setVisible/dispose`。

## ② Java 用户用法
```java
Dialog d = new Dialog(frame, "t", Dialog.ModalityType.APPLICATION_MODAL);
d.setResizable(false);
d.setVisible(true);   // modal 会阻塞，实际由用户关闭后返回
d.dispose();
```
组合链路：构造（不同模态形态）→ 标志位查询 → modeless setVisible 路径 → dispose。模态对话框只断标志位，不断言可见路径。

## ③ 开源实际用法调研
JDK 自用（来源 `~/git/jdk25u-dev`）：
- `Dialog.setModal(boolean)` 依据布尔把 `modalityType` 设为 `APPLICATION_MODAL` 或 `MODELESS`（`Dialog.java`）。
- `Dialog.setModalityType` 校验后同步 `modal` 布尔（`Dialog.java`）。
- `Dialog.DEFAULT_MODALITY_TYPE` 定义为 `MODELESS`（`Dialog.java`）。

## ④ 测试用例设计 + 自动化实现
- 默认 modeless/标题/可缩放/装饰 → `testBlackBox`。
- modeless `setVisible(true)/false` 显示隐藏路径 + dispose → `testBlackBox`。
- 布尔构造 modal→APPLICATION_MODAL、ModalityType 构造 DOCUMENT_MODAL → `testBlackBox`。
- `setModal/setModalityType` 标志位往返（不 setVisible 模态框）→ `testBlackBox`。
