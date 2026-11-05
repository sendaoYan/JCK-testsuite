# javax.print.DocFlavor 黑盒行为测试设计

## 1. API 功能
DocFlavor(mimeType, representationClassName)：MIME 解析归一 + rep class 名；7 个嵌套常量族。

## 2. Java 用户如何使用
匹配打印数据格式；equals 以归一化 MIME + rep class 为准。

## 3. 开源代码实际用法调研
实现权威：JDK 源码（JDK-8: jdk8u jdk/src/share/classes/javax/print；JDK-27: jdk/src/java.desktop/share/classes/javax/print）。行为断言均以双 JDK 实测探针为基准（javap + 运行时探针输出 diff）。

## 4. 测试用例设计 + 自动化实现
归一：charset=us-ascii 输出 charset="us-ascii"（参数加引号、小写归一、多余空白折叠）；TEXT/PLAIN 大小写不敏感 equals + hashCode 一致；额外参数 x=y 归一为 x="y" 且使 flavor 不等；charset 参数读取小写化；getParameter(未知)=null。
- 常量表：SERVICE_FORMATTED 三常量 rep class；AUTOSENSE/PNG/GIF/PDF/POSTSCRIPT/TEXT_HTML mime 精确串；BYTE_ARRAY/INPUT_STREAM/URL/CHAR_ARRAY/READER/STRING 各自 rep class（[B / java.io.InputStream / java.net.URL / [C / java.io.Reader / java.lang.String）。
- unicode 别名不归一（charset="unicode" 保持原样），预定义常量实际用 charset="utf-16"，故 unicode 构造 != 预定义。
- HOST 常量镜像宿主 file.encoding（本机 utf-8，TEXT_PLAIN_HOST == TEXT_PLAIN_UTF_8 且 mime 为 charset="utf-8"）。
- 序列化 round-trip 相等。

## 5. 负例与精确异常
NPE：new DocFlavor(null, cls) / (mime, null) 均 NPE（消息 null）。IAE：无 "/" 的 "nonsense" → IAE（消息 null）；"" → IAE；"/" → IAE。未知 rep class 名被接受（只存字符串）。

## 6. 闭环与版本差异
mime 归一/equals/hashCode 闭环；序列化闭环。无版本差异（双侧逐字节一致）。

## 7. 运行纪律
纯 headless（env -u DISPLAY），不加 @key；不真用打印机/声卡。jtreg 双侧（JDK-8 需 JTREG_JAVA=jdk-17）独立 workDir/reportDir 全绿后提交。
