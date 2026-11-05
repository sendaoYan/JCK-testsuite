# java.lang.classfile.ClassFileVersion — 设计案

## 1. API 功能
`java.lang.classfile.ClassFileVersion`（interface，extends `ClassElement`）。静态工厂：`of(int major, int minor)`（唯一工厂，**无 latest()**）。实例方法：`majorVersion()`、`minorVersion()`。无 equals/hashCode 值语义（**身份相等**，实测）。

## 2. Java 用户如何使用
`cb.with(ClassFileVersion.of(52,0))` 或 `cb.withVersion(52,0)`（ClassBuilder default）注入版本；`cm.majorVersion()/minorVersion()` 回读。范围校验：两参数均按 u2（0..65535），越界抛 IAE。parse 侧：major ∈ [45..latestMajorVersion(=71)]，超出抛 IAE "Unsupported class file version: N"；minor 65535（预览位）可正常解析。

## 3. 参考来源
- JDK 27 javadoc + `javap java.lang.classfile.ClassFileVersion`。
- OpenJDK `test/jdk/jdk/classfile/`（ClassVersionTest 形态）。
- 本机实测（关键事实全部锁定）：of(-1,0)→IAE "major version out of range of u2: -1"；of(71,-5)→IAE "minor version out of range of u2: -5"；of(65536,0)→IAE；**两个 of(52,0) equals 为 false（无值相等）**；toString=="ClassFileVersion[majorVersion=52, minorVersion=0]"；**parse major 72/80/100/65535 → IAE "Unsupported class file version: N"**；minor 65535 parse 正常。

## 4. 测试用例设计 + 易错规范细节
- 白盒（原有）+ 黑盒（原有：of 回读、preview minor、build 注入往返）。
- `testFactoryMatrix`（深化）：of(71,0)/of(45,3)/of(0,0)/of(65535,65535) 回读；**u2 边界负例矩阵**（-1 major、-5 minor、65536 major → IAE 精确消息）；toString 精确串；**值语义实测**：a.equals(a) true、a.equals(of(52,0)) **false**（身份语义记录）、equals(null) false、hashCode 自洽；层次 instanceof ClassElement/ClassFileElement。
- `testBuildInjection`（深化）：with(ClassFileVersion(50,0)) 注入→re-parse 50.0 + elementList 含该元素；withVersion(45,3) 默认方法往返；withVersion(71,65535) 预览 minor 往返；**future 版本（72）build 成功但 parse IAE 精确消息**；默认 build 版本==latestMajorVersion==JAVA_27_VERSION==71 且 minor==latestMinorVersion==0；注入后 verify 仍干净。
- `testDefineClassLoop`（深化）：DynClassFileVersion+seq v52 类 → defineClass → go()==7（低版本类仍可定义运行）。
- 易错点：**不要断言两个 of(52,0) equals**（无值相等）；parse 支持上限是 ClassFile.latestMajorVersion() 而非 u2 上限 65535。
