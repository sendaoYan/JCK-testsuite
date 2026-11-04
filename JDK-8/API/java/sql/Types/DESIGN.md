# java.sql.Types 测试设计案（JDK-8）

> 完整契约：每个 public static final int 常量的**精确值**（含负数 XOPEN 码）+ 字段修饰符 + 常量集完整性。

## 1. API 提供功能
39 个 JDBC 类型常量（`BIT/TINYINT/.../TIMESTAMP_WITH_TIMEZONE`），均为 `public static final int`。

## 2. 使用方式
`setNull(1, Types.VARCHAR)` 等用 `Types.X` 指定 SQL 类型。

## 3. 参考来源
- javadoc `java/sql/Types.html`；源码 `~/git/jdk8u-dev/.../java/sql/Types.java`；JCK `api/java_sql/Types`。

## 4. 测试用例设计
| 测试点 | 用例方法 |
|---|---|
| 精确值表（含 BIT=-7、BIGINT=-5、NULL=0、OTHER=1111 等 39 项） | main（表驱动） |
| 每字段 public/static/final/int | main（反射） |
| 常量总数精确匹配（无缺漏/多余） | main（反射） |

## 关键规范细节
- **部分常量为负**（XOPEN 码：BIT=-7、TINYINT=-6、BIGINT=-5、LONGVARBINARY=-4、VARBINARY=-3、BINARY=-2、LONGVARCHAR=-1、ROWID=-8、NCHAR=-15、NVARCHAR=-9、LONGNVARCHAR=-16）。任务简报中的「Types 常量非负」与规范不符，故改为**精确值断言**。
- JDK-27 额外增加 `DECFLOAT=2015`、`JSON=2016`（镜像侧已加）。
