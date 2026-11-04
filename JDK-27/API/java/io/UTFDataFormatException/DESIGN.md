# java.io.UTFDataFormatException 测试方案 (DESIGN)

## 1. API 功能
- `(String)` / `()`：modified UTF-8（DataInput.readUTF）编码非法（坏首字节、断续字节、编码长度超 65535）。

## 2. Java 用户如何使用
- readUTF 解码损坏串时捕获；writeUTF 串太长（>65535 编码字节）时同异常。

## 3. 开源代码实际用法调研
- 解码检查 — `jdk25u-dev/src/java.base/share/classes/java/io/DataInputStream.java`（readUTF 的 UTF 字节校验）。
- 长度上限 — `jdk25u-dev/src/java.base/share/classes/java/io/ObjectOutputStream.java`（writeUTF 长度检查）与 `DataOutputStream.writeUTF`。

## 4. 测试用例设计 + 自动化实现（UTFDataFormatExceptionTest.java）
- 白盒: extends IOException、两构造。
- 黑盒场景:
  1. 构造器。
  2. 手工字节流矩阵触发：0xFF 首字节 / 0xC3 后接非续字节 / 3 字节序列被声明长度截断。
  3. 声明长度超实体 → EOFException（对照）。
  4. writeUTF 65536+ 双字节字符 → 抛出（长度超限）。
  5. 对照：ASCII/2 字节/3 字节/NUL(C0 80) 正常往返。
  6. 可按 IOException 捕获。
- 健壮性: 手工字节，无随机。

产出：`UTFDataFormatExceptionTest.java`（白盒 4 + 黑盒 12 断言）。
