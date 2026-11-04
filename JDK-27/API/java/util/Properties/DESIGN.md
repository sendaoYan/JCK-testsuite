# java.util.Properties 测试设计

## 1. API 功能
- 构造器：`Properties()`、`Properties(Properties defaults)`、`Properties(int)`。
- 属性读写：`setProperty`、`getProperty(key[, default])`、`remove`。
- 批量：`stringPropertyNames`、`propertyNames`、`load(Reader/InputStream)`、`store(Writer/OutputStream)`、`loadFromXML`/`storeToXML`。
- 继承自 Hashtable：`put`/`get`/`containsKey`/`size` 等。

## 2. Java 用户如何使用
- 配置读写：`p.setProperty("k", "v")` / `p.getProperty("k", "default")`。
- 默认值链：`new Properties(defaults)`。
- 持久化：`p.store(writer, comment)` → `new Properties().load(reader)`。
- 键枚举：`p.stringPropertyNames()`。

## 3. 开源/源码实际用法调研
- JDK 源码 `java/lang/System.java`：系统属性即 `Properties`。
- JDK 源码 `java/time/chrono/HijrahChronology.java`：内部用 `Properties` 读配置文件。
- JDK 源码 `java/util/Properties.java`：自身 `load/store` 实现。
- 广泛用法（Spring/Guava 前身）：`Properties` 加载 `.properties` 配置。

## 4. 测试用例设计 + 自动化实现
- 白盒：`Class.forName` 断言具体类、继承 Hashtable、关键方法存在。
- 黑盒（用 StringWriter/StringReader 往返，不写临时文件）：
  - setProperty/getProperty/default、size/containsKey、覆盖与 remove 回退默认值。
  - stringPropertyNames 排除默认、propertyNames 包含默认+自有。
  - store/load 往返（含空格与 `=` 的值）。
  - put/get 非字符串值与 getProperty 忽略非字符串。
