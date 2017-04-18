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
