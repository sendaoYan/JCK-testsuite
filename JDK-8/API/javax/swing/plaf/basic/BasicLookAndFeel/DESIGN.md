# javax.swing.plaf.basic.BasicLookAndFeel — DESIGN

## 1. API 功能
抽象共享引擎（Metal 的父类）。`BasicLookAndFeelContractTest` 用最小具体子类锁定：抽象面（getID/getName/getDescription/isNativeLookAndFeel/isSupportedLookAndFeel 五钩子留抽象）、自己实现的 getDefaults/initialize/uninitialize/initClassDefaults/initSystemColorDefaults/initComponentDefaults。

## 2. Java 用户如何使用
用户不直接实例化；写第三方 LaF 时 extends BasicLookAndFeel 并实现五钩子。`getDefaults()` 返回把所有组件映射到 Basic*UI 的 UIDefaults；`initialize()/uninitialize()` 生命周期由 UIManager.setLookAndFeel 调用。

## 3. 开源用法调研
- `jdk8u-dev/jdk/src/share/classes/javax/swing/plaf/basic/BasicLookAndFeel.java`：initComponentDefaults 注入 "ButtonUI"→"javax.swing.plaf.basic.BasicButtonUI" 等。
- `jdk8u-dev/jdk/src/share/classes/javax/swing/plaf/metal/MetalLookAndFeel.java`：官方子类，覆盖 initXxx 前后钩子。
- LookAndFeel.toString：`"[" + getDescription() + " - " + getClass().getName() + "]"`。

## 4. 测试用例设计 + 自动化实现（30 断言）
- 抽象面：BasicLookAndFeel abstract + extends LookAndFeel；五钩子仍 abstract（反射 isAbstract）。
- 具体服务：六方法在 declaredMethods 中。
- 子类身份：getName/getID/getDescription 往返；getSupportsWindowDecorations 默认 false；toString 格式精确（[desc - 类名]）。
- getDefaults：非空且 ButtonUI/LabelUI/ScrollBarUI 映射到 basic.* 实现类名。
- 生命周期：initialize 两次、uninitialize 后不再抛。
- Metal 实例 instanceof BasicLookAndFeel；安装后按钮 UI 为 ButtonUI；Metal getDefaults 表非空。

## 5. 易错规范细节
- BasicLookAndFeel 不实现 getSupportsWindowDecorations（继承 LookAndFeel 返回 false），Metal 覆盖为 true。
- ProbeSub 若漏实现 isNativeLookAndFeel/isSupportedLookAndFeel 编译即失败——两钩子在 BasicLookAndFeel 层仍是抽象的。
