# jdk.swing.interop.DropTargetContextWrapper 设计案

## 1. API 功能
拖放目标上下文抽象类。

## 2. Java 用户如何使用
嵌入场景子类化。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.swing.interop/share/classes/jdk/swing/interop/DropTargetContextWrapper.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 abstract；黑盒降级。

## 5. 契约深化（JDK-27）

- 抽象面恰 11 方法（setTargetActions/getTargetActions/getDropTarget/getTransferDataFlavors/getTransferable/isTransferableJVMLocal/acceptDrag/rejectDrag/acceptDrop/rejectDrop/getDropTargetContextPeer），全部 public abstract。
- 具体辅助方法恰两个：reset、setDropTargetContext（把 JDK 内部 DropTargetContext 绑进 wrapper）。
- public 无参构造器允许外部框架匿名实现。
