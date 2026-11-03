# java.util.ListResourceBundle 测试设计

## 1. API 功能
- 子类钩子：`getContents()`（返回 `Object[][]`）。
- 查询：`getString`、`getObject`、`getStringArray`、`containsKey`、`keySet`。
- 继承：`handleGetObject`、`handleKeySet`、`getKeys`（来自 ResourceBundle）。

## 2. Java 用户如何使用
- 以代码定义资源：`class MyBundle extends ListResourceBundle { Object[][] getContents() { ... } }`。
- 取值：`bundle.getString("k")`、`getStringArray("list")`。

## 3. 开源/源码实际用法调研
- JDK 源码 `java/util/ListResourceBundle.java`：自身实现与 handleGetObject 缓存。
- JDK 自带测试 `test/jdk/java/util/ResourceBundle/`：ListResourceBundle 子类取值用例。
- 广泛用法：遗留国际化方案用代码表而非 `.properties`。

## 4. 测试用例设计 + 自动化实现
- 白盒：`Class.forName` 断言抽象类、继承 ResourceBundle、`getContents` 等方法存在。
- 黑盒（固定 `getContents`）：getString/getObject/getStringArray/containsKey/getKeys/keySet 组合、
  缺失键抛 MissingResourceException。
