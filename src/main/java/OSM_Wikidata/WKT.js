/**
 * Created by SmallApple on 2017/4/17.
 */
//1、空间点转换成WKT：
//x为经度lng，y为纬度lat
var poi = new OpenLayers.Geometry.Point(x, y);
var wkt = new OpenLayers.Format.GeoJSON(poi);

//2、空间线转换成wkt
//objectArray存放空间点集的数组
var x, y; //声明点的经纬度坐标变量
var points = []; //声明一个点数组
var wkt = null;
for (var i = 0; i < objectArray.length; i++) {
    x = objectArray[i].lng; //经度
    y = objectArray[i].lat; //纬度
    points.push(new OpenLayers.Geometry.Point(x, y));
}
//线对象
var ring = new OpenLayers.Geometry.LineString(points);
//线的wkt字符串
wkt = new OpenLayers.Format.WKT().extractGeometry(ring);

//3、空间面对像转换成wkt
for (var i = 0; i < objectArray.length; i++) {
    x = objectArray[i].lng;
    y = objectArray[i].lat;
    points.push(new OpenLayers.Geometry.Point(x, y));
}
var ring = new OpenLayers.Geometry.LinearRing(points);
var poly = new OpenLayers.Geometry.Polygon([ ring ]);
wkt = new OpenLayers.Format.WKT().extractGeometry(poly);

//4、将wkt形式的数据转换成点集
var geometry = OpenLayers.Geometry.fromWKT(wkt);
var points = geometry.getVertices();
// points 是一个点数组，如果为点，其长度为1，如果为线和面，长度大于1，后面可以自己看情况处理
for (var i = 1; i < points.length + 1; i++) {
    var obj = new Object();
    var lng = points[i - 1].x; //经度
    var lat = points[i - 1].y;// 纬度
}

/**
POINT(6 10)
LINESTRING(3 4,10 50,20 25)
POLYGON((1 1,5 1,5 5,1 5,1 1),(2 2,2 3,3 3,3 2,2 2))
MULTIPOINT(3.5 5.6, 4.8 10.5)
MULTILINESTRING((3 4,10 50,20 25),(-5 -8,-10 -8,-15 -4))
MULTIPOLYGON(((1 1,5 1,5 5,1 5,1 1),(2 2,2 3,3 3,3 2,2 2)),((6 3,9 2,9 4,6 3)))
GEOMETRYCOLLECTION(POINT(4 6),LINESTRING(4 6,7 10))
POINT ZM (1 1 5 60)
POINT M (1 1 80)
POINT EMPTY
MULTIPOLYGON EMPTY
 */