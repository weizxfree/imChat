package com.itutorgroup.tutorchat.phone.domain.beans;

import com.itutorgroup.tutorchat.phone.domain.db.model.MessageModel;

import java.util.List;

/**
 * Created by tom_zxzhang on 2016/9/27.
 */
public class MessageModelByPage {

    public int page;

    public int index;

    public List<MessageModel> list;

    @Override
    public String toString() {
        return "MessageModelByPage{" +
                "page=" + page +
                ", index=" + index +
                ", list=" + list.size() +
                '}';
    }
}
