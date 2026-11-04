# java.awt.FileDialog 测试设计（JDK-8）

## 1. API 功能
文件选择对话框（extends Dialog，**构造即 APPLICATION_MODAL**——全部构造器 `super(parent, title, true)`）：
LOAD=0/SAVE=1、6 构造器（Frame 族 3 + Dialog 族 3）、getMode/setMode、getDirectory/setDirectory、
getFile/setFile/getFiles、setMultipleMode/isMultipleMode、getFilenameFilter/setFilenameFilter。

## 2. Java 用户如何使用
`FileDialog fd = new FileDialog(frame, "Open", FileDialog.LOAD); fd.setDirectory("/tmp");`
显示后 getFile/getDirectory 读回选择；setFilenameFilter 在 X11 上原生 peer 忽略但 getter 返回最近值。

## 3. 开源代码实际用法调研（本地源码，web 未使用）
`~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/FileDialog.java`
（构造器/setMode 校验 mode 非 LOAD/SAVE 抛 IAE；super(...,true) 固定 modal；javadoc 注明 X11 忽略 filter）。

## 4. 测试用例设计 + 易错细节
- 结构反射（保留原断言全集 + LOAD/SAVE 常量存在）→ `testStructure`。
- 常量精确值、6 构造器矩阵（默认 LOAD/owner/title/SAVE 透传）、null parent 两形态 getOwner()==null → `testBlackBox`。
- setMode 往返 + 失败后不变；directory/file 往返与 null；getFiles() 未显示空数组；multipleMode 往返；
  filenameFilter 身份往返（匿名类形式——JDK-8 兼容写法）；**isModal()==true +
  getModalityType()==APPLICATION_MODAL（构造即模态，易错）**；toString 含 title → `testBlackBox`。
- 反例与精确异常：`new FileDialog(parent, title, 99)/(-1)`→IllegalArgumentException（构造器校验）；
  `setMode(99)/(-1)`→IllegalArgumentException 且 getMode 不变。
- JDK-8 侧 53 断言通过（DISPLAY=:77，@key headful）。
