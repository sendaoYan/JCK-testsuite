# java.nio.charset.Charset 测试方案

## 1. API 功能
- 查找: forName(String)/isSupported(String)/availableCharsets()/defaultCharset() (JDK-8 无 forName(name,fallback))
- 身份: name()/aliases()/displayName()/displayName(Locale)/isRegistered()/equals/hashCode/compareTo/toString
- 关系: contains(Charset); 编解码: newEncoder()/newDecoder()/canEncode()/encode(String)/encode(CharBuffer)/decode(ByteBuffer)

## 2. Java 用户如何使用
- Charset.forName("UTF-8") (大小写/别名归一化); 组合链路 forName->encode(String)->decode(ByteBuffer) 还原
- contains(Charset) 判断字符集子集 (UTF-8 contains US-ASCII)

## 3. 开源代码实际用法调研
- jdk8u-dev Charset: 规范名大小写不敏感; availableCharsets 返回排序 SortedMap; 无 forName(name,fallback) (JDK-9+)

## 4. 测试用例设计 + 自动化实现
- 白盒: 抽象/extends Object/Comparable/工厂与身份方法存在; 黑盒: 查找(大小写/别名/isSupported/availableCharsets)、equals/hashCode/compareTo/toString/displayName/isRegistered、contains、ASCII/CJK/代理对往返、decode 尊重 remaining、异常(Unsupported/IllegalCharsetName)。实现于 CharsetTest.java

健壮性: 只测标准字符集 (UTF-8/US-ASCII/UTF-16/ISO-8859-1); 中文/emoji/é/€/� 用 Unicode 字面量
固定 (源码 ASCII 安全); defaultCharset 只断言非空不绑具体值; 无墙钟/随机/哈希顺序/全局状态/文件/网络/线程依赖.

### 第 4 部分增补（A3 批次：契约深化）

新增覆盖（双侧等价，JDK-27 侧另含既有 forName(name,fallback) 断言）:

1. **forName 别名矩阵**（表驱动 15 组）: "UTF8"/"utf-8"/"unicode-1-1-utf-8"→UTF-8、
   "ISO8859_1"/"latin1"/"ISO_8859_1"/"8859_1"→ISO-8859-1、"ascii"/"ANSI_X3.4-1968"/
   "ISO646-US"→US-ASCII、"UnicodeBigUnmarked"→UTF-16BE、"UnicodeLittleUnmarked"→UTF-16LE、
   "UnicodeBig"/"unicode"/"utf16"→UTF-16；每组断言规范名解析 + 与规范名查找返回**同一单例**。
2. **非法/未知名精确异常**: 空/空白/含空格/`-bad`/`a#b`/`a/b`/`a?`/`a%b` → IllegalCharsetNameException
   （getCharsetName 往返断言）；`isSupported` 对非法名**同样抛 ICE**（双侧实测，非返回 false）；
   合法但未知名 `a:b`（RFC-2278 允许 `:`）/`1abc`（数字开头合法）→ UnsupportedCharsetException、
   isSupported 返回 false；`forName(null)`/`isSupported(null)` → **裸 IllegalArgumentException**
   （精确类型断言，非 ICE 子类）。
3. **availableCharsets**: size>=150（本机 JDK-8=171/JDK-27=173，不绑精确值）；六规范名键存在且值
   == forName 单例；keySet 迭代**忽略大小写严格升序**（TreeMap 传 String.CASE_INSENSITIVE_ORDER，
   实测 "x-euc-jp-linux" 与 "x-EUC-TW" 共存即证明）；不存在仅大小写不同的两个键。
4. **contains 关系全表**（6×6 矩阵）: US-ASCII 仅含自身；ISO-8859-1 含 US-ASCII；UTF-8/UTF-16BE/
   UTF-16LE/UTF-16 对六者全 true（含 UTF-16BE∋UTF-16LE 实测）。
5. **便捷法默认 REPLACE**: Charset.decode 对畸形 UTF-8 {41,C3,28,42} → "A\ufffd(B"；
   Charset.encode(String) 对 US-ASCII 不可映射 → "a?b"；**手写 Encoder 小缓冲循环**（4 字节原子
   写单元，UTF-8 代理对=4B）逐段输出与便捷法字节一致（UTF-8/US-ASCII/UTF-16 三字符集）。
6. **字节布局**: UTF-8 "Aé中😀" = 41 C3A9 E4B8AD F09F9880（1+2+3+4=10 字节精确）；UTF-16BE/LE
   "A" = 00 41 / 41 00（无 BOM）；UTF-16（默认名）= FE FF 00 41（先发大端 BOM）。
7. **身份/顺序**: displayName(Locale)/displayName(null)==name；六常量 canEncode/isRegistered/
   toString==name；equals/hashCode 大小写与别名归一；compareTo 符号与规范名 String 比较
   一致；newDecoder/newEncoder 每次新对象且默认 REPORT；defaultCharset 非空、isSupported、
   == forName 单例。
