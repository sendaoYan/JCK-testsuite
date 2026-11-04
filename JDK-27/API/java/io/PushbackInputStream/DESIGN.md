# java.io.PushbackInputStream 测试方案 (DESIGN)

## 1. API 功能（按方法分组）
- 构造：`(InputStream)`（回退 1 字节）、`(InputStream, int size)`。
- `unread(int)`、`unread(byte[])`、`unread(byte[],off,len)`（LIFO 回退）；`read()`/`read(byte[],int,int)`（先出回退区）；`available()`（含回退区计数）。
- `skip`、`markSupported()==false`（不支持 mark/reset）、`close`。

## 2. Java 用户如何使用
- 词法扫描回退超前读：读一个字节判定类型，不合则 `unread` 回去重新读；解析器/StreamTokenizer 类逻辑。

## 3. 开源代码实际用法调研（真实片段，来源标注）
- 回退缓冲实现与 "Push back buffer is full" — `jdk25u-dev/src/java.base/share/classes/java/io/PushbackInputStream.java`。
- markSupported 返回 false 的覆盖 — 同文件。

## 4. 测试用例设计 + 自动化实现（PushbackInputStreamTest.testBlackBox）
1. read→unread→read 得到同一字节（LIFO）；unread 多字节后按后进先出次序吐出。
2. 显式 size-2/size-3 构造下多字节回退；默认 1 字节缓冲第二次 unread 抛 IOException("Push back buffer is full")。
3. available() = 底层 + 已回退计数；skip 先耗尽回退区。
4. markSupported()==false；close 后 read 抛异常。
- 健壮性: BAIS 固定数据。

产出：`PushbackInputStreamTest.java`（白盒 + 黑盒共 31 断言）。

## 深化追加（契约级，批次 ioC）

新增维度清单（与 JDK-8 侧完全镜像 + JDK-27 特有 testTransferToContract）：

1. **回推满精确消息**（三种 unread 形式 → "Push back buffer is full" + 缓冲原样）。
2. **close 后全操作精确消息**（read/read(b)/unread(int)/unread(arr)/available/skip/skip(负) → "Stream closed"）。
3. **unread(int) 低 8 位**（0x141→0x41、0→0、-1→255）。
4. **skip×回推**（队头先耗、恰耗尽、跨区 6、pushback-only）。
5. **available 算术**。
6. **超前读 + len==0 no-op**。
7. **单次 bulk read 跨回推+流（LIFO）**。
8. **交错 parser 序列**。
9. **JDK-27 特有（transferTo 覆写，javap 确认为本类声明）**：中途 unread 后 transferTo 计数含回退字节且顺序还原（"abcdef"）、EOF；无回退时只搬剩余；transferTo 排空后仍可 unread 再读。

负例矩阵与消息：同 JDK-8（双侧一致："Push back buffer is full"/"Stream closed"/NPE/IOBE/IAE）。同步修饰差异（mark 非同步）不构成契约差异。

产出：`PushbackInputStreamTest.java` 27 → 82 断言（jtreg Passed）。
