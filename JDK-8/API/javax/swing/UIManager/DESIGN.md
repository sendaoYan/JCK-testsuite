# javax.swing.UIManager 测试设计

## 1. API 功能
LaF 全局门面：get/setLookAndFeel（实例与类名两种）、installed 列表、getDefaults、
跨平台 LaF、九类类型化默认值读取。

## 2. Java 用户如何使用
`UIManager.setLookAndFeel(new MetalLookAndFeel())`；`UIManager.getColor("Table.background")`。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/UIManager.java`；
LookAndFeel.java；LookAndFeelInfo；JDK 自带 `test/jdk/javax/swing/` 相应模块用例。

## 4. 测试用例设计 + 易错细节
- setLookAndFeel 触发 "lookAndFeel" 属性事件（newValue 为新实例）。
- setLookAndFeel(String)：合法类名 OK；**bogus 类名抛 ClassNotFoundException 且当前 LaF 不变**。
- installed：≥2 且含 Metal/Nimbus；LookAndFeelInfo 名称/类名精确。
- Metal 下的已知默认值精确锁定（yesButtonText=Yes、tabInsets=0,9,1,9、minimumSize=262x90、
  focusCellBackground 白、getUI(JLabel)=MetalLabelUI）。
- put/get 往返；getDefaults() 同一实例。
- **易错**：setLookAndFeel(null) 不抛但会毒化 UI——测试里放在最后并立即恢复 Metal。

自动化实现位于同目录 `*Test.java`：`check(boolean, msg)` 计数断言、`checkThrows` 校验精确异常类型，
`main` 结尾打印 `PASS (N assertions)`；jtreg `@run main` 直接驱动（headless 安全，Metal LaF 先行）。

