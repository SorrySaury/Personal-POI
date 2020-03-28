package com.baidu.mapapi.clusterutil.projection;


import com.baidu.mapapi.model.LatLng;


//球面墨卡托坐标转换
public class SphericalMercatorProjection {
    final double mWorldWidth;//世界宽度，1就够用了

    public SphericalMercatorProjection(final double worldWidth) {
        mWorldWidth = worldWidth;
    }

    @SuppressWarnings("deprecation")
    public Point toPoint(final LatLng latLng) {
        final double x = latLng.longitude / 360 + .5;
        final double siny = Math.sin(Math.toRadians(latLng.latitude));
        final double y = 0.5 * Math.log((1 + siny) / (1 - siny)) / -(2 * Math.PI) + .5;

        return new Point(x * mWorldWidth, y * mWorldWidth);
    }

    public LatLng toLatLng(com.baidu.mapapi.clusterutil.projection.Point point) {
        final double x = point.x / mWorldWidth - 0.5;
        final double lng = x * 360;

        double y = .5 - (point.y / mWorldWidth);
        final double lat = 90 - Math.toDegrees(Math.atan(Math.exp(-y * 2 * Math.PI)) * 2);

        return new LatLng(lat, lng);
    }


//瓦片图划分规则
//
//百度地图SDK会根据不同的比例尺将地图划分成若干个瓦片，并且以中心点经纬度(0,0)开始计算瓦片，当地图显示缩放级别增大时，每一个瓦片被划分成4 个瓦片。
//
//如：地图级别为0时，只有1张瓦片地图级别为1时，会分成 1 * 4 = 4 张瓦片依次类推，
//地图级别为n时，总共划分的瓦片为：4的n次方，为了保证瓦片的显示效果，第n级的瓦片显示的地图level范围为[n - 0.5, n + 0.5)



}
