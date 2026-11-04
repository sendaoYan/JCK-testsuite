# javax.swing.Renderer 设计案

## 1. API 功能
`Renderer` 接口抽象渲染器。方法分组：
- 赋值：`setValue(Object, boolean)`。
- 组件：`getComponent()`。

## 2. Java 用户如何使用
用户实现 Renderer 供列表/表格等复用组件：
```java
Renderer renderer = new Renderer() {
    public void setValue(Object value, boolean selected) { label.setText(String.valueOf(value)); }
    public Component getComponent() { return label; }
};
```
典型链路：`setValue` → `getComponent`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/Renderer.java` 定义该接口（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/Renderer.java`）。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验接口。
- 行为（黑盒）：`testBlackBox()` 匿名实现覆盖 `setValue/getComponent`，验证赋值后组件更新。
