# java.io.WriteAbortedException 测试方案 (DESIGN)

## 1. API 功能
- `(String detail, Exception ex)`：写侧异常中止了序列化，读侧读该流时抛出；公共字段 `detail` 保存原始写侧异常；`getMessage()` = `detail message + "; " + detail`；`getCause()` 返回 detail。

## 2. Java 用户如何使用
- 读一个写入中途失败的流（写方 writeObject 抛过异常但流已产生部分数据）时，读方得到本异常并可取 detail 定位写侧根因。

## 3. 开源代码实际用法调研
- 抛出点 — `jdk25u-dev/src/java.base/share/classes/java/io/ObjectInputStream.java`（读 TC_EXCEPTION 时恢复写侧异常表）。
- 写侧记录 — `jdk25u-dev/src/java.base/share/classes/java/io/ObjectOutputStream.java`（writeObject 异常写入 TC_EXCEPTION/bout）。
- 官方测试 — `jdk25u-dev/test/jdk/java/io/Serializable/writeAbortedException/`。

## 4. 测试用例设计 + 自动化实现（WriteAbortedExceptionTest.java）
- 白盒: extends ObjectStreamException、public detail、getMessage/getCause 重写、单构造。
- 黑盒场景:
  1. 构造：组合 message、detail 字段、getCause==detail。
  2. 真实触发 1：自定义 writeObject 抛 IOException → 写侧原样传播，读侧抛 WAE 且 detail 为原异常。
  3. 真实触发 2：非 Serializable 字段毒化流 → 读侧 WAE.detail 为 NotSerializableException。
  4. 可按 ObjectStreamException 捕获。
- 健壮性: 全内存流。

产出：`WriteAbortedExceptionTest.java`（白盒 8 + 黑盒 7 断言）。
