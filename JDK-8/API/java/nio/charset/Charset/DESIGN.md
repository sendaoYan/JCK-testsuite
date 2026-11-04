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
