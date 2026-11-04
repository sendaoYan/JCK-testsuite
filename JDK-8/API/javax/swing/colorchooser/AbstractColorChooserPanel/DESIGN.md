# javax.swing.colorchooser.AbstractColorChooserPanel 测试设计（JDK-8 侧）

## 1. API 功能
颜色选择面板基类（extends JPanel）：5 个抽象方法（buildChooser/updateChooser/
getDisplayName/getSmallDisplayIcon/getLargeDisplayIcon）；install/uninstall 生命周期
（由 JColorChooser UI 调用）；getMnemonic/getDisplayedMnemonicIndex。

## 2. Java 用户如何使用
子类实现 5 抽象方法 → `chooser.addChooserPanel(panel)`；面板在 install 后通过
getColorSelectionModel() 读写当前颜色。

## 3. 开源代码实际用法调研（本地源码+自带测试，web 未使用）
`javax/swing/colorchooser/AbstractColorChooserPanel.java`；DefaultSwatchChooserPanel 等内置面板。

## 4. 测试用例设计 + 易错细节（headless 锁定，Metal LaF + EDT）
- 反射：abstract、恰好 5 个抽象方法、superclass JPanel。
- **默认：getMnemonic()==0、getDisplayedMnemonicIndex()==-1、getColorSelectionModel()==null
  （install 前）、生命周期回调计数 0**（注意：无 setMnemonic 公有方法，助记符由子类覆写 getter）。
- install 契约：installChooserPanel(chooser) → buildChooser 恰好 1 次 + updateChooser 恰好 1 次 +
  模型接线为 chooser.getSelectionModel()。
- **二次 install 同一面板实例 → RuntimeException（"already installed"）**；
  uninstallChooserPanel → 模型解链（null）。
- install 后通过面板模型可见 chooser 的 setSelectedColor。

自动化实现位于同目录 `AbstractColorChooserPanelTest.java`（headless，无 @key）。
