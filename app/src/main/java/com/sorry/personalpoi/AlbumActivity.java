package com.sorry.personalpoi;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gyf.immersionbar.ImmersionBar;
import com.sorry.event.AlbumEventCode;
import com.sorry.event.AlbumMessageEvent;
import com.sorry.personalpoi.adapter.AlbumAdapter;
import com.sorry.personalpoi.bean.AlbumData;
import com.sorry.personalpoi.bean.MaterialBean;
import com.sorry.personalpoi.util.AlbumUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AlbumActivity extends Activity implements View.OnClickListener {
    private int REQUEST_CODE_ADD_DATA = 2;
    private int REQUEST_CODE_TAKE_PHOTO = 3;

    private RecyclerView feedList;
    private LinearLayout mSuspensionBar, lyNext;
    private TextView tvDate, tvNext, tvBack;

    private ContentResolver resolver;
    private LinearLayoutManager mLayoutManager;
    private AlbumUtils albumUtils;
    private AlbumAdapter adapter;

    private ArrayList<MaterialBean> materialBeans;
    private List<String> checkName;
    private List<AlbumData> photoDatas;
    private List<MaterialBean> checkList;

    private MediaScannerConnection msc;
    private int mCurrentPosition = 0;
    private int mSuspensionHeight;
    //是否选中并添加拍照返回图片
    private boolean isAdd = false;
    //视频已选中数量
    private int videoCheckCount = 0;
    //是否显示弹窗
    private String isShowDialog = AlbumConfig.YES_SHOW_DIALOG;
    private String cameraPath;
    private int flag;

    //RecyleView添加数据
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
                    Bundle bundle = msg.getData();
                    photoDatas = (ArrayList<AlbumData>) bundle.getParcelableArrayList("materialBeans").get(0);
                    materialBeans = (ArrayList<MaterialBean>) bundle.getParcelableArrayList("materialBeans").get(1);
                    if (isAdd && checkList.size() < AlbumConfig.ALBUM_CHECK_MAX) {
                        photoDatas.get(0).getList().get(1).setCheck(true);
                        checkList.add(materialBeans.get(0));
                        checkName.add(materialBeans.get(0).getName());
                        tvNext.setText(checkList.size() + "");
                        videoCheckCount = videoCheckCount + 1;
                    }
                    adapter = new AlbumAdapter(photoDatas, AlbumActivity.this);//将图片载入RecyleView
                    adapter.setRefresh(false, false);
                    feedList.setAdapter(adapter);
                    adapter.setCheckId(checkName);
                    if (isAdd) {
                        toGray(checkList.size(), videoCheckCount, 3);
                        isAdd = false;
                    }
                    if (photoDatas != null && photoDatas.size() > 0) {
                        tvDate.setText(photoDatas.get(mCurrentPosition).getDate());
                    }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //沉浸式状态栏
        ImmersionBar.with(this)
                .transparentStatusBar()
                .statusBarDarkFont(true)
                .init();
        setContentView(R.layout.activity_album);
        parseIntent();
        EventBus.getDefault().register(this);
        initData();
        getData();

    }


    //解析Intent跳转方式
    protected void parseIntent() {
        Intent intent = getIntent();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_TAKE_PHOTO && resultCode == RESULT_OK) {
            saveImageToGallery();
        }
        if (requestCode == REQUEST_CODE_ADD_DATA && resultCode == Activity.RESULT_OK) {
            //预览页返回数据
            checkList = data.getParcelableArrayListExtra("changedata");
            checkName = data.getStringArrayListExtra("checkname");
            videoCheckCount = data.getIntExtra("videocount", 0);
            adapter.setCheckId(checkName);
            toGray(checkList.size(), videoCheckCount, 3);
            tvNext.setText(checkList.size() + "");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
    //单击图片或视频进入预览界面
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageReceived(AlbumMessageEvent event) {
        if (event != null && event.getCode() == AlbumEventCode.ALBUM_CHECK) {
            MaterialBean checkBean = (MaterialBean) event.getData();
            if (!checkList.contains(checkBean)) {
                checkList.add(checkBean);
                if (checkBean.getType() == 2) {
                    videoCheckCount = videoCheckCount + 1;
                }
            }
            if (!checkName.contains(checkBean.getName())) {
                checkName.add(checkBean.getName());
            }
            //置灰
            toGray(checkList.size(), videoCheckCount, checkBean.getType());
            tvNext.setText(checkList.size() + "");
        }
        if (event != null && event.getCode() == AlbumEventCode.ALBUM_CHECK_NO) {
            MaterialBean checkBean = (MaterialBean) event.getData();
            if (checkList.contains(checkBean)) {
                checkList.remove(checkBean);
                if (checkBean.getType() == 2) {
                    videoCheckCount = videoCheckCount - 1;
                }
            }
            if (checkName.contains(checkBean.getName())) {
                checkName.remove(checkBean.getName());
            }
            //去除置灰
            clearGray(checkList.size(), videoCheckCount, checkBean.getType());
            tvNext.setText(checkList.size() + "");
        }
        if (event != null && event.getCode() == AlbumEventCode.ALBUM_SHOW_DETAIL) {
            MaterialBean checkBean = (MaterialBean) event.getData();
            if (checkBean != null && checkBean.getType() != 3) {
                int position = materialBeans.indexOf(checkBean);
                Intent intent = new Intent(this, AlbumPreviewActivity.class);
                intent.putParcelableArrayListExtra("checkBeans", (ArrayList<? extends Parcelable>) checkList);
                intent.putStringArrayListExtra("checkId", (ArrayList<String>) checkName);
                intent.putExtra("position", position);
                intent.putExtra("videocount", videoCheckCount);
                startActivityForResult(intent, REQUEST_CODE_ADD_DATA);
            } else {
                takePhoto();
            }

        }
    }

    /**
     * 清除置灰
     */
    private void clearGray(int size, int videoSize, int type) {
        if (type == 1) {
            if (size == AlbumConfig.ALBUM_CHECK_MAX - 1) {
                if (videoSize == AlbumConfig.ALBUM_CHECK_VIDEO_MAX) {
                    adapter.setRefresh(false, true);
                    adapter.notifyDataSetChanged();
                } else {
                    adapter.setRefresh(false, false);
                    adapter.notifyDataSetChanged();
                }
            }
        } else {
            if (videoSize == AlbumConfig.ALBUM_CHECK_VIDEO_MAX - 1) {
                adapter.setRefresh(false, false);
                adapter.notifyDataSetChanged();
            }
        }

    }

    /**
     * 置灰
     */
    private void toGray(int size, int videoSize, int type) {
        if (type == 1) {
            if (size == AlbumConfig.ALBUM_CHECK_MAX) {
                adapter.setRefresh(true, false);
                adapter.notifyDataSetChanged();
            }
        } else {
            if (size == AlbumConfig.ALBUM_CHECK_MAX) {
                adapter.setRefresh(true, false);
                adapter.notifyDataSetChanged();
            } else {
                if (videoSize == AlbumConfig.ALBUM_CHECK_VIDEO_MAX) {
                    adapter.setRefresh(false, true);
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }

    private void initData() {
        mSuspensionBar = (LinearLayout) findViewById(R.id.suspension_bar);
        feedList = (RecyclerView) findViewById(R.id.feed_list);
        lyNext = (LinearLayout) findViewById(R.id.ly_next);
        tvDate = (TextView) findViewById(R.id.tv_date);//当前位置的照片时间
        tvNext = (TextView) findViewById(R.id.tv_next);
        tvBack = (TextView) findViewById(R.id.tv_back);
        lyNext.setOnClickListener(this);
        tvBack.setOnClickListener(this);

        resolver = getContentResolver();
        checkList = new ArrayList<>();
        checkName = new ArrayList<>();

        mLayoutManager = new LinearLayoutManager(this);
        feedList.setLayoutManager(mLayoutManager);
        feedList.setHasFixedSize(true);
        ((SimpleItemAnimator) feedList.getItemAnimator())
                .setSupportsChangeAnimations(false);


        feedList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                mSuspensionHeight = mSuspensionBar.getHeight();
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                View view = mLayoutManager.findViewByPosition(mCurrentPosition + 1);
                if (view != null) {
                    if (view.getTop() <= mSuspensionHeight) {
                        mSuspensionBar.setY(-(mSuspensionHeight - view.getTop()));
                    } else {
                        mSuspensionBar.setY(0);
                    }
                }

                if (mCurrentPosition != mLayoutManager.findFirstVisibleItemPosition()) {
                    mCurrentPosition = mLayoutManager.findFirstVisibleItemPosition();
                    updateSuspensionBar();
                    mSuspensionBar.setY(0);
                }
            }
        });
    }
    //获取照片
    private void getData() {
        new Thread() {
            @Override
            public void run() {
                Message msg = new Message();
                albumUtils = new AlbumUtils(resolver);
                ArrayList<MaterialBean> beans = null;
                ArrayList Alist = new ArrayList();
                msg.what=1;
                beans = albumUtils.getSortData();//进行按照日期排序}
                Alist.add(albumUtils.getFormatData(beans, AlbumConfig.ADD_CAMERA));//添加进入相机位

                Bundle bundle = new Bundle();
                Alist.add(beans);
                bundle.putParcelableArrayList("materialBeans", Alist);
                msg.setData(bundle);
                handler.sendMessage(msg);
                Log.i("flagvalue",String.valueOf(msg.what));
            }
        }.start();
    }

    //更新顶级时间标题
    private void updateSuspensionBar() {
        if (photoDatas != null && photoDatas.size() > 0) {
            tvDate.setText(photoDatas.get(mCurrentPosition).getDate());
        }
    }

    //当前应用拍照保存位置
    private void takePhoto() {
        // 首先保存图片
        final File appDir = new File(Environment.getExternalStorageDirectory(), "PersonalPOI");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        cameraPath=file.getAbsolutePath();
        Uri imageUri = parUri(file);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
        startActivityForResult(intent, REQUEST_CODE_TAKE_PHOTO);
    }

    /**
     * 生成uri
     *
     * @param cameraFile
     * @return
     */
    private Uri parUri(File cameraFile) {
        Uri imageUri;
        String authority = getPackageName() + ".provider";
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            //通过FileProvider创建一个content类型的Uri
            imageUri = FileProvider.getUriForFile(this, authority, cameraFile);
        } else {
            imageUri = Uri.fromFile(cameraFile);
        }
        return imageUri;
    }

    //将相片保存到相册的接口
    private void saveImageToGallery() {
        // on take photo success
        final File file = new File(cameraPath);
        msc = new MediaScannerConnection(this, new MediaScannerConnection.MediaScannerConnectionClient() {
            @Override
            public void onMediaScannerConnected() {
                msc.scanFile(String.valueOf(file), null);
            }

            @Override
            public void onScanCompleted(String path, Uri uri) {
                msc.disconnect();
                isAdd = true;
                getData();
            }

        });
        msc.connect();
    }



    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.tv_back) {
            finish();
        } else if (i == R.id.ly_next) {
            if (checkList == null || checkList.size() == 0) {
                Toast.makeText(AlbumActivity.this, "请选择照片", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!TextUtils.isEmpty(isShowDialog) && isShowDialog.equals(AlbumConfig.NO_SHOW_DIALOG)) {
                setResult(Activity.RESULT_OK, new Intent().putParcelableArrayListExtra(AlbumConfig.RESULT_TIMEALBUM_DATA, (ArrayList<? extends Parcelable>) checkList).putExtra(AlbumConfig.RESULT_IS_SPLIT_TIME, false));
                finish();
                return;
            }

        }
    }
}
