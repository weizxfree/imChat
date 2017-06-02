package com.itutorgroup.tutorchat.phone.domain.request.v2;

import com.itutorgroup.tutorchat.phone.domain.db.model.TopModel;
import com.itutorgroup.tutorchat.phone.domain.request.CommonRequest;

import java.util.List;

import io.protostuff.Tag;

/**
 * Created by joyinzhao on 2016/11/14.
 */
public class SetChatOrderRequest extends CommonRequest {
    @Tag(4)
    public List<TopModel> Tops;
}
