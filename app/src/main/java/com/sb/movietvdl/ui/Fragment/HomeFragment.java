package com.sb.movietvdl.ui.Fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sb.movietvdl.Adapter.MTRecyclerViewAdapter;
import com.sb.movietvdl.R;
import com.sb.movietvdl.Utils.CustomDialog;
import com.sb.movietvdl.Utils.LogUtil;
import com.sb.movietvdl.config.MConfig;
import com.sb.movietvdl.helper.Fetcher;
import com.sb.movietvdl.source.MovieTV;
import com.sb.movietvdl.ui.Activities.DetailsActivity;
import com.sb.movietvdl.ui.Activities.MainActivity;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.List;

    /*
    * Created by Sharn25
    * dated 12-06-2020
    */

public class HomeFragment extends Fragment implements MTRecyclerViewAdapter.OnRVClickListener {
    //ListArray
    private List<MovieTV> movieTVList;
    //boolean
    private boolean isLoading;
    private boolean isListUpdated;
    private boolean searchmode;
    private boolean isFirstloaded;
    private boolean isNullInserted;
    private boolean isDataEndReached;
    private boolean isScrolledAction;
    //int
    private int page;
    private int nullPos;
    private int oldSource;
    //String
    private String searchtxt, oldurl, newurl;
    //Handler
    public Handler mHandler;
    public Message msg;
    //Elements
    private SwipeRefreshLayout srl_items;
    private RecyclerView rv_item;
    private ProgressBar pg_load;
    private TextView tv_on_info;
    private MTRecyclerViewAdapter rv_adapter;
    //Constants
    public static final int POP_INFO = 10;
    private static final String TAG = "HomeFragment";

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(!hidden){
            LogUtil.l(TAG + "_onHiddenChanged", "Showing fragment", true);
            if (oldSource!=MConfig.getConfig().source && !isLoading) {
                oldSource = MConfig.getConfig().source;
                searchmode=false;
                isFirstloaded=false;
                if(movieTVList!=null) {
                    movieTVList.clear();
                }
                page = 1;
                create_list(page);
            }
        }else{
            LogUtil.l(TAG + "_onHiddenChanged", "Hideing fragment", true);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.framgment_home, container, false);
        //init
        init();
        //init elements
        srl_items = (SwipeRefreshLayout) view.findViewById(R.id.srl_items);
        rv_item = (RecyclerView) view.findViewById(R.id.rv_items);
        pg_load = (ProgressBar) view.findViewById(R.id.pb_load);
        tv_on_info = (TextView) view.findViewById(R.id.tv_on_info);
        //Data_creator
        isFirstloaded=false;
        page = 1;
        create_list(page);
        //RefreashLayout
        srl_items.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!isLoading) {
                    LogUtil.l(TAG + "_onCreate", "Swipe Refreshing Layout.", true);
                    searchmode=false;
                    isFirstloaded=false;
                    if(movieTVList!=null) {
                        movieTVList.clear();
                    }
                    page = 1;
                    create_list(page);
                }
            }
        });
        FloatingActionButton search =(FloatingActionButton) view.findViewById(R.id.fb_search);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Show_search_dialog();
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void init() {
        oldSource = MConfig.getConfig().source;
        setHandler();
        //CustomDialog.popinfo(getActivity(), "Completed init.");
    }

    //Search
    private void Show_search_dialog(){
        LogUtil.l(TAG+"_Show_search_dialog","Search Pressed",true);
        if(isLoading){
            return;
        }
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.dialog_search, null);
        final EditText eb_search = alertLayout.findViewById(R.id.eb_search);
        AlertDialog.Builder downall_dialog = new AlertDialog.Builder(getActivity());
        downall_dialog.setTitle("Search");
        downall_dialog.setView(alertLayout);
        downall_dialog.setNegativeButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        downall_dialog.setPositiveButton("Search", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String txt_search = String.valueOf(eb_search.getText());
                LogUtil.l(TAG+"_Show_search_dialog","Entered Anime Name: " + txt_search,true);
                action_search(txt_search);
            }
        });
        AlertDialog dialog = downall_dialog.create();
        dialog.show();
    }
    private void action_search(String t){
        if(t.length()<3){
            CustomDialog.popinfo(getActivity(),"Please Enter more then 3 charcters.");
            LogUtil.e(TAG+"_action_serach","Please Enter more then 3 charcters.",true);
            return;
        }

        searchmode=true;
        searchtxt = t;
        isFirstloaded=false;
        if(movieTVList!=null) {
            movieTVList.clear();
        }
        page = 1;
        create_list(page);
    }
    //movieTVarray init
    private List<MovieTV> get_movieTVarray(){
        if(movieTVList==null){
            movieTVList = new ArrayList<>();
        }
        return movieTVList;
    }

    //List updater
    private void UpdateRecylerView() {
        try {
            if (movieTVList != null) {
                if (rv_adapter == null) {
                    LogUtil.l(TAG + "UpdateRecyclerView", "rv_adapter is null. creating new.", true);
                    rv_adapter = new MTRecyclerViewAdapter(this.getActivity(), movieTVList, this);
                    rv_item.setLayoutManager(new GridLayoutManager(this.getActivity(), 3));
                    rv_item.setAdapter(rv_adapter);
                    add_scrollListener();
                } else {
                        LogUtil.l(TAG + "UpdateRecyclerView", "rv_adapter already exist. Updateing..", true);
                        rv_adapter.notifyDataSetChanged();
                }
                isListUpdated = true;
            } else {
                throw new java.lang.NullPointerException();
            }
        } catch (Exception e) {
            isListUpdated = false;
            tv_on_info.setVisibility(View.VISIBLE);

        }
    }
    private void add_scrollListener(){
        rv_item.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                GridLayoutManager linearLayoutManager = (GridLayoutManager) recyclerView.getLayoutManager();

                if (!isLoading) {
                    if(!isDataEndReached) {
                        if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == movieTVList.size() - 1) {

                            //bottom of list!
                            //loadMore();
                            isScrolledAction =true;
                            isLoading = true;
                            add_more();
                            LogUtil.l(TAG + "_rv_scrolListener", "End of scroll reached adding new items.", true);


                        }
                    }else{
                        LogUtil.l(TAG + "_rv_scrolListener", "Oldurl: " + oldurl + " != " + newurl + " NewURL", true);
                    }
                }
            }
        });

    }

    //List creator Area
    private String get_url(int i){
        String url="";
        if(searchmode){
            url = Fetcher.getConfig().getsrchbox(searchtxt,MConfig.getConfig().source,i) +"/";
        }else{
            url = Fetcher.getConfig().gethomeurrl(MConfig.getConfig().source,i) + "/";
        }
        return url;
    }

    private void add_more(){
        movieTVList.add(null);
        nullPos = movieTVList.size();
        isNullInserted=true;
        rv_item.post(new Runnable() {
            public void run() {
                rv_adapter.notifyItemInserted(movieTVList.size() - 1);
            }
        });
        //rv_adapter.notifyDataSetChanged();
        page = page + 1;
        LogUtil.l(TAG+"_add_more","Page: " + page,true);
        create_list(page);
    }
    private void create_list(int i){
        final int r = i;
        oldurl="";
        newurl="";
        LogUtil.l(TAG+"_create_list()", "Creating List",true);
        isLoading = true;
        isListUpdated=false;
        isDataEndReached=false;
        if(!isFirstloaded){
        tv_on_info.setVisibility(View.GONE);
        rv_item.setVisibility(View.GONE);
        pg_load.setVisibility(View.VISIBLE);
        }
        Thread t = new Thread(){
            public void run(){
                createMTVlist(get_url(r));
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(isNullInserted){
                            isNullInserted=false;
                            rv_item.post(new Runnable() {
                                public void run() {
                                    movieTVList.remove(nullPos - 1);
                                    rv_adapter.notifyItemRemoved(nullPos);
                                }
                            });
                        }
                        UpdateRecylerView();
                        if(srl_items.isRefreshing()){
                            srl_items.setRefreshing(false);
                        }
                        if(isListUpdated && !isFirstloaded) {
                            isFirstloaded=true;
                            pg_load.setVisibility(View.GONE);
                            rv_item.setVisibility(View.VISIBLE);
                        }
                        isLoading=false;
                        isScrolledAction=false;
                        LogUtil.l(TAG+"_create_list()", "Thread Completed for creating list.",true);
                    }
                });
            }
        };
        t.start();

    }

    private void createMTVlist(String url){
        LogUtil.l(TAG+"_createMTVlist(url)","parsing data from internet...... " + url,true);
        Document doc=parsehtml(url);
        if(doc!=null){
            LogUtil.l(TAG+"_createMTVlist(url)","Fetching html completed. Intiating list creating......",true);
            movieTVList = Fetcher.getConfig().getdata(doc, get_movieTVarray(), MConfig.getConfig().source, this);
            LogUtil.l(TAG+"_createMTVlist(url)","Fetching getdata completed. showing view......",true);
        }else{
            isDataEndReached=true;
            //movieTVList=null;
            LogUtil.e(TAG+"_createMTVlist(url)","Error while fetching data. Doc returned null.",true);
        }
    }

    private Document parsehtml(String url) {
        Document doc = null;
        Connection.Response response = null;
        String useragent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/84.0.4147.89 Safari/537.36 Edg/84.0.522.48";
        int responseCode=0;
        msg = new Message();
        msg.arg1 = POP_INFO;
        try{
            response = Jsoup.connect(url).userAgent(useragent).ignoreContentType(true).execute();
            responseCode = response.statusCode();
            String responseMsg = response.statusMessage();
            LogUtil.l(TAG+"_parsehtml","Response Code: " + responseCode +" Response Msg: " + responseMsg,true);
            //LogUtil.l(TAG+"_parsehtml","Document: " + response.parse().html(),true);
            if(responseCode==200){
                doc = response.parse();
            }
            else{
                String[] ary = {"Error! Invalid response code: " + responseCode};
                msg.obj=ary;
                mHandler.sendMessage(msg);
                doc = null;
            }
        }catch(org.jsoup.HttpStatusException e){
            int st = e.getStatusCode();
            String ERROR_MSG;
            if(st==404) {
                ERROR_MSG = "HTTP ERROR " + e.getStatusCode() + "\nAnime not found.";
            }else{
                ERROR_MSG = "HTTP ERROR " + e.getStatusCode() + "\nTry different Anime.";
            }
            if(!isScrolledAction) {
                String[] ary = {ERROR_MSG};
                msg.obj = ary;
                mHandler.sendMessage(msg);
            }
            LogUtil.e(TAG+"_parsehtml",ERROR_MSG,true);
            doc=null;
        }catch(java.net.SocketTimeoutException ex){
            String[] ary = {"Sever Timeout.\nPlease retry."};
            msg.obj=ary;
            mHandler.sendMessage(msg);
            LogUtil.e(TAG+"_parsehtml","Sever Timeout. Please retry.",true);
            doc=null;
        }
        catch (Exception e) {
            e.printStackTrace();
            String[] ary = {"Some unexpected error occured.\nTry checking your internet connection."};
            msg.obj=ary;
            mHandler.sendMessage(msg);
            //CustomDialog.popinfo(MainActivity.this,"Error code: 1001.\nTimeout or internet error  ");
            LogUtil.e(TAG+"_parsehtml","Some unexpected error occured. Try checking your internet connection.",true);
            doc=null;
        }
        if(doc!=null && (isFirstloaded || searchmode)){
            oldurl = url.trim();
            newurl = response.url().toString().trim();
        }else{
            oldurl="";
            newurl="";
        }
        if(!oldurl.equals(newurl)){
            doc=null;

        }
        if(doc==null){
            isDataEndReached=true;
        }
        LogUtil.l(TAG+"_parser","oldURL: " + oldurl,true);
        LogUtil.l(TAG+"_parser","newUrl: " + newurl,true);

        return doc;
    }

    @Override
    public void OnRVClick(int position) {
        LogUtil.l("HomeFragment","Clicked Url: " + movieTVList.get(position).getUrl(),true);
        Bundle sendBundle = new Bundle();
        sendBundle.putString("title", movieTVList.get(position).getTitle());
        sendBundle.putString("url", movieTVList.get(position).getUrl());
        sendBundle.putString("imgicon", movieTVList.get(position).getImgicon());
        Intent j = new Intent(getContext(), DetailsActivity.class);
        j.putExtras(sendBundle);
        startActivity(j);
    }

    private void setHandler(){
        mHandler=new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.arg1){
                    case POP_INFO:
                        String[] ary = (String[]) msg.obj;
                        CustomDialog.popinfo(getActivity(),ary[0]);
                        break;
                }
                return false;
            }
        });
    }
}
