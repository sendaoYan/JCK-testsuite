# java.io.StringBufferInputStream 测试方案 (DESIGN)

## 1. API 功能（按方法分组）
- @Deprecated 字节视图：`(String)`，每字符取低 8 位（忽略高位，非 ASCII 损坏）。
- `read()`/`read(byte[],int,int)`、`skip`、`available()`（全长度）、`reset()`（回起点，无需 mark）、`close()`（无操作）。
- protected buffer/count/pos；无 mark 支持（mark/reset 语义退化为 reset 回 0）。

## 2. Java 用户如何使用
- 遗留 API：ASCII 测试数据字节源；新代码用 ByteArrayInputStream 或 StringReader（正确 Unicode）。

## 3. 开源代码实际用法调研（真实片段，来源标注）
- 低 8 位截断实现 — `jdk25u-dev/src/java.base/share/classes/java/io/StringBufferInputStream.java`（`buffer[pos++] & 0xFF`...）。

## 4. 测试用例设计 + 自动化实现（StringBufferInputStreamTest.testBlackBox）
1. 顺序/区域/bulk 读与 EOF；skip 夹紧；available 全长度。
2. 'é'(U+00E9) 读出 0xE9——低 8 位截断的实证（非 ASCII 损坏）。
3. reset 无 mark 回起点；close 后仍可读（无失效语义）；空串即 EOF。
- 健壮性: 纯内存。

产出：`StringBufferInputStreamTest.java`（白盒 + 黑盒共 24 断言）。

## 深化追加（契约级）

新增维度（全部经 JDK-27 实测锁定）：
- 构造 null NPE；低 8 位截断矩阵（ሴ→0x34、ÿ→0xFF、Ā→0x00、￿→0xFF）；ASCII 值矩阵
- read(b,off,len) 参数矩阵、区域填充、部分填充
- SBIS 特有 EOF 优先：EOF 处 len==0 读也返回 -1（区别于基类）
- skip 矩阵；markSupported false；mark no-op；reset 总回 0；close no-op 后仍可读
- available 生命周期（满长/递减/reset 恢复/EOF 0）

负例矩阵：ctor(null) NPE / read(b,1,8) IOOBE / read(b,-1,0) IOOBE / read(null,0,1) NPE / read(null) NPE。

版本差异记录：双侧行为一致；JDK-27 的 ctor(null) NPE 带 helpful 消息（指名 s.length()），8 为 null——仅消息差异，均断类型级。
