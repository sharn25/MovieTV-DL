package com.sb.movietvdl.ui.Activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sb.movietvdl.Adapter.LBRecyclerViewAdapter;
import com.sb.movietvdl.R;
import com.sb.movietvdl.Utils.CustomDialog;
import com.sb.movietvdl.Utils.LogUtil;
import com.sb.movietvdl.Utils.StaticResource;
import com.sb.movietvdl.config.MConfig;
import com.sb.movietvdl.helper.Fetcher;
import com.sb.movietvdl.source.LinkButton;
import com.sb.movietvdl.source.MovieTV;
import com.squareup.picasso.Picasso;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class DetailsActivity extends AppCompatActivity implements LBRecyclerViewAdapter.OnLBClickListener {
    //String
    public String title, url, imgicon, des, sty, epnumber, hdtype, season;
    //String array
    public String[] epname, epurl;
    //ListArray
    public List<LinkButton> LinkButttonList;
    //Elements
    private ProgressBar pgDetail, pgEpDown;
    private NestedScrollView svDetails;
    private ImageView ivImgBg, ivImg, ivImgBgHelper;
    private TextView tvTitle, tvDes, tvSty;
    private RecyclerView rvEp;
    private LinearLayout llEp;
    private LBRecyclerViewAdapter lbAdapter;
    private Switch swStmDwn;
    private static View view;
    //boolean
    private boolean isDarkTheme;
    public boolean isListUpdated;
    public boolean isEpLink;
    public boolean isAlreadyAsked;
    //Handler
    public Handler mHandler;
    public Message msg;
    //Constants
    public static final String TAG = "DetailsActivity";
    public static final int POP_INFO=10;
    private static final int DOWNL_ALL_UPDATE=20;

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        LogUtil.l(TAG+"_onSaveInstanceState", "Saveing activity state.",true);
        savedInstanceState.putString("title",title);
        savedInstanceState.putString("url",url);
        savedInstanceState.putString("imgicon",imgicon);
        savedInstanceState.putString("des",des);
        savedInstanceState.putString("sty",sty);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        this.finish();
    }

    private void setHandler(){
        mHandler=new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.arg1){
                    case POP_INFO:
                        String[] ary = (String[]) msg.obj;
                        CustomDialog.popinfo(DetailsActivity.this,ary[0]);
                        break;
                }
                return false;
            }
        });
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setTheme(StaticResource.CUR_THEME);
        setContentView(R.layout.activity_details);
        view = findViewById(R.id.activity_details);
        //init recived
        initRecived(savedInstanceState);
        setHandler();
        //Elements
        pgDetail = (ProgressBar) findViewById(R.id.pg_detail);
        svDetails = (NestedScrollView) findViewById(R.id.sv_details);
        ivImgBg = (ImageView) findViewById(R.id.iv_img_bg);
        ivImg = (ImageView) findViewById(R.id.iv_img);
        ivImgBgHelper = (ImageView) findViewById(R.id.iv_img_bg_helper);
        tvTitle = (TextView) findViewById(R.id.tv_title);
        tvDes = (TextView) findViewById(R.id.tv_des);
        tvSty = (TextView) findViewById(R.id.tv_sty);
        rvEp = (RecyclerView) findViewById(R.id.rv_ep);
        llEp = (LinearLayout) findViewById(R.id.ll_ep);
        pgEpDown = (ProgressBar) findViewById(R.id.pg_ep_down);
        swStmDwn = (Switch) findViewById(R.id.sw_stm_dwn);
        swStmDwn.setOnClickListener(tbtnStmDwnListener);
        setInitElements();
        getData();
        LogUtil.l(TAG+"onCreate","Operation Completed. URL: " + url,true);
    }

    private Switch.OnClickListener tbtnStmDwnListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(swStmDwn.isChecked()){
                MConfig.getConfig().isStreamMode=true;
            }else{
                MConfig.getConfig().isStreamMode=false;
            }
            LogUtil.l(TAG+"_tbtnStmDwnListener","Toggle button pressed stream " + MConfig.getConfig().isStreamMode,true);
        }
    };

    public List<LinkButton> get_LinkButttonList(){
        if(LinkButttonList==null){
            LinkButttonList = new ArrayList<>();
        }
        return LinkButttonList;
    }
    public void set_LinkButttonList(List<LinkButton> l){
        if(LinkButttonList==null){
            LinkButttonList = new ArrayList<>();
        }
        LinkButttonList = l;
    }

    private void initRecived(Bundle savedInstanceState){
        if(savedInstanceState!=null){
            LogUtil.l(TAG+"savedInstanceState", "Saved activity resumed.",true);
            title = savedInstanceState.getString("title");
            des = savedInstanceState.getString("des");
            url = savedInstanceState.getString("url");
            imgicon = savedInstanceState.getString("imgicon");
            sty = savedInstanceState.getString("sty");
        }else{
            LogUtil.l(TAG+"savedInstanceState", "no saved instance found. creating new activity.",true);
            Bundle recBundle = getIntent().getExtras();
            title = recBundle.getString("title");
            url = recBundle.getString("url");
            imgicon = recBundle.getString("imgicon");
        }
        isDarkTheme = MConfig.getConfig().isDarkTheme;
    }

    private void getData(){
        pgDetail.setVisibility(View.VISIBLE);
        llEp.setVisibility(View.GONE);
        Thread t = new Thread(){
            public void run(){
                //Background Run
                Fetcher.getConfig().mphelper(url,MConfig.getConfig().source,DetailsActivity.this);

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        setPostElements();
                        if(isListUpdated) {
                            pgDetail.setVisibility(View.GONE);
                            llEp.setVisibility(View.VISIBLE);
                        }

                        LogUtil.l(TAG+"_getData()", "Thread Completed for getting Data",true);
                    }
                });
            }
        };
        t.start();
    }

    private void setInitElements(){
        tvTitle.setText(title);
        int c=0;
        c = ContextCompat.getColor(DetailsActivity.this,R.color.MT_dtl);
        ivImgBgHelper.setBackgroundColor(c);
        try{
            Picasso.with(this)
                    .load(imgicon)
                    .into(ivImgBg);
            Picasso.with(this)
                    .load(imgicon)
                    .into(ivImg);
        }catch(Exception e){
            LogUtil.e(TAG,"Image loading fail: " + imgicon,true);
        }

        if(MConfig.getConfig().isStreamMode){
            swStmDwn.setChecked(true);
        }else {
            swStmDwn.setChecked(false);
        }
        epnumber="";
        hdtype="";
    }

    private void setPostElements(){
        tvTitle.setText(title);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                tvDes.setText(Html.fromHtml(des, Html.FROM_HTML_MODE_COMPACT));
            } else {
                tvDes.setText(Html.fromHtml(des));
            }
        }catch(Exception e){
            tvDes.setText("No description found...");
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                tvSty.setText(Html.fromHtml(sty, Html.FROM_HTML_MODE_COMPACT));
            } else {
                tvSty.setText(Html.fromHtml(sty));
            }
        }catch(Exception e){
            tvDes.setText("No story info...");
        }
        UpdateRecylerView();
    }
    //List updater
    private void UpdateRecylerView() {
        try {
            if (LinkButttonList != null) {
                if (lbAdapter == null) {
                    LogUtil.l(TAG + "_UpdateRecyclerView", "lbAdapter is null. creating new.", true);
                    lbAdapter = new LBRecyclerViewAdapter(this, LinkButttonList, this);
                    rvEp.setLayoutManager(new GridLayoutManager(this, 1));
                    rvEp.setAdapter(lbAdapter);
                } else {
                    LogUtil.l(TAG + "_UpdateRecyclerView", "lbAdapter already exist. Updateing..", true);
                    lbAdapter.notifyDataSetChanged();
                }
                isListUpdated = true;
            } else {
                throw new java.lang.NullPointerException();
            }
        } catch (Exception e) {
            isListUpdated = false;
           //tv_on_info.setVisibility(View.VISIBLE);
        }
    }

    //popOptions
    public void popPlayerOptions(Context context, String message, String s){
        final String epnamee = s;
        LayoutInflater popPLayerOption = LayoutInflater.from(context);
        View alertLayout = popPLayerOption.inflate(R.layout.player_select_dialog, null);
        final RadioGroup rg_player_select = (RadioGroup) alertLayout.findViewById(R.id.rg_player_select);
        final RadioButton rb_Iplayer = (RadioButton) alertLayout.findViewById(R.id.rb_Iplayer);
        final RadioButton rb_Eplyer = ( RadioButton) alertLayout.findViewById(R.id.rb_Eplayer);
        final CheckBox cb_player_default = (CheckBox) alertLayout.findViewById(R.id.cb_player_default);
        rb_Iplayer.setSelected(true);
        new AlertDialog.Builder(context)
                .setTitle(message)
                .setView(alertLayout)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("Start", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(rb_Iplayer.isChecked()){
                            MConfig.getConfig().isEPlayer=false;
                        }else if(rb_Eplyer.isChecked()){
                            MConfig.getConfig().isEPlayer=true;
                        }else {
                            CustomDialog.popSnackinfo(view,"Player not selected.");
                            return;
                        }
                        if(cb_player_default.isChecked()){
                            MConfig.getConfig().isPlayerAsk=false;
                        }else{
                            MConfig.getConfig().isPlayerAsk=true;
                        }
                        isAlreadyAsked=true;
                        LogUtil.l(TAG+"_popalert", "isEPlayer: " + MConfig.getConfig().isEPlayer,true);
                        LogUtil.l(TAG+"_popalert", "isPLayerAsk: " + MConfig.getConfig().isPlayerAsk,true);
                        MConfig.getConfig().save();
                        startOpeation(epnamee);
                    }
                }).create().show();
    }

    @Override
    public void OnLBClick(int position) {
        LogUtil.l(TAG+"_OnLBClick","Name: " + LinkButttonList.get(position).getEpname()+  " Click: " + LinkButttonList.get(position).getEpurl(),true);
        String link = LinkButttonList.get(position).getEpurl();
        String name = LinkButttonList.get(position).getEpname();
        if(name.contains("Season")){
            season = name.substring(name.indexOf("Season"));
        }
        if(name.contains("Episode") || name.contains("Epi")){
            epnumber = LinkButttonList.get(position).getEpname();
        }else{
            hdtype = LinkButttonList.get(position).getEpname();
        }

        if(MConfig.getConfig().isPlayerAsk && MConfig.getConfig().isStreamMode && !isAlreadyAsked){
            popPlayerOptions(DetailsActivity.this, "Select Player to Start with",link);
        }else{
            startOpeation(link);
        }
    }

    private void startOpeation(final String link){
        pgEpDown.setVisibility(View.VISIBLE);
        rvEp.setVisibility(View.GONE);
        Thread t = new Thread(){
            public void run(){
                //Background Run

                Fetcher.getConfig().downloadhelper(link,MConfig.getConfig().source,DetailsActivity.this);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(isListUpdated){
                            isEpLink=true;
                            UpdateRecylerView();
                        }
                        pgEpDown.setVisibility(View.GONE);
                        rvEp.setVisibility(View.VISIBLE);

                        LogUtil.l(TAG+"_getData()", "Thread Completed for getting Data",true);
                    }
                });
            }
        };
        t.start();
    }


}

