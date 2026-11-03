# java.io.IOError 测试方案 (DESIGN)

## 1. API 功能
- `(Throwable cause)`：严重不可恢复 I/O 错误（Error 而非 Exception），message 默认 cause.toString()，无 message-only 构造。

## 2. Java 用户如何使用
- 表示"不该被 catch (IOException) 恢复"的致命 I/O 问题（如 Console 读失败）；一般不捕获，任其终止线程。

## 3. 开源代码实际用法调研
- 类型定义 — `jdk25u-dev/src/java.base/share/classes/java/io/IOError.java`。
- 抛出先例 — `jdk25u-dev/src/java.base/share/classes/java/io/Console.java`（readLine/readPassword 的底层失败包装为 IOError）。

## 4. 测试用例设计 + 自动化实现（IOErrorTest.java）
- 白盒: extends Error、唯一 (Throwable) 构造、非 RuntimeException。
- 黑盒场景:
  1. 构造：cause 保留、message=cause.toString()。
  2. 任意 Throwable（不止 IOException）可包装。
  3. 无 (String) 构造（反射证实）。
  4. 不受检传播。
  5. 是 Error 不是 Exception（类型系统证明，杜绝被 catch(IOException) 误捕）。
  6. toString/stackTrace。
- 健壮性: 全内存。

产出：`IOErrorTest.java`（白盒 4 + 黑盒 10 断言）。
