# java.awt.Dialog.ModalityType 黑盒行为测试设计

## ① API 功能分组
- 枚举常量：`MODELESS`、`DOCUMENT_MODAL`、`APPLICATION_MODAL`、`TOOLKIT_MODAL`。
- 继承方法：`values()`/`valueOf(String)`/`ordinal()`/`name()`/`compareTo`。

## ② Java 用户用法
```java
Dialog d = new Dialog(frame, "title", Dialog.ModalityType.APPLICATION_MODAL);
d.getModalityType();                 // -> ModalityType
d.setModalityType(ModalityType.MODELESS);
ModalityType[] all = ModalityType.values();
ModalityType t = ModalityType.valueOf("DOCUMENT_MODAL");
```
组合链路：构造 Dialog 指定枚举 → get/set 模态类型 → 遍历 values 断言序数与名称。

## ③ 开源实际用法调研
JDK 自用（来源 `~/git/jdk25u-dev`）：
- `Dialog.setModalityType` 校验入参非 null 后存入 `modalityType` 字段，并同步 `modal` 布尔（`Dialog.java`）。
- `Dialog.isModal()` 返回 `modalityType != MODELESS`（`Dialog.java`）。
典型外部用法（Netty/Swing 客户端常见）：主窗口用 `APPLICATION_MODAL` 阻止其他窗口交互，`MODELESS` 表示无阻塞。

## ④ 测试用例设计 + 自动化实现
- `values()` 长度与顺序（固定序数断言）→ `testBlackBox`。
- `ordinal()` 固定序数（0/3）→ `testBlackBox`。
- `valueOf(name)` 往返 + 字面量 + `name()` → `testBlackBox`。
- `compareTo` 自反 → `testBlackBox`。
