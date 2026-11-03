# javax.swing.InputVerifier 设计案

## 1. API 功能
`InputVerifier` 是输入校验抽象类，组件失去焦点前校验。方法分组：
- 校验（抽象）：`verify(JComponent)`。
- 焦点：`shouldYieldFocus(JComponent)`（默认委托 verify）、`verifyTarget(JComponent)`、`shouldYieldFocus(JComponent, JComponent)`。

## 2. Java 用户如何使用
用户继承它校验文本输入：
```java
field.setInputVerifier(new InputVerifier() {
    public boolean verify(JComponent c) {
        return ((JTextField) c).getText().matches("\\d+");
    }
});
```
典型链路：`new InputVerifier(){verify}` → `verifyTarget` → `shouldYieldFocus`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/JComponent.java` 在焦点转移时调用 `InputVerifier.shouldYieldFocus/verifyTarget`（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JComponent.java`）。
- JDK 测试 `test/jdk/javax/swing/JComponent/` 覆盖输入校验器。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验 abstract、父类 `Object`。
- 行为（黑盒）：`testBlackBox()` 覆盖匿名 `verify`、`shouldYieldFocus` 默认委托 verify、`verifyTarget`、带目标组件的 `shouldYieldFocus`。
