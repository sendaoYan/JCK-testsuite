# java.awt.Dialog 黑盒行为测试设计

## ① API 功能分组
- 构造：14 个 public 构造器 —— Frame 族 6 个、Dialog 族 4 个、Window 族 4 个（gc 可 null；owner 必须为 Frame/Dialog/null）。
- 模态：`isModal/setModal/getModalityType/setModalityType`、常量 `DEFAULT_MODALITY_TYPE`；`ModalityType` 4 常量（MODELESS=0/DOCUMENT_MODAL=1/APPLICATION_MODAL=2/TOOLKIT_MODAL=3）；`ModalExclusionType` 3 常量。
- 标题/外观：`setTitle/getTitle`（默认 ""，`setTitle(null)` 保存 null——与 Frame 不同）、`setResizable/isResizable`、`setUndecorated/isUndecorated`。
- 生命周期：`setVisible/show/hide/dispose/toBack`。

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
- `Dialog.setModalityType` 校验后同步 `modal` 布尔；null 重置为该 Dialog 默认 MODELESS（`Dialog.java`）。
- `Dialog` 构造器校验 owner：非 Frame/Dialog/null 抛 `IllegalArgumentException("Wrong parent window")`（`Dialog.java` L652 附近）。

## ④ 测试用例设计 + 自动化实现
- 默认 modeless/标题 ""/可缩放/装饰/BorderLayout → `testBlackBox`。
- modeless `setVisible(true)/false` 显示隐藏路径 + dispose → `testBlackBox`。
- 布尔构造 modal→APPLICATION_MODAL、ModalityType 构造 DOCUMENT_MODAL → `testBlackBox`。
- `setModal/setModalityType` 往返（true→APPLICATION_MODAL、false→MODELESS、null→MODELESS）→ `testBlackBox`。
- 完整构造矩阵 14 形态：Frame 族/Dialog 族/Window 族（Frame 作 Window 型 owner 合法）、null owner 三形态 getOwner()==null、gc=null、构造器 null ModalityType→MODELESS → `testConstructorMatrix`。
- 模态矩阵：4 类型 × isModal 映射 {false,true,true,true}；`setModalityType(null)`→MODELESS → `testModalityMatrix`。
- 枚举契约：ModalityType 4 常量/序数 0-3/valueOf；ModalExclusionType 3 常量/序数 0-2 → `testModalityMatrix`。
- toString 含 title+MODELESS；invisible 三态 false；displayable 下 setUndecorated 抛 ICSE；未显示 Dialog 自由改模态 → `testMiscContracts`。

## ⑤ 反例与精确异常
- `new Dialog(普通 Window[,title[,modalityType]])` → IllegalArgumentException（"Wrong parent window"；Frame/Dialog/null 才是合法 owner）。
- `Dialog.ModalityType.valueOf("BOGUS_MODAL")` → IllegalArgumentException。
- displayable 状态下 `setUndecorated(true)` → IllegalComponentStateException。

版本差异：JDK-8 与 JDK-27 语义一致（双侧实测相同；`setTitle(null)` 存 null 与 Frame 的 "" 归一化对照为类间差异而非版本差异）。

自动化实现位于同目录 `DialogTest.java`：`check/checkThrows` 计数断言，`main` 打印 `PASS (N assertions)`；@key headful（DISPLAY=:77），通过 91 断言。
