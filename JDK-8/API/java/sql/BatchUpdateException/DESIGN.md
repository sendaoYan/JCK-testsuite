# java.sql.BatchUpdateException 测试设计案（JDK-8）

> 完整契约：11 构造器矩阵、int[]/long[] 更新计数**防御性拷贝**、int↔long 互填、null 计数、cause、序列化。

## 1. API 提供功能
- 构造器（11）：int[] 系 5 个 + long[] 系 1 个（`(String,String,int,long[],Throwable)`）+ 5 个 cause 变体。
- 访问器：`getUpdateCounts()`（int[] 拷贝）、`getLargeUpdateCounts()`（long[] 拷贝）。

## 2. 使用方式
批量更新失败时驱动抛此异常；读 `getUpdateCounts()/getLargeUpdateCounts()` 得到失败前成功命令的更新计数。

## 3. 参考来源
- javadoc `java/sql/BatchUpdateException.html`；源码 `~/git/jdk8u-dev/.../java/sql/BatchUpdateException.java`；JCK `api/java_sql/BatchUpdateException`。

## 4. 测试用例设计
| 测试点 | 用例方法 |
|---|---|
| 11 构造器 + message/SQLState/errorCode/counts 默认值 | testConstructors |
| 防御性拷贝（构造入参、getter 返回均拷贝） | testDefensiveCopy |
| int↔long 互填（int[] 构造器填 large、long[] 构造器填 int） | testCrossPopulation |
| null counts → 两者 null | testCrossPopulation |
| cause 构造器（message=cause.toString()） | testCauseConstructors |
| 层次（extends SQLException） | testHierarchy |
| 序列化 round-trip + serialVersionUID=5977529877145521757L | testSerialization |

## 关键规范细节
- `getUpdateCounts()` / `getLargeUpdateCounts()` 返回**防御性拷贝**，改返回值不影响内部状态。
- int[]/long[] 互填**不做溢出校验**（long→int 可能回绕，spec 注明）。
