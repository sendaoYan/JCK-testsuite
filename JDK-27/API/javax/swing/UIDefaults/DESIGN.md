# javax.swing.UIDefaults 设计案

## 1. API 功能
`UIDefaults` 存储外观默认值（继承 `Hashtable`）。方法分组：
- 类型化取值：`getInt/getBoolean/getString/getColor/getFont/getIcon/getInsets/getDimension`。
- 批量：`putDefaults`。
- 资源/区域：`addResourceBundle/removeResourceBundle/setDefaultLocale/getDefaultLocale`。

## 2. Java 用户如何使用
LookAndFeel 用 UIDefaults 存储颜色/字体等：
```java
UIDefaults defaults = UIManager.getDefaults();
Color c = defaults.getColor("control");
defaults.putDefaults(new Object[] { "key", value });
```
典型链路：`put` → `getInt/getColor/getFont` → `putDefaults`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/UIDefaults.java` 是 JDK 自身实现（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/UIDefaults.java`）。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验父类 `Hashtable`。
- 行为（黑盒）：`testBlackBox()` 覆盖各类型化 getter、缺失键 null、`putDefaults`、数组构造、默认区域 get/set。
