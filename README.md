# OSM-Wikidata
运行/src/main/java/OSM下的HandleOSMFiles.java文件，可以得到OSM的feature（node、way、relation）中有wikidata这个key的集合

运行/src/main/java/Wikidata下的HandleJSONFiles.java文件，可以得到Wikidata的entity中有OSM references的集合
顺便运行一下得到中间文件"F:\\Wiki-Name_EN&&ID.csv"，为了记录下实体的英文名（因为OSM中有的实体没有英文名）

运行/src/main/java下的HelloGeoKG.java文件，可以得到以上两个集合的union结果

运行之后发现，由于Wikidata的数据稀疏，最后与GeoData有关的数据union结果就是OSMwithWiki的结果：
"F:\\OSMwithWiki_Taiwan.csv";
"F:\\OSMwithWiki_China.csv";
格式是：node/way/relation, OSM ID, Name, Wikidata ID

运行/src/main/java/OSM_Wikidata下的OSM2WKT，java文件，可以得到将OSM中所有feature（node、way、relation）的地理数据转成WKT格式的结果
这只是为了记录，真正需要的是node2WKT(), way2WKT(), relation2WKT()函数，为生成RDF做准备

运行/src/main/java/OSM_Wikidata下的OSM.java文件，得到OSM数据的RDF格式：
"F:\\RDF_OSM_Taiwan.ttl";
"F:\\RDF_OSM_China.ttl";
运行/src/main/java/OSM_Wikidata下的Wikidata.java文件，得到OSM数据的RDF格式：
"F:\\RDF_Wiki_Taiwan.ttl";
"F:\\RDF_Wiki_China.ttl";

另外，
运行/src/main/java/Wikidata下的HandleJSONFiles.java文件，还可以生成WikidatawithOSM.json，Wikidata中有OSM链接的原始数据保存下来
运行/src/main/java/OSM下的OSMInfoSave.java文件，可以生成OSMwithWiki_Taiwan.osm、OSMwithWiki_China.osm，将OSM中有Wikidata链接的原始数据保存下来，此外还生成一些其他的中间数据