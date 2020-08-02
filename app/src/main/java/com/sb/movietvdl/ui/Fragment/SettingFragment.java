package com.sb.movietvdl.ui.Fragment;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sb.movietvdl.R;
import com.sb.movietvdl.Utils.CustomDialog;
import com.sb.movietvdl.Utils.LogUtil;
import com.sb.movietvdl.Utils.StaticResource;
import com.sb.movietvdl.config.MConfig;

import org.wlf.filedownloader.util.ArrayUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lib.folderpicker.FolderPicker;

public class SettingFragment extends Fragment {
    //boolean
    private boolean oldThemeChanged;
    private boolean isSettingSaved;
    //Buttons
    private Spinner sp_source_item, sp_p_dwn;
    private FloatingActionButton btn_save;
    private Button btn_browse;
    private Button btn_about;
    //TextView
    private TextView tv_dwon_loc;
    private TextView tv_source_info;
    //Radio Group
    private RadioGroup rg_theme;
    //Radio Button
    private RadioButton rb_sIplayer, rb_sEplayer, rb_sAplayer, rb_dark, rb_light;
    //SharedPrefEditor
    private SharedPreferences.Editor editor = null;
    //Constants
    private static final String TAG = "SettingFragment";
    private static int FOLDERPICKER_CODE = 9999;
    //view
    private static View viewsetting;

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtil.l(TAG+"_Browse", "return to result activity with: " + data.getData(),true);
        if(requestCode==FOLDERPICKER_CODE && resultCode == Activity.RESULT_OK){
            String path = data.getExtras().getString("data");
            LogUtil.l(TAG+"_Browse", "Folder: " + path,true);
            tv_dwon_loc.setText(path);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_setting,container,false);
        viewsetting = view;
        tv_dwon_loc=(TextView)view.findViewById(R.id.tv_dwon_loc);
        sp_source_item = (Spinner) view.findViewById(R.id.sp_source);
        sp_p_dwn = (Spinner) view.findViewById(R.id.sp_p_dwn);
        btn_save = (FloatingActionButton) view.findViewById(R.id.btn_save);
        btn_browse = (Button) view.findViewById(R.id.btn_down_loc_brw);
        btn_about = (Button) view.findViewById(R.id.btn_about);
        //tv_source_info = (TextView) view.findViewById(R.id.tv_source_info);
        rg_theme = (RadioGroup) view.findViewById(R.id.rg_theme);
        rb_sEplayer = (RadioButton) view.findViewById(R.id.rb_sEplayer);
        rb_sIplayer = (RadioButton) view.findViewById(R.id.rb_sIplayer);
        rb_sAplayer = (RadioButton) view.findViewById(R.id.rb_sAplayer);
        rb_light = (RadioButton) view.findViewById(R.id.rb_light);
        rb_dark = (RadioButton) view.findViewById(R.id.rb_dark);

        //init
        initvalues();

        return view;
    }

    private void initvalues(){
        int source  = MConfig.getConfig().source;
        tv_dwon_loc.setText(MConfig.getConfig().destdir);
        ArrayList<String> sp_list = new ArrayList<String>();
        for(int i=0;i<MConfig.getConfig().source_url.length;i++){
            sp_list.add(i + " " + MConfig.getConfig().source_des[i]);
        }
        ArrayAdapter sp_adapter = new ArrayAdapter(getActivity(),android.R.layout.simple_spinner_dropdown_item, sp_list);
        sp_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_source_item.setAdapter(sp_adapter);
        sp_source_item.setSelection(source);
        if(MConfig.getConfig().isPlayerAsk){
            rb_sAplayer.setChecked(true);
        }else if(MConfig.getConfig().isEPlayer){
            rb_sEplayer.setChecked(true);
        }else{
            rb_sIplayer.setChecked(true);
        }
        oldThemeChanged=MConfig.getConfig().isDarkTheme;
        if(oldThemeChanged){
            rb_dark.setChecked(true);
        }else{
            rb_light.setChecked(true);
        }
        String[] a = getResources().getStringArray(R.array.s_pdwn_enteries);
        ArrayList<String> arry = new ArrayList<String>(Arrays.asList(a));
        String s = Integer.toString(StaticResource.PARALLEL_DOWNLOAD);
        int j = arry.indexOf(s);
        sp_p_dwn.setSelection(j);

        btn_save.setOnClickListener(saveListener);
        btn_about.setOnClickListener(aboutListener);
        btn_browse.setOnClickListener(bwrListener);
        rg_theme.setOnCheckedChangeListener(themeListener);
    }



    private Button.OnClickListener saveListener = new Button.OnClickListener(){

        @Override
        public void onClick(View view) {
            LogUtil.l(TAG+"","Save Button Clicked.",true);
            String result="Setting Saved.";
            boolean makeToast=true;
            MConfig.getConfig().destdir = tv_dwon_loc.getText().toString();
            MConfig.getConfig().source = sp_source_item.getSelectedItemPosition();
            if(rb_sAplayer.isChecked()){
                MConfig.getConfig().isPlayerAsk=true;
                MConfig.getConfig().isEPlayer=false;
            }else if(rb_sEplayer.isChecked()){
                MConfig.getConfig().isEPlayer=true;
                MConfig.getConfig().isPlayerAsk=false;
            }else {
                MConfig.getConfig().isEPlayer=false;
                MConfig.getConfig().isPlayerAsk=false;
            }
            SharedPreferences MySetting = getActivity().getSharedPreferences(StaticResource.PREF_SETTING, 0);
            editor = MySetting.edit();
            //Dark theme
            if(oldThemeChanged!=MConfig.getConfig().isDarkTheme) {
                if (MConfig.getConfig().isDarkTheme) {
                    editor.putInt("cur_theme", StaticResource.DARK_THEME);
                    editor.putBoolean("isDarkTheme", true);
                    result = result + "\nTheme Settings take affect on next start.";
                } else {
                    editor.putInt("cur_theme", StaticResource.LIGHT_THEME);
                    editor.putBoolean("isDarkTheme", false);
                    result = result + "\nTheme Settings take affect on next start.";
                }
                makeToast=false;
            }
            editor.putInt("parallel_download", Integer.parseInt((String) sp_p_dwn.getSelectedItem()));
            editor.commit();
            MConfig.getConfig().save();
            if(makeToast){
                CustomDialog.showToast(getContext(),result);
            }else {
                CustomDialog.popinfo(getContext(), result);
            }
        }
    };

    private Button.OnClickListener bwrListener = new Button.OnClickListener(){

        @Override
        public void onClick(View view) {
            LogUtil.l(TAG+"","Browser Button Clicked.",true);
            Intent intent = new Intent(getContext(), FolderPicker.class);
            intent.putExtra("title","Choose save location");
            startActivityForResult(intent,FOLDERPICKER_CODE);
        }
    };

    private Button.OnClickListener aboutListener = new Button.OnClickListener(){

        @Override
        public void onClick(View view) {
            LogUtil.l(TAG+"","About Button Clicked.",true);
            CustomDialog.popinfo(getActivity(),getString(R.string.about));
        }
    };

    private RadioGroup.OnCheckedChangeListener themeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int i) {
            LogUtil.l(TAG+"","Theme changed Clicked: " + radioGroup.getCheckedRadioButtonId(), true);
            switch (radioGroup.getCheckedRadioButtonId()){
                case R.id.rb_dark:
                    MConfig.getConfig().isDarkTheme=true;
                    break;
                case R.id.rb_light:
                    MConfig.getConfig().isDarkTheme=false;
                    break;
            }

        }
    };

}
