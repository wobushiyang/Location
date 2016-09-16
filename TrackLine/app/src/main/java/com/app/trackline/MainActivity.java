package com.app.trackline;

import android.app.Activity;
import android.content.DialogInterface;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity {

    public static final int PLAYBACK_OVER = 0x1;
    private MapView mapView;
    private BaiduMap baiduMap;
    private LocationClient mLocationClient = null;
    private MyLocationListener myLocationListener;
    private double currentLat, currentLng;//当前经纬度（纬度，经度）
    private String currentAddr;//当前所在的地址
    private DatabaseAdapter dbAdapter;
    private GeoCoder geoCoder;
    private int currentTrackLineID;//当前跟踪的线路ID

    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        mapView = (MapView) findViewById(R.id.bmapView);
        initBaiduMap();//初始化地图，显示我的当前位置
        dbAdapter = new DatabaseAdapter(this);

        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    //初始化百度地图
    private void initBaiduMap() {
        baiduMap = mapView.getMap();
        baiduMap.setMyLocationEnabled(true);//打开定位图层
        mLocationClient = new LocationClient(getApplicationContext());//声明LocationClient类
        myLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(myLocationListener);//注册监听函数
        initLocation();
        mLocationClient.start();
        mLocationClient.requestLocation();//发起定位请求 回到监听事件
        //地理编码 用于转换地理编码的监听器
        geoCoder = GeoCoder.newInstance();
        geoCoder.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
                                                   @Override
                                                   public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {
                                                   }

                                                   @Override
                                                   public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
                                                       if (reverseGeoCodeResult == null || reverseGeoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) { //没有检索到结果
                                                       } else { //获取地理编码结果
                                                           // System.out.println(reverseGeoCodeResult.getAddress());
                                                           currentAddr = reverseGeoCodeResult.getAddress();
                                                           //目的 更新线路的结束位置
                                                           dbAdapter.updateEndLoc(currentAddr, currentTrackLineID);
                                                       }
                                                   }
                                               }
        );
    }
/*设置定位参数包括：定位模式（高精度定位模式，低功耗定位模式和仅用设备定位模式），返回坐标类型，是否打开GPS，是否返回地址信息、位置语义化信息、POI信息等等。 LocationClientOption类，该类用来设置定位SDK的定位方式，e.g.：*/

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span = 5000;
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于5000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近” option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setNeedDeviceDirect(true);//返回的定位结果包含手机机头的方向
        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死 option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        mLocationClient.setLocOption(option);
    }

    boolean flag = true;


    //位置监听器
    class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {//接收位置信息的回调方法
            if (location != null && flag) {
                flag = false;
                currentLat = location.getLatitude();//当前的纬度
                currentLng = location.getLongitude();//当前的经度
                currentAddr = location.getAddrStr();//地址信息
                //System.out.println("currentAddr="+currentAddr);
                //构造我的当前位置信息
                MyLocationData.Builder builder = new MyLocationData.Builder();
                builder.latitude(location.getLatitude());//设置纬度
                builder.longitude(location.getLongitude());//设置经度
                builder.accuracy(location.getRadius());//设置精度（半径）
                builder.direction(location.getDirection());//设置方向
                builder.speed(location.getSpeed());//设置速度
                MyLocationData locationData = builder.build(); //把我的位置信息设置到地图上
                baiduMap.setMyLocationData(locationData); //配置我的位置 当前经纬度
                LatLng latLng = new LatLng(currentLat, currentLng); //（跟随态,允不允许显示方向,默认图标）
                baiduMap.setMyLocationConfigeration(new MyLocationConfiguration(MyLocationConfiguration.LocationMode.FOLLOWING, true, null));
                //设置我的位置为地图的中心点,16表示缩放级别 3--20
                baiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLngZoom(latLng, 16));
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }


    //功能菜单项
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mylocation:
                mylocation();//我的位置
                break;
            case R.id.start_track:
                startTrack();//开始跟踪
                break;
            case R.id.end_track:
                endTrack();//结束跟踪
                break;
            case R.id.track_back:
                trackBack();//跟踪回放
                break;
            default:
                break;
        }
        return true;
    }


    //我的位置
    private void mylocation() {
        Toast.makeText(MainActivity.this, "正在定位中...", Toast.LENGTH_SHORT).show();
        flag = true;
        baiduMap.clear();//清除地图上自定义的图层
        baiduMap.setMyLocationEnabled(true);
        mLocationClient.requestLocation();//发起定位请求
    }

    //开始跟踪功能
    private void startTrack() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("线路跟踪");
        builder.setCancelable(true);
        final View view = getLayoutInflater().inflate(R.layout.add_track_line_dialog, null);
        builder.setView(view);
        builder.setPositiveButton("添加", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText et_track_name = (EditText) view.findViewById(R.id.editText1_track_name);
                String trackName = et_track_name.getText().toString();
                //System.out.println(trackName);
                createTrack(trackName);//核心创建线路跟踪方法
                Toast.makeText(MainActivity.this, "跟踪开始...", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private boolean isTracking = false; //用于存储两个相邻的经纬度点，再画线

    private ArrayList<LatLng> list = new ArrayList<>(); //创建一条线路跟踪

    private void createTrack(String trackName) {
        Track track = new Track();
        track.setTrack_name(trackName);
        track.setCreate_date(DateUtil.toDate(new Date()));
        track.setStart_loc(currentAddr);
        currentTrackLineID = dbAdapter.addTrack(track);
        dbAdapter.addTrackDetail(currentTrackLineID, currentLat, currentLng);
        baiduMap.clear();
        addOverlay();
        list.add(new LatLng(currentLat, currentLng));
        isTracking = true;//线程模拟的标记
        //System.out.println(list);
        new Thread(new TrackThread()).start();
    }

    //在地图上添加图层(线路的每一个点)
    private void addOverlay() {
        //添加一个标注覆盖物在当前位置
        //构建Marker图标
        baiduMap.setMyLocationEnabled(false);//关闭定位图层
        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher); //构建MarkerOption，用于在地图上添加Marker
        LatLng latLng = new LatLng(currentLat, currentLng);
        OverlayOptions options = new MarkerOptions().position(latLng).icon(bitmap);
        //在地图上添加Marker，并显示
        baiduMap.addOverlay(options);
        //把当前添加的位置作为地图的中心点
        baiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLng(latLng));
    }

    //在两个点之间画线
    private void drawLine() {
        OverlayOptions lineOptions = new PolylineOptions().points(list).color(0xFFFF0000);
        baiduMap.addOverlay(lineOptions);
        list.remove(0);
    }

    //模拟跟踪的线程
    class TrackThread implements Runnable {
        @Override
        public void run() {
            while (isTracking) {
                getLocation();
                dbAdapter.addTrackDetail(currentTrackLineID, currentLat, currentLng);
                addOverlay();
                list.add(new LatLng(currentLat, currentLng));
                drawLine();
                //System.out.println("drawLine");
                try {
                    Thread.sleep(5000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //结束跟踪
    private void endTrack() {
        isTracking = false;//结束线程
        Toast.makeText(MainActivity.this, "跟踪结束...", Toast.LENGTH_SHORT).show();
        //转换地理编码 把最后的一个经纬度转换成地址 异步的过程
        geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(new LatLng(currentLat, currentLng)));
    }

    AlertDialog dialog = null;

    private void trackBack() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);//设立是否可撤销
        builder.setTitle("跟踪路线列表");
        View view = getLayoutInflater().inflate(R.layout.track_line_playback_dialog, null);
        ListView playbackListView = (ListView) view.findViewById(R.id.listView_play_back);

        ArrayList<HashMap<String, String>> data = new ArrayList<>();
        ArrayList<Track> tracks = dbAdapter.getTracks();
        HashMap<String, String> map = null;
        Track t = null;
        for (int i = 0; i < tracks.size(); i++) {
            map = new HashMap<>();
            t = tracks.get(i);
            map.put("id", String.valueOf(t.getId()));
            map.put("trackName_createDate", t.getTrack_name() + "--" + t.getCreate_date());
            map.put("startEndLoc", "从[" + t.getStart_loc() + "]到[" + t.getEnd_loc() + "]");
            data.add(map);
        }
        SimpleAdapter adapter = new SimpleAdapter(this, data, R.layout.playback_item, new String[]{"id", "trackName_createDate", "startEndLoc"}, new int[]{R.id.textView_id, R.id.textView_trackname, R.id.textView_startEndLoc});
        playbackListView.setAdapter(adapter);
        playbackListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView tv_id = (TextView) view.findViewById(R.id.textView_id);
                int _id = Integer.parseInt(tv_id.getText().toString());
                baiduMap.clear();
                new Thread(new TrackPlaybackThread(_id)).start();
                dialog.dismiss();
            }
        });
        builder.setView(view);
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                list.clear();
                dialog.dismiss();
            }
        });
        dialog = builder.create();
        dialog.show();
    }

    //模拟位置
    private void getLocation() {
        currentLat = currentLat + Math.random() / 1000;
        currentLng = currentLng + Math.random() / 1000;
    }

    //跟踪回放的线程
    class TrackPlaybackThread implements Runnable {
        private int id;

        public TrackPlaybackThread(int id) {
            this.id = id;
        }

        @Override
        public void run() {
            ArrayList<TrackDetail> trackDetails = dbAdapter.getTrackDetails(id);
            TrackDetail td = null;
            list.clear();
            currentLat = trackDetails.get(0).getLat();
            currentLng = trackDetails.get(0).getLng();
            list.add(new LatLng(currentLat, currentLng));
            addOverlay();
            for (int i = 1; i < trackDetails.size(); i++) {
                td = trackDetails.get(i);
                currentLat = td.getLat();
                currentLng = td.getLng();
                list.add(new LatLng(currentLat, currentLng));
                addOverlay();
                drawLine();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            handler.sendEmptyMessage(PLAYBACK_OVER);
        }

    }

    private Handler handler = new Handler() {
        //防止内存泄漏，使用弱关联 这里没用
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PLAYBACK_OVER:
                    Toast.makeText(MainActivity.this, "回放结束", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行MapView.onDestroy(),实现地图生命周期管理
        mapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }


}
