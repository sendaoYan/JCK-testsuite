# java.io.CharArrayWriter 测试设计案（JDK-27）

> 覆盖目标：**完整契约**（黑盒）+ 反射白盒结构。与 JDK-8 侧同源。

## 1. API 功能
- 构造器（2）：`CharArrayWriter()`、`CharArrayWriter(int)`（负→IAE）。
- 写：`write(int)`、`write(char[],int,int)`、`write(String,int,int)`。
- 追加/查询：`append*`、`writeTo(Writer)`、`reset()`、`toCharArray()`、`size()`、`toString()`。

## 2. 使用方式
- 收集字符到内存；`toCharArray()` 取副本；`writeTo` 直拷；`reset` 复用。

## 3. 参考来源
- javadoc + `~/git/jdk/src/java.base/share/classes/java/io/CharArrayWriter.java`。

## 4. 测试用例设计
| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | 反射白盒：继承 Writer、write(int)/writeTo/toCharArray/size/reset 存在 | testWhiteBox |
| P0 | 构造器负容量 IAE；write(int) 低 16 位；write 校验 | testConstructors、testWriteInt、testWriteArrayValidation |
| P0 | write(String,off,len)；writeTo；append | testWriteString、testWriteTo、testAppend |
| P0 | reset/size/toCharArray 副本/toString；flush/close no-op | testResetSizeToCharArray、testFlushClose |

## 关键细节
- 继承的 `write(String)` 抛 IOException；`write(String,off,len)` 不抛。

## 深化追加（契约级）

实测环境：JDK 8u372 / JDK 27.0.1（行为一致）。

### 新增维度
- write(int) 低 16 位边界矩阵（0/0xD7FF/0xE000/0xFFFF/0x10041/0x1FFFF）。
- 继承 write(char[])/write(String)（Writer 默认 → 3 参形式）；null 参数 NPE；空数组/空串 no-op。
- write(String,off,len) 边界矩阵 + 失败不污染缓冲 + off==length 且 len==0 合法 + 代理对原样写入。
- append 协变返回 CharArrayWriter；append(null,0,4) → "null"；越界 IOOBE。
- **writeTo 委托探针**：自定义 Writer 记录 —— 整个缓冲以**单次** out.write(buf,0,count) 交付；源不被消费；目标失败时 IOException 原消息传播；writeTo(null) NPE；空 writer 不写任何内容。
- 扩容：默认 32 容量跨 100 字符逐字符写 + 单次 bulk 写越 4 容量 + 单次 String 写越 2 容量，数据完整（size/toCharArray/toString 三重验证）。
- toCharArray 独立性：连续两次返回不同对象；改副本互不影响。
- close/flush 无操作深契约：close 后 write 各形态/append/flush/二次 close/size/toCharArray/toString 照常。

### 负例矩阵（双侧实测）
| 操作 | 异常 | 备注 |
|---|---|---|
| new CharArrayWriter(-1) | IllegalArgumentException | 已有 |
| write((char[])null)/write((String)null)（含继承形态） | NullPointerException | - |
| write("abc",越界) | IndexOutOfBoundsException | 双侧实际类均 StringIndexOutOfBoundsException |
| append 越界 | IndexOutOfBoundsException | 同上 |
| writeTo(null) | NullPointerException | 含空 writer |

### 版本差异
- **CharArrayWriter.toString(Charset) 双侧均不存在**（javap-8/javap-27 逐名确认；任务简报中"JDK-10+ 有 toString(Charset)"仅适用于 ByteArrayOutputStream，本类无）。断言 16 → 73。
