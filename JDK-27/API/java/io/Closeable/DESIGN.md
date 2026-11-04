# java.io.Closeable 测试方案 (DESIGN)

## 1. API 功能
- `close() throws IOException`，extends AutoCloseable；流/读/写资源释放的标准接口。

## 2. Java 用户如何使用
- try-with-resources 自动调用；手工资源管理；lambda 适配第三方资源。

## 3. 开源代码实际用法调研
- 定义 — `jdk25u-dev/src/java.base/share/classes/java/io/Closeable.java`（close 幂等要求）。
- 官方测试 — `jdk25u-dev/test/jdk/lang/exceptions/`（TWR 行为）。

## 4. 测试用例设计 + 自动化实现（CloseableTest.java）
- 白盒: 接口、extends AutoCloseable、close 声明 IOException、InputStream/Reader 实现它。
- 黑盒场景:
  1. lambda 实现 + instanceof AutoCloseable。
  2. TWR 恰好 close 一次、块内未 close。
  3. 异常路径先 close 再抛出。
  4. close 自身抛 IOException 传播。
  5. 多资源逆序关闭。
  6. 幂等 close（实现方自证）。
  7. BAIS 经接口使用。
- 健壮性: 全内存。

产出：`CloseableTest.java`（白盒 7 + 黑盒 10 断言）。

## 深化追加（契约级）

实测环境：JDK 8u372 / JDK 27.0.1（TWR 语义一致）。

### 新增维度
- **抑制异常（suppressed）**：body 抛 ISE + close 抛 IOE → ISE 传播且 getSuppressed() 恰含该 IOE（消息保真）。
- **双失败 close**：逆序关闭 → 后声明者先关、其异常传播；先声明者的 close 失败成为 suppressed（实测 "close-inner" 传播 / "close-outer" 被抑制）。
- 三资源逆序精确序列（c,b,a）；嵌套 TWR：内层块结束即关内资源，外层随后。
- body 内显式 close() 后隐式 close 仍执行（计数 2）。
- AutoCloseable/Closeable 边界：纯 AutoCloseable 匿名类 **不是** Closeable；Closeable 必是 AutoCloseable；无检查异常 lambda 实现合法。
- JDK 实现经接口观察：StringReader 关闭后 read → IOException("Stream closed")（close 有实效）；StringWriter.close no-op（关闭后仍可写）。
- 反射白盒：接口、仅 extends AutoCloseable、close 抽象且 throws IOException、InputStream/Reader/Writer 均实现。

### 负例矩阵（双侧实测）
| 场景 | 结果 |
|---|---|
| body 异常 + close 失败 | body 异常传播，close 异常进 suppressed |
| 两资源 close 均失败 | 后声明资源的 close 异常传播，另一为 suppressed |
| close 抛 IOException（无 body 异常） | 直接传播 |

### 版本差异
- 无。断言 17 → 36（含既有反射白盒）。
