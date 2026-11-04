# javax.swing.plaf.basic.BasicLookAndFeel — DESIGN

## 1. API 功能
`LookAndFeel` 抽象基类，实现 `Serializable`：`getDefaults`、`initialize`、`uninitialize`、`initClassDefaults`/`initComponentDefaults`/`initSystemColorDefaults`。

## 2. Java 用户如何使用
抽象类不可直接实例化，由具体子类（Basic*/Metal* UI）继承；本测试白盒验证抽象修饰与公开契约方法签名。

## 3. 开源代码实际用法调研（来源：JDK 源码 jdk25u-dev + test/jdk）
来源：JDK 源码 `src/java.desktop/share/classes/javax/swing/plaf/basic/BasicLookAndFeel.java`（jdk25u-dev）；为 LookAndFeel/TextUI 体系基类。

## 4. 测试用例设计 + 自动化实现（`BasicLookAndFeelTest.testBlackBox`）
白盒结构断言 + 反射验证关键公开方法返回类型（降级白盒，因抽象类不可实例化）。

---

## 契约深化（本轮追加，`BasicLookAndFeelContractTest`，30 断言）

- **抽象面**（反射）：BasicLookAndFeel abstract + extends LookAndFeel；getID/getName/getDescription/isNativeLookAndFeel/isSupportedLookAndFeel 五钩子在该层仍 abstract；getDefaults/initialize/uninitialize/initClassDefaults/initSystemColorDefaults 为已声明方法。
- **最小子类 ProbeBasic**（实现五钩子）：getName/getID/getDescription 往返；getSupportsWindowDecorations 默认 false；toString 精确格式 `"[ProbeBasicDesc - <类名>]"`。
- **getDefaults**：非空且 ButtonUI/LabelUI/ScrollBarUI 映射到 `javax.swing.plaf.basic.Basic*UI` 类名。
- **生命周期**：initialize() 两次、uninitialize() 后再调用不抛。
- **Metal 关系**：MetalLookAndFeel instanceof BasicLookAndFeel；Metal getDefaults 表非空。

### 版本备注
双侧（JDK-8/JDK-27）行为一致；JDK-27 源码锚点 `~/git/jdk27/src/java.desktop/share/classes/javax/swing/plaf/basic/BasicLookAndFeel.java`。
