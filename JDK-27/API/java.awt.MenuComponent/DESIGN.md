# java.awt.MenuComponent 黑盒行为测试设计

## ① API 功能分组
- 构造：`MenuComponent()`（protected，抽象类）。
- 名称/父：`getName/setName/getParent`。
- 字体：`getFont/setFont`。
- 生命周期/事件：`removeNotify/postEvent/dispatchEvent`。
- 工具：`toString`。

## ② Java 用户用法
```java
// 通过具体子类使用继承的 MenuComponent 方法
MenuItem mi = new MenuItem("x");
mi.setName("comp");
mi.setFont(new Font("Dialog", Font.BOLD, 14));
```
组合链路：构造子类 → 名称/字体 setter → 父/toString 查询。

## ③ 开源实际用法调研
JDK 自用（来源 `~/git/jdk25u-dev`）：
- `MenuComponent.setName` 设置 name 字段并标记显式命名（`MenuComponent.java`）。
- `MenuComponent.setFont` 存储 font（`MenuComponent.java`）。
- `MenuComponent.getParent` 返回 `MenuContainer` 父（`MenuComponent.java`）。

## ④ 测试用例设计 + 自动化实现
- 白盒：抽象类、Serializable、关键方法存在性 → `testWhiteBox`。
- 通过 MenuItem 子类验证名称/字体/父/toString → `testBlackBox`。
