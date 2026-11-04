# java.awt.Dialog 黑盒行为测试设计（JDK-8）

## 1. API 功能
对话框（extends Window）：14 个 public 构造器（Frame 族 6/Dialog 族 4/Window 族 4；owner 必须是
Frame/Dialog/null）、模态 isModal/setModal/getModalityType/setModalityType、DEFAULT_MODALITY_TYPE、
ModalityType 4 常量（0-3）、ModalExclusionType 3 常量（0-2）、setTitle/getTitle、setResizable、
setUndecorated、setVisible/show/hide/toBack。

## 2. Java 用户如何使用
`Dialog d = new Dialog(frame, "t", true); d.setVisible(true); // 阻塞`
modeless 路径可显示/隐藏断言；模态只断标志位。构造时 owner 用 Frame/Dialog；普通 Window 会被拒。

## 3. 开源实际用法调研（本地源码，web 未使用）
`~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/Dialog.java`
（setModal(bool) 映射 APPLICATION_MODAL/MODELESS；setModalityType(null) 回默认；构造器对非 Frame/Dialog/null
owner 抛 IllegalArgumentException("Wrong parent window")）。

## 4. 测试用例设计 + 易错细节
- 结构反射（保留原断言全集）→ `testStructure`。
- 默认 modeless/标题/可缩放/BorderLayout + modeless 显示隐藏 + 布尔/类型构造模态映射 +
  setModal/setModalityType 往返（true→APPLICATION_MODAL、false→MODELESS、null→MODELESS）→ `testBlackBox`。
- 完整构造矩阵 14 形态：三族、null owner 三形态 getOwner()==null、gc=null、ctor null ModalityType→MODELESS、
  **默认 title "" 且 setTitle(null) 保存 null（与 Frame 归一化 "" 对照，类间差异易错）**
  → `testConstructorMatrix`。
- 模态矩阵 4 类型 × isModal {false,true,true,true} + 枚举序数/valueOf → `testModalityMatrix`。
- toString 含 title+MODELESS、displayable 下 setUndecorated 抛 ICSE、未显示自由改模态 → `testMiscContracts`。
- 反例与精确异常：`new Dialog(普通 Window[,...])`→IllegalArgumentException；
  `ModalityType.valueOf("BOGUS_MODAL")`→IllegalArgumentException；
  displayable `setUndecorated(true)`→IllegalComponentStateException。
- JDK-8 侧 97 断言通过（DISPLAY=:77，@key headful）。
