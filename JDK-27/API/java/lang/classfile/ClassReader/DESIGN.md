# java.lang.classfile.ClassReader — 设计案

## 1. API 功能
`java.lang.classfile.ClassReader`（interface，extends `constantpool.ConstantPool`）。实例方法：`customAttributes()`（Function&lt;Utf8Entry,AttributeMapper&gt;）、`flags()`、`thisClassEntry()`、`superclassEntry()`（Optional）、`classfileLength()`、`readEntry(int)`/`readEntry(int,Class)`/`readEntryOrNull(int)`、`readU1/readU2/readS1/readS2/readInt/readLong/readFloat/readDouble(int)`、`readBytes(int,int)`、`copyBytesTo(BufWriter,int,int)`。

## 2. Java 用户如何使用
**无公共工厂**：唯一途径是自定义 `AttributeMapper.readAttribute(AttributedElement, ClassReader, int)` 的回调参数（parse 期间触发）或在 `writeAttribute(BufWriter, ...)` 内使用此前捕获的实例。`pos` 参数是**属性数据起点**（跳过 name_index+length 头）；读 classfile 头部用绝对偏移（0=magic、4=minor、6=major、8=cp count）。`readEntry(pos)` 在字节偏移 pos 处读 u2 当作池下标再解析；`readEntryOrNull(pos)` 仅当该 u2 为 0 时返回 null，坏下标仍抛 ConstantPoolException。

## 3. 参考来源
- JDK 27 javadoc + `javap java.lang.classfile.ClassReader`。
- OpenJDK `test/jdk/jdk/classfile/`（OptionsTest 的 StrangeAttribute 模板）。
- 本机实测（全部锁定）：classfileLength==bytes.length；readInt(0)==0xCAFEBABE、readU2(4)==0、readU2(6)==71、readS1(0)==-54、readS2(0)==-13570、readLong(0)==0xCAFEBABE00000047L；readInt(-1)/readInt(len)/readU1(len)→IAE "Reading beyond classfile bounds"；readEntry(0)→ConstantPoolException "Bad CP index: 51966"（0xCAFE）；readEntryOrNull(u2==0 的偏移)→null；**readBytes(0, len+1) 不抛**（宽松，勿断言异常）；copyBytesTo 只能在 writeAttribute 里拿到 BufWriter。

## 4. 测试用例设计 + 易错规范细节
- 白盒（原有）+ 黑盒（原有：mapper 捕获 + 头部读取基础断言）。
- `testCapturedReader`（深化）：自造 mapper（payload 0x12 0x34）→ **pos 语义锁定**（readU1(pos)==0x12、readU1(pos+1)==0x34）；classfileLength==bytes.length；thisClassEntry 内名/asSymbol；superclassEntry==java/lang/Object；flags==ACC_PUBLIC|ACC_SUPER；`ConstantPool` 继承面（size()>0、entryByIndex(thisClassEntry().index()) **身份** 返回同一 entry）；绝对偏移读全家（magic/minor/major/cp count/readS1/readS2/readLong/readFloat 位精确）；readBytes 两段；`customAttributes()` 函数对已知名返回注册 mapper（==）、未知名 null；**越界负例矩阵**（readInt(-1)/readInt(len)/readU1(len)/readU2(len-1) → IAE；readEntry(0)→ConstantPoolException "Bad CP index"；readEntry(len)→IAE；readEntryOrNull(u2==0 偏移)==null、readEntryOrNull(0) 仍抛）。
- `testCopyBytesTo`（深化）：writeAttribute 回调内 `cr.copyBytesTo(bw, 0, 4)` 拷贝 magic → build→re-parse 闭环。
- `testUnregisteredParse`（深化）：未注册 mapper 的 parse **不抛**（UnknownAttribute 保留于 attributes()）、verify 干净。
- 易错点：**pos 是属性数据起点不是属性头**；readU2(6) 是 major（minor 在 4）；readS1/readS2 是有符号；readEntry 的参数是字节偏移不是池下标。
