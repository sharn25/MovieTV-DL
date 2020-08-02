package com.sb.movietvdl.ui.Fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout;
import com.sb.movietvdl.Adapter.DWNListViewAdapter;
import com.sb.movietvdl.R;
import com.sb.movietvdl.Utils.CustomDialog;
import com.sb.movietvdl.Utils.LogUtil;
import com.sb.movietvdl.Utils.MTVUtil;
import com.sb.movietvdl.config.MConfig;

import org.wlf.filedownloader.DownloadFileInfo;
import org.wlf.filedownloader.DownloadStatusConfiguration;
import org.wlf.filedownloader.FileDownloader;
import org.wlf.filedownloader.listener.OnDeleteDownloadFileListener;
import org.wlf.filedownloader.listener.OnDeleteDownloadFilesListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/*
    * Created by Sharn25
    * Dated 18-06-2020
    */

public class DownloadFragment extends Fragment implements DWNListViewAdapter.OnItemSelectListener {
    //Elements
    private TabLayout tlDwn;
    private LinearLayout mLnlyOperation, lvDefaultOptions;
    private Button mBtnDelete, btnPauseAll, btnResumeAll;
    private Button mBtnResume;
    private Button mBtnPause;
    private ListView lvDownloadFileList;
    //Adapter
    private DWNListViewAdapter mDownloadFileListAdapter;
    //toast
    private Toast mToast;
    //constants
    private final static String TAG = "DownloadFragment";

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(!hidden){
            LogUtil.l(TAG + "_onHiddenChanged", "Showing fragment", true);
            if (mDownloadFileListAdapter != null) {
                //mDownloadFileListAdapter.updateShow();
            }
        }else{
            LogUtil.l(TAG + "_onHiddenChanged", "Hideing fragment", true);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_download,container,false);
        //init Elements
        tlDwn = (TabLayout) view.findViewById(R.id.tl_dwn);
        tlDwn.addOnTabSelectedListener(tlDwnListener);
        lvDownloadFileList = (ListView) view.findViewById(R.id.lv_dwn);
        mLnlyOperation = (LinearLayout) view.findViewById(R.id.lnlyOperation);
        lvDefaultOptions = (LinearLayout) view.findViewById(R.id.lv_defalutoption);
        mBtnDelete = (Button) view.findViewById(R.id.btnDelete);
        mBtnResume = (Button) view.findViewById(R.id.btnResume);
        mBtnPause = (Button) view.findViewById(R.id.btnPause);
        btnPauseAll = (Button) view.findViewById(R.id.btn_pause_all);
        btnPauseAll.setOnClickListener(btnPauseAllListener);
        btnResumeAll = (Button) view.findViewById(R.id.btn_resume_all);
        btnResumeAll.setOnClickListener(btnResumeAllListener);
        // ListView
        mDownloadFileListAdapter = new DWNListViewAdapter(getActivity());
        lvDownloadFileList.setAdapter(mDownloadFileListAdapter);
        mDownloadFileListAdapter.setOnItemSelectListener(this);

        // registerDownloadStatusListener

        boolean isDownloadStatusConfigurationTest = false;// TEST

        if (!isDownloadStatusConfigurationTest) {
            // register to listen all
            FileDownloader.registerDownloadStatusListener(mDownloadFileListAdapter);
        } else {
            // register to only listen special url
            DownloadStatusConfiguration.Builder builder = new DownloadStatusConfiguration.Builder();
            builder.addListenUrl("http://182.254.149.157/ftp/image/shop/product/Kids Addition & Subtraction 1.0.apk");
            FileDownloader.registerDownloadStatusListener(mDownloadFileListAdapter, builder.build());
        }
        LogUtil.l(TAG+"_OnCreateView", "DownloadActivity oncreate complete.",true);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        LogUtil.l(TAG,"onStart",true);
    }

    @Override
    public void onResume() {
        super.onResume();
        LogUtil.l(TAG,"onResume",true);
    }

    @Override
    public void onPause() {
        super.onPause();
        LogUtil.l(TAG,"onPause",true);
    }

    @Override
    public void onStop() {
        super.onStop();
        LogUtil.l(TAG,"onStop",true);
    }

    private Button.OnClickListener btnPauseAllListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(lvDownloadFileList.getCount()>=1) {
                FileDownloader.pauseAll();
            }
        }
    };

    private Button.OnClickListener btnResumeAllListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(lvDownloadFileList.getCount()>=1) {
                FileDownloader.continueAll(false);
            }
        }
    };

    private TabLayout.OnTabSelectedListener tlDwnListener = new TabLayout.OnTabSelectedListener(){

        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            switch (tab.getPosition()){
                case 0:
                    LogUtil.l(TAG+"_tlDwnListener","Incompleted Clicked",true);
                    break;
                case 1:
                    LogUtil.l(TAG+"_tlDwnListener","Completed Clicked",true);
                    break;
            }
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {
        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {
        }
    };

    private void updateAdapter() {
        if (mDownloadFileListAdapter == null) {
            return;
        }
        mDownloadFileListAdapter.updateShow();
    }



    @Override
    public void onSelected(final List<DownloadFileInfo> selectDownloadFileInfos) {
        mLnlyOperation.setVisibility(View.VISIBLE);
        lvDefaultOptions.setVisibility(View.GONE);

        mBtnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getString(R.string.main__confirm_whether_delete_save_file));
                builder.setNegativeButton(getString(R.string.main__confirm_no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteDownloadFiles(false, selectDownloadFileInfos);
                    }
                });
                builder.setPositiveButton(getString(R.string.main__confirm_yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        deleteDownloadFiles(true, selectDownloadFileInfos);
                    }
                });
                builder.show();
            }

            private void deleteDownloadFiles(boolean deleteDownloadedFile, List<DownloadFileInfo>
                    selectDownloadFileInfos) {

                List<String> urls = new ArrayList<String>();

                for (DownloadFileInfo downloadFileInfo : selectDownloadFileInfos) {
                    if (downloadFileInfo == null) {
                        continue;
                    }
                    urls.add(downloadFileInfo.getUrl());
                }

                // single delete
                if (urls.size() == 1) {
                    FileDownloader.delete(urls.get(0), deleteDownloadedFile, new OnDeleteDownloadFileListener() {
                        @Override
                        public void onDeleteDownloadFileSuccess(DownloadFileInfo downloadFileDeleted) {
                            CustomDialog.showToast(getContext(),getString(R.string.main__delete_succeed));
                            updateAdapter();
                            String f = downloadFileDeleted.getFileDir();
                            LogUtil.l(TAG+"_onfiledelete","Filedir: " + f,true);
                            MTVUtil.delEmptyDir(f);
                            LogUtil.l(TAG+"_btnDelete", "onDeleteDownloadFileSuccess deleting，File " + downloadFileDeleted.getFileName()
                                    + "Done",true);
                        }

                        @Override
                        public void onDeleteDownloadFilePrepared(DownloadFileInfo downloadFileNeedDelete) {
                            if (downloadFileNeedDelete != null) {
                                CustomDialog.showToast(getContext(),getString(R.string.main__deleting) + downloadFileNeedDelete.getFileName());
                            }
                        }

                        @Override
                        public void onDeleteDownloadFileFailed(DownloadFileInfo downloadFileInfo,
                                                               DeleteDownloadFileFailReason failReason) {
                            CustomDialog.showToast(getContext(),getString(R.string.main__delete) + downloadFileInfo.getFileName() + getString(R
                                    .string.main__failed));

                            LogUtil.l(TAG+"_btnDelete","onDeleteDownloadFileFailed Delete，File " + downloadFileInfo.getFileName() +
                                    "Failed",true);
                        }
                    });
                }
                // multi delete
                else {
                    LogUtil.l(TAG+"_btnDelete", "Click to start the bulk delete",true);
                    FileDownloader.delete(urls, deleteDownloadedFile, new OnDeleteDownloadFilesListener() {

                        @Override
                        public void onDeletingDownloadFiles(List<DownloadFileInfo> downloadFilesNeedDelete,
                                                            List<DownloadFileInfo> downloadFilesDeleted,
                                                            List<DownloadFileInfo> downloadFilesSkip,
                                                            DownloadFileInfo downloadFileDeleting) {
                            if (downloadFileDeleting != null) {
                                CustomDialog.showToast(getContext(),getString(R.string.main__deleting) + downloadFileDeleting.getFileName() +
                                        getString(R.string.main__progress) + (downloadFilesDeleted.size() +
                                        downloadFilesSkip.size()) + getString(R.string.main__failed2) +
                                        downloadFilesSkip.size() + getString(R.string
                                        .main__skip_and_total_delete_division) +
                                        downloadFilesNeedDelete.size());
                            }

                            updateAdapter();
                        }

                        @Override
                        public void onDeleteDownloadFilesPrepared(List<DownloadFileInfo> downloadFilesNeedDelete) {
                            CustomDialog.showToast(getContext(),getString(R.string.main__need_delete) + downloadFilesNeedDelete.size());
                        }

                        @Override
                        public void onDeleteDownloadFilesCompleted(List<DownloadFileInfo> downloadFilesNeedDelete,
                                                                   List<DownloadFileInfo> downloadFilesDeleted) {

                            String text = getString(R.string.main__delete_finish) + downloadFilesDeleted.size() +
                                    getString(R.string.main__failed3) + (downloadFilesNeedDelete.size() -
                                    downloadFilesDeleted.size());

                            CustomDialog.showToast(getContext(),text);
                            for(int i =0; i<downloadFilesDeleted.size();i++){
                                String f = downloadFilesDeleted.get(i).getFileDir();
                                LogUtil.l(TAG+"_onfiledeletecompleted","Filedir: " + f,true);
                                MTVUtil.delEmptyDir(f);
                            }
                            updateAdapter();

                            LogUtil.l(TAG+"_btnDelete","onDeleteDownloadFilesCompleted Complete_callback，" + text,true);
                        }
                    });
                }
            }
        });
        mBtnResume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtil.l(TAG+"_btnResume","onResume button pressed.",true);
                List<String> urls = new ArrayList<String>();

                for (DownloadFileInfo downloadFileInfo : selectDownloadFileInfos) {
                    if (downloadFileInfo == null) {
                        continue;
                    }
                    urls.add(downloadFileInfo.getUrl());
                }
                FileDownloader.start(urls);

            }
        });
        mBtnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtil.l(TAG+"_btnPause","onPause button pressed.",true);
                List<String> urls = new ArrayList<String>();

                for (DownloadFileInfo downloadFileInfo : selectDownloadFileInfos) {
                    if (downloadFileInfo == null) {
                        continue;
                    }
                    urls.add(downloadFileInfo.getUrl());
                }
                FileDownloader.pause(urls);
            }
        });
    }

    @Override
    public void onNoneSelect() {
        mLnlyOperation.setVisibility(View.GONE);
        lvDefaultOptions.setVisibility(View.VISIBLE);
    }

}
