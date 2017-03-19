师兄好!

这是用于整理.osm数据的初步测试

我利用第三方开源包cpdetector获取文件编码格式，需要引入antlr.jar、chardet.jar包，maven仓库里没有，需要手动配置

cambodia-latest.osm是测试文件。源文件下载于http://download.geofabrik.de/asia.html，列表中第4个Cambodia，[.osm.bz2]格式的。解压后200+M

运行HandleFile文件，改一下main函数中的filePath就行