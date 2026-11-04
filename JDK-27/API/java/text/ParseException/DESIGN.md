# java.text.ParseException 测试方案

## 1. API 功能（按方法分组）

- 构造器：`ParseException(String s, int errorOffset)`。
- 查询：`getErrorOffset()`。
- 继承自 `Exception`/`Throwable`：`getMessage()`、`initCause(Throwable)`、`getCause()`、`getStackTrace()`。

## 2. Java 用户如何使用

- 构造：`new ParseException("bad", 5)` 表示解析失败，错误位于偏移 5。
- 典型调用：`catch (ParseException e) { e.getErrorOffset(); }` 定位错误位置。
- 组合链路：`DateFormat.parse` 抛 `ParseException` → `getMessage/getErrorOffset` → `initCause/getCause` 挂底层原因。

## 3. 开源代码实际用法调研（来源明确）

- JDK 源码 `src/java.base/share/classes/java/text/ParseException.java`：存储 `errorOffset`，`getErrorOffset()` 返回偏移。
- JDK 源码 `src/java.base/share/classes/java/text/SimpleDateFormat.java`、`MessageFormat.java`、`NumberFormat.java` 的 `parse` 均抛 `ParseException`。
- JDK 测试 `test/jdk/java/text/Format/*.java` 大量 `assertThrows(ParseException.class, ...)`。

## 4. 测试用例设计 + 自动化实现

实现位置：`ParseExceptionTest.java`（`testWhiteBox()` + `testBlackBox()`）。

- 白盒：继承 `Exception`、构造器 `(String,int)`、`getErrorOffset` 存在。
- 黑盒：message/errorOffset、checked 层次、`initCause/getCause` 因果链、`getStackTrace`、`SimpleDateFormat.parse` 抛出的 `ParseException`。
- 健壮性：纯对象构造，无 locale/墙钟/全局副作用，headless-safe，不加 `@key`。

## 契约深化（本批新增，两侧同构断言）

- 构造链（message+errorOffset）、getErrorOffset（非 getErrorIndex）
- errorOffset 镜像 ParsePosition.errorIndex（经 DateFormat 严格解析 "2024-02-30" err=10 实证）
- serialVersionUID=2703218443322787634；toString 含 message+offset
