# java.io.Writer 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。针对抽象基类的**默认实现**：write(int)/write(String)/
> write(String,off,len)/append(...) 从 write(char[],int,int) 派生、append 返回 this、
> flush/close 抽象、null 与越界校验。

## 1. API 提供了哪些功能

- **抽象**：`write(char[],int,int)`、`flush()`、`close()`。
- **默认实现**：`write(int)`（低 16 位）、`write(char[])`、`write(String)`（null 抛 NPE）、`write(String,off,len)`。
- **append 家族**：`append(char)`、`append(CharSequence)`、`append(CharSequence,int,int)` 均返回 `this`；null CharSequence 写作 `"null"`。
- **接口**：`Appendable`、`Closeable`、`Flushable`。

## 2. Java 用户如何使用

- 继承 Writer 实现 `write(char[],int,int)` + `flush` + `close` 即可得到功能完整的字符输出流。
- 依赖 append 返回 this 做链式调用；依赖 write(String) 对 null 抛 NPE。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc**（`java/io/Writer.html`）：默认实现契约、append 返回 this、null CharSequence 写作 "null"。
- **JDK 实现源码**（`src/share/classes/java/io/Writer.java`）：`write(String)` 经 `str.length`/`str.getChars` 抛 NPE、`write(String,off,len)` 经 `str.getChars` 抛 NPE/StringIndexOutOfBoundsException。

## 4. 测试用例设计（映射到 WriterTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 所有 write 形式经 write(char[],int,int) 捕获、零长度 no-op | testWriteForms |
| P0 | append 家族返回 this、内容、null CharSequence→"null" | testAppend |
| P1 | flush/close 经基类引用派发到子类覆写 | testFlushCloseDispatch |
| P0 | 参数校验：null char[]/String→NPE、越界→IOBE | testArgumentChecking |
| P1 | 多态使用：具体 JDK 流经基类引用 | testPolymorphicUse |

## 关键规范细节（易错，已锁入断言）

- `write(int)` 只写低 16 位（0x10041 与 'A' 同低 16 位）。
- `write((String)null)` 抛 NPE；`write((char[])null)` 抛 NPE。
- `append((CharSequence)null)` 不抛异常，写入字符串 `"null"`。
- 越界 `write(char[],off,len)`/`write(String,off,len)` 抛 IOBE（后者为 StringIndexOutOfBoundsException，是 IOBE 子类）。

## 注意事项

- 全部输入为固定字符串/字符数组，无墙钟/随机/Locale/Charset/文件/网络依赖，headless 安全。
- JDK-27 侧同目录额外保留反射白盒 + 黑盒 nullWriter（JDK 11+ 方法）。

## 深化追加（契约级）

实测环境：JDK 8u372 / JDK 27.0.1（行为一致）。

### 新增维度（计数探针子类逐调用验证）
- **派生形态精确委托**：write(int) → write(cbuf,0,1) 恰一次（len "1,"）且只保留低 16 位（0x10041→'A'、0→NUL 照写）；write(String) → 恰一次 len==s.length()；write(String,off,len) → 恰一次区域写；**零长写仍委托（len 0）但不写内容**。
- **null 检查先于委托**：write((char[])null)/write((String)null) NPE 且子类零调用；**write(char[],off,len) 是抽象方法 → 基类不做区域预检**，越界异常来自子类（探针证实调用已到达子类）—— 重要的契约细节。
- write(String,off,len) 完整边界矩阵（-1,1/0,-1/1,3/4,0）→ IOOBE；失败调用零残留。
- append(csq,s,e) 越界（2,5/3,2）→ IOOBE（subSequence 层）。
- append(null)/append(null,0,4) 写四字符 "null"；空区域（2,2）写空。
- 反射白盒：abstract、Appendable+Closeable+Flushable、write(char[],int,int)/flush/close 抽象、protected lock。

### 负例矩阵（双侧实测）
| 操作 | 异常 | 备注 |
|---|---|---|
| write((char[])null)/write((String)null) | NullPointerException | 委托前检查 |
| write(new char[2],2,1) | IndexOutOfBoundsException | 异常来自子类（抽象形态无基类预检） |
| write("abc",越界) | IndexOutOfBoundsException | 双侧实际类均 StringIndexOutOfBoundsException |
| append("abc",越界) | IndexOutOfBoundsException | 同上 |

### 版本差异
- JDK-27 独有 nullWriter()（本侧不适用，javap-27 确认）。断言 11 → 47。
