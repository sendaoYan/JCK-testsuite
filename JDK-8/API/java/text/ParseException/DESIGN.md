# java.text.ParseException 测试设计案（JDK-8）

> 覆盖目标：完整契约（白盒层次、message/errorOffset、因果链、实际抛出），非冒烟。

## 1. API 提供了哪些功能

- 构造器：`ParseException(String, int errorOffset)`。
- 查询：`getErrorOffset()`；继承 `Exception`/`Throwable` 的 `getMessage()`、`initCause/getCause`、
  `getStackTrace()`。

## 2. Java 用户如何使用

- `catch (ParseException e) { e.getErrorOffset(); }` 定位失败偏移；`initCause` 挂底层原因。

## 3. 参考来源（不凭记忆）

- Oracle Java SE 8 javadoc + `~/git/jdk8u-dev/jdk/src/share/classes/java/text/ParseException.java`。
- JDK-27 同目录 DESIGN.md（行为一致）。

## 4. 测试用例设计（映射到 ParseExceptionTest.java）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒：具体类、extends Exception、ctor(String,int)、getErrorOffset | testWhiteBox |
| P0 | message/errorOffset、checked 非 unchecked、getStackTrace 非空 | testBlackBox |
| P0 | initCause/getCause 因果链 | testBlackBox |
| P0 | SimpleDateFormat.parse 实际抛 ParseException 且 errorOffset>=0 | testBlackBox |

## 关键规范细节（易错，已锁入断言）

- `ParseException` 是 checked `Exception`（非 RuntimeException）。

## 注意事项

- 纯对象构造，无 Locale/墙钟/全局副作用；headless-safe 不加 `@key`。

## 契约深化（本批新增，两侧同构断言）

- 构造链（message+errorOffset）、getErrorOffset（非 getErrorIndex）
- errorOffset 镜像 ParsePosition.errorIndex（经 DateFormat 严格解析 "2024-02-30" err=10 实证）
- serialVersionUID=2703218443322787634；toString 含 message+offset
