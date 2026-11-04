# java.io.StringBufferInputStream 测试方案 (DESIGN)

## 1. API 功能
- 弃用（@Deprecated）的 InputStream 字节视图：把 String 每字符取低 8 位当字节。
- read()/read(byte[])/read(byte[],int,int)/skip(long)/available()/reset()。

## 2. Java 用户如何使用
- 旧式把字符串当字节流读取（新代码应使用 StringReader）。

## 3. 开源代码实际用法调研
- 实现 — `jdk8u-dev/jdk/src/share/classes/java/io/StringBufferInputStream.java`。

## 4. 测试用例设计 + 自动化实现（StringBufferInputStreamTest.java，纯黑盒）
黑盒场景:
1. 顺序 read() + EOF。
2. read(byte[],off,len) 区域 + read(byte[]) 剩余 + EOF。
3. 非 ASCII 只取低 8 位（'é'→0xE9）。
4. available 全长、skip(2)、skip 截断到剩余。
5. reset 回卷到位置 0（无需 mark）。
6. 精确长度读尽。
7. close 为无操作（close 后仍可读）。
8. 空串 EOF + available 0。
- 健壮性: 内存；无反射白盒。

## 深化追加（契约级）

新增维度（全部经 JDK-8 实测锁定）：
- 构造 null → NPE
- 低 8 位截断矩阵：ሴ→0x34、ÿ→0xFF、Ā→0x00、￿→0xFF（只取低 8 位，非 16 位）；ASCII 值矩阵 Az09→65,122,48,57
- read(b,off,len) 参数矩阵：(b,1,8)/(b,-1,0)/(b,0,-1) IOOBE、null NPE、区域读 off..off+len 填充与界外槽不动、部分填充（读剩余数、未填槽保持 0）
- **SBIS 特有：EOF 检查先于长度检查——EOF 处 read(b,off,0) 与 read(new byte[0]) 均返回 -1（基类 InputStream 返回 0）**
- skip：-1/0 返回 0 不动位置、精确跳、越界夹紧到剩余、空源 0
- mark/reset：markSupported false；mark 为基类 no-op；reset 无需 mark 总是回到 0；close 为 no-op（继承基类），close 后仍可读
- available：满长、递减、skip 后、reset 后恢复满长、EOF 0

负例矩阵：ctor(null) NPE / read(b,1,8) IOOBE / read(b,-1,0) IOOBE / read(b,0,-1) IOOBE / read(null,0,1) NPE / read(null) NPE。

版本差异（8 vs 27）：双侧行为完全一致（deprecated 类未再改动）；27 侧另有白盒（protected buffer/pos/count 字段、@Deprecated、extends InputStream）。
