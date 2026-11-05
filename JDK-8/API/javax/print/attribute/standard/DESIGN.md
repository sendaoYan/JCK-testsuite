# javax.print.attribute.standard 包级 黑盒行为测试设计

## 1. API 功能
~70 个标准属性（枚举 51 + 整数 9 + 文本 5 + URI 1 + 日期 1 + SetOfInteger 4 + 尺寸/分辨率/Media 族 + 集合语义 3）。

## 2. 测试设计
每子目录一个测试 + DESIGN：枚举子类常量值/toString/getName/getCategory 全表、序列化 readResolve 单例、子类严格 equals、IAE 边界矩阵；整数子类上下界与越界精确消息；SetOfInteger 解析/归并/成员矩阵；MediaSize ISO/NA/JIS/Engineering/Other 精确毫米表；PrinterResolution 参数序与物理等值；JobStateReasons/PrinterStateReasons 集合/映射语义。

## 3. 版本差异
JDK-27 独有 OutputBin（27 侧单独断言）；Sides.DUPLEX==TWO_SIDED_LONG_EDGE 同对象；MediaSizeName/MediaSize 跨侧常量表一致。

## 4. 运行纪律
纯 headless；jtreg 双侧全绿。

