package com.itutorgroup.tutorchat.phone.domain.beans;

/**
 * Created by tom_zxzhang on 2016/8/24.
 */


public class ChatEmoji {


    private int id;

    private String character;

    private String faceName;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCharacter() {
        return character;
    }

    public void setCharacter(String character) {
        this.character = character;
    }

    public String getFaceName() {
        return faceName;
    }

    public void setFaceName(String faceName) {
        this.faceName = faceName;
    }

    @Override
    public String toString() {
        return "ChatEmoji{" +
                "id=" + id +
                ", character='" + character + '\'' +
                ", faceName='" + faceName + '\'' +
                '}';
    }



}
