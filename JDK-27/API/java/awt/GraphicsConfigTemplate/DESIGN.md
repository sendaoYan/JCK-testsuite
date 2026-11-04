# java.awt.GraphicsConfigTemplate 黑盒行为测试设计

## 1. API 功能
抽象类（`implements Serializable`），描述选择 GraphicsConfiguration 的准则模板。
- 常量：`REQUIRED(1)`、`PREFERRED(2)`、`UNNECESSARY(3)`。
- 构造：`GraphicsConfigTemplate()`（无参，protected/public 由子类继承）。
- 抽象选择：`getBestConfiguration(GraphicsConfiguration[])`、`isGraphicsConfigSupported(GraphicsConfiguration)`。
- 消费方：`GraphicsDevice.getBestConfiguration(GraphicsConfigTemplate)` 委派到模板的 `getBestConfiguration(getConfigurations())`。

## 2. Java 用户如何使用
用户不直接 new 抽象类，而是匿名/具名子类实现两个抽象方法，交 `GraphicsDevice.getBestConfiguration(tpl)` 选择配置：
```java
GraphicsConfigTemplate tpl = new GraphicsConfigTemplate() {
    public GraphicsConfiguration getBestConfiguration(GraphicsConfiguration[] gc) {
        return gc.length > 0 ? gc[0] : null;
    }
    public boolean isGraphicsConfigSupported(GraphicsConfiguration gc) { return true; }
};
GraphicsConfiguration best = dev.getBestConfiguration(tpl);
```

## 3. 开源代码实际用法调研（来源标注）
- JDK 源码 `src/java.desktop/share/classes/java/awt/GraphicsConfigTemplate.java`：`REQUIRED=1/PREFERRED=2/UNNECESSARY=3`；两个抽象方法签名。
- `src/java.desktop/share/classes/java/awt/GraphicsDevice.java`：`getBestConfiguration(GraphicsConfigTemplate gct)` 实现为 `gct.getBestConfiguration(getConfigurations())`（本组 GraphicsDevice 用例据此验证委派行为）。

## 4. 测试用例设计 + 自动化实现
本类为接口型抽象模板，按简报要求仅做白盒形态 + 方法签名反射，不实例化显示。
- 白盒 `testWhiteBox()`：类型为 public abstract class、非接口、implements Serializable、extends Object；无参构造存在；两个抽象方法签名（参数/返回类型/abstract 修饰符）；三个 `public static final int` 常量。
- 黑盒 `testBlackBox()`：仅断言常量契约（REQUIRED=1/PREFERRED=2/UNNECESSARY=3 且两两不同）；不实例化。
断言 19 项，白盒 15 + 黑盒 4。
