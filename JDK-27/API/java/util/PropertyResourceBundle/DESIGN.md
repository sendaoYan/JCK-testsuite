# java.util.PropertyResourceBundle 测试设计

## 1. API 功能
- 构造器：`PropertyResourceBundle(InputStream)`、`PropertyResourceBundle(Reader)`。
- 查询：`getString`、`getObject`、`containsKey`、`keySet`、`getKeys`、`handleGetObject`。
- 解析：支持 properties 语法（`=` 分隔、unicode 转义）。

## 2. Java 用户如何使用
- 从流加载资源：`new PropertyResourceBundle(stream).getString("k")`。
- 从 Reader 加载：`new PropertyResourceBundle(reader)`。
- ResourceBundle.getBundle 内部对 `.properties` 文件创建该类。

## 3. 开源/源码实际用法调研
- JDK 源码 `java/util/PropertyResourceBundle.java`：`getBundle` 对 `*.properties` 的实现。
- JDK 源码 `java/util/ResourceBundle.java`：`Control` 加载 `.properties` 时实例化该类。
- 广泛用法：JDK 内部消息资源、旧式 `.properties` 国际化。

## 4. 测试用例设计 + 自动化实现
- 白盒：`Class.forName` 断言具体类、继承 ResourceBundle、关键方法存在。
- 黑盒（StringReader/ByteArrayInputStream 固定内容）：
  - 从流/Reader 取值、containsKey、keySet。
  - unicode 转义解析（` ` → 空格）。
  - getString/getObject 缺失键抛 MissingResourceException；
    getStringArray 对 String 值抛 ClassCastException。
  - `PropertyResourceBundle((InputStream/Reader)null)` → NPE。
