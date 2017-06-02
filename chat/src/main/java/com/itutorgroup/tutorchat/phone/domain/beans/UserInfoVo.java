package com.itutorgroup.tutorchat.phone.domain.beans;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.app.LPApp;
import com.itutorgroup.tutorchat.phone.domain.db.model.UserInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cn.salesuite.saf.utils.Preconditions;

/**
 * Created by tom_zxzhang on 2016/8/16.
 */
public class UserInfoVo implements Serializable{



    public String id;

    public String img;

    public String Name;

    public String Title;

    public String Department;

    public String firstLetter;

    public UserInfo userInfo;

    public UserInfoVo() {

    }




    /**
     *
     * 将数据转换为ListView需要的类型
     * @param list
     * @return
     */

    public static List<UserInfoVo> getUserInfoVo(List<UserInfo> list){

        if(Preconditions.isBlank(list))
            return null;
        List<UserInfoVo> userInfoVoList = new ArrayList<UserInfoVo>();
        for(UserInfo info: list){
            UserInfoVo userInfoVo = new UserInfoVo();
            userInfoVo.id = info.UserID;
            userInfoVo.img = info.Image;
            userInfoVo.Name = info.Name;
            userInfoVo.Title = info.Title;
            userInfoVo.Department = LPApp.getInstance().getString(R.string.personal_department_position, info.DepartmentGroup, info.Department);
            userInfoVo.userInfo = info;
            if (info.Name.substring(0,1).toUpperCase().matches("[A-Z]")) {
                userInfoVo.firstLetter = info.Name.substring(0,1).toUpperCase();
            }else{
                userInfoVo.firstLetter = "#";
            }
            userInfoVoList.add(userInfoVo);
        }


        /**
         * 对List进行排序
         */

        Collections.sort(userInfoVoList, new Comparator<UserInfoVo>() {
            @Override
            public int compare(UserInfoVo lhs, UserInfoVo rhs) {
                if (lhs.firstLetter.contains("#")) {
                    return 1;
                } else if (rhs.firstLetter.contains("#")) {
                    return -1;
                } else {
                    return lhs.firstLetter.compareTo(rhs.firstLetter);
                }
            }
        });


        return  userInfoVoList;
    }

    @Override
    public String toString() {
        return "UserInfoVo{" +
                "id='" + id + '\'' +
                ", img='" + img + '\'' +
                ", Name='" + Name + '\'' +
                ", Title='" + Title + '\'' +
                ", Department='" + Department + '\'' +
                ", firstLetter='" + firstLetter + '\'' +
                '}';
    }
}
