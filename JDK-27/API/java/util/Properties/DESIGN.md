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

## 深化追加（契约级，2026-11）

### 新增覆盖

| 维度 | 内容 |
|---|---|
| \\uXXXX 转义 | store(OutputStream) 对 >0x7F 全部转义 \\\\uXXXX、round-trip equals；store(Writer) 原样不转义、load(Reader) 回读 equals；控制字符（tab/换行）经转义 round-trip；load 解码 \\\\u4E2D\\\\u6587 → 中文 |
| list() | 头行 `-- listing properties --`、key=value、长值截 37+`...`（键不截）、defaults 也列出（成员断言）、非 String 键/值 → CCE（实测，非跳过）、PrintStream/PrintWriter 一致 |
| 解析边界 | 无值行→空值、头/尾空格规则、转义尾空格、行首空白、`#`/`!` 注释、键内转义 `=`、显式空值；截断 \\\\u4E → IAE |
| Hashtable 视图通道 | keySet().remove、entrySet setValue 反写、compute 非 String 值 getProperty 盲/get 可见、forEach 原始表 |
| 版本面（JDK 22+） | Properties(int) 可用且 -1 → IAE；storeToXML(os,comment,Charset) 中文 round-trip、XML 声明存在 |

### 易错点

- 同 JDK-8 侧：Writer/OutputStream 转义差异、list 含 defaults、getProperty 只见 String。
- Properties(int) 与 storeToXML Charset 重载为 JDK 22+ 新面，JDK-8 侧反射锁缺席。
