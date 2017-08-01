# OSM-Wikidata
运行/src/main/java/OSM下的OSMInfoSave.java文件
调用OSMFileSave()函数，可以生成"OSMwithWiki_Area.osm"（Area表示的是某地区的OSM文件）,将OSM中有Wikidata链接的原始数据保存下来
此外调用readOSM()函数还生成一些其他的中间数据：
//(All)这是为了生成WKT格式做准备，所以只存了ID和地理信息
"NodePath_Area.txt";        格式：nodeID--nodeLON--nodeLAT
"WayPath_Area.txt";         格式：wayID--引用的node ID集合
"RelationPath_Area.txt";    格式：relationID--引用的node ID集合--引用的way ID集合--引用的relation ID集合
//(Wiki)这是因为存进nodelist、waylist、relationlist会堆栈溢出，所以就把需要的信息存进文件再读出来
"NodePath_Area(Wiki).txt";      格式：nodeID--链接的wikidata ID--Name--Name_zh--Name_en--nodeLon--nodeLat
"WayPath_Area(Wiki).txt";       格式：wayID--链接的wikidata ID--Name--Name_zh--Name_en--引用的node ID集合
"RelationPath_Area(Wiki).txt";  格式：relationID--链接的wikidata ID--Name--Name_zh--Name_en--引用的node ID集合--引用的way ID集合--引用的relation ID集合

运行/src/main/java/Wikidata下的HandleJSONFiles.java文件，可以得到Wikidata的entity中有OSM references的集合
顺便运行一下得到中间文件"F:\\Wiki-Name_EN&&ID.csv"，为了记录下实体的英文名（因为OSM中有的实体没有英文名）

由于Wikidata的数据稀疏，最后OSMwithWiki与WikiwithOSM数据union结果就是OSMwithWiki的结果：

运行/src/main/java/OSM_Wikidata下的OSM2WKT.java文件里的node2WKT(), way2WKT(), relation2WKT()函数，
可以将OSM中所有feature（node、way、relation）的地理数据转成WKT格式

运行/src/main/java/OSM_Wikidata下的RDF.java文件，调用RDF_OSM()、RDF_Wiki()函数，得到OSM数据和Wikidata数据的RDF格式（代码优化）
"RDF_OSM_Area.xml";
"RDF_Wiki_Area.xml";
利用writeRDF()、readRDF2model()、writeSelectedRDF()可以选择model输出的格式，比如RDF/XML、Turtle

运行/src/main/java/OSM_Wikidata下的Tag.java文件，得到OSM链接实体的Tag统计信息：
"F:\\TagQuantityNWR.txt";
"F:\\TagQuantityALL.txt";

另外，
运行/src/main/java/Wikidata下的HandleJSONFiles.java文件，还可以生成WikidatawithOSM.json，Wikidata中有OSM链接的原始数据保存下来
