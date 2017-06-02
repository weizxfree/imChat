package com.itutorgroup.tutorchat.phone.adapter.group;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.domain.db.model.UserInfo;

import java.util.List;

import cn.salesuite.saf.adapter.SAFAdapter;
import cn.salesuite.saf.inject.Injector;
import cn.salesuite.saf.inject.annotation.InjectView;

/**
 * Created by Administrator on 2016/5/12 0012.
 */
public class GroupMemberListdapter extends SAFAdapter<UserInfo> {


    private LayoutInflater mInflater ;
    private ViewHolder holder;
    private Context mContext;


    public GroupMemberListdapter(Context context, List<UserInfo> list){
        this.mInflater = LayoutInflater.from(context);
        mContext = context;
        mList = list;
    }
    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        UserInfo item = (UserInfo) getItem(i);
        if (view == null) {
            view = mInflater.inflate(R.layout.list_item_message_recipients, null);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }


        return view;
    }

    class ViewHolder{
        @InjectView
        ImageView img;
        @InjectView
        TextView name;
        public ViewHolder(View view) {
            Injector.injectInto(this, view);
        }
    }
}
