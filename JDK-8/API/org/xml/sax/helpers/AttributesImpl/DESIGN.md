# AttributesImpl — 设计案

## 1. API 功能
SAX2 Attributes 可变实现：add/set/remove/clear/拷贝构造，全字段可改。

## 2. Java 用户如何使用
过滤/增删属性后再转发（filter 中间件标配）。

## 3. 开源代码实际用法调研
SAX 2.0.2 helpers javadoc。

## 4. 测试用例设计 + 自动化实现
黑盒：全访问器矩阵；重复 qName 共存而按名取首个；拷贝构造快照独立；setValue/setType/setURI/setLocalName/setQName 原位改；setAttribute 原位替换；越界读 null vs 越界写/删 ArrayIndexOutOfBoundsException 双轨；JDK quirk：null-URI 条目使 getIndex(uri,local) 抛 NPE（两侧一致）但按 qName 仍可查。
