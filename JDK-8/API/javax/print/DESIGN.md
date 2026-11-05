# javax.print 包级 黑盒行为测试设计

## 1. API 功能
打印核心：DocFlavor(+7 嵌套族)/Doc/MultiDoc/SimpleDoc、PrintService 家族(lookup/stream/job/multidoc)、ServiceUI(Factory)、PrintException+3 子接口、attribute/ 属性系统、event/ 事件体系。

## 2. 测试设计
各子目录均有独立 DESIGN.md（常量表/负例矩阵/闭环）。核心闭环：PS 流输出 %!PS 端到端、registerServiceProvider 注册发现、DocFlavor mime 归一 equals、SimpleDoc 读写与视图语义、EnumSyntax 序列化单例、SetOfIntegerSyntax 解析。

## 3. 版本差异
registerService(null)：JDK-8 true+泄漏 null 元素（过滤查询 NPE），JDK-27 false 干净；registerServiceProvider(null) 双侧 true。AttributeException 双方法签名（Class[]/Attribute[]）。URIException 常量 1/2/-1。JDK-27 独有 OutputBin/DialogOwner。

## 4. 运行纪律
纯 headless（env -u DISPLAY）；不真用打印机（无打印机时断言空数组/null/HeadlessException 无服务契约）；jtreg 双侧全绿。
