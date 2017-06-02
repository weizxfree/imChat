package com.itutorgroup.tutorchat.phone.ui.photo.picker.model;

import java.util.ArrayList;
import java.util.List;

public class PhotoDirectory {

    private String id;
    /**
     * 文件夹名
     */
    private String name;
    /**
     * 文件夹路径
     */
    private String coverPath;
    /**
     * 该文件夹下图片列表
     */
    private ArrayList<Photo> photos = new ArrayList<Photo>();
    /**
     * 标识是否选中该文件夹
     */
    private boolean isSelected;
    private long dateAdded;

    public boolean isSelected() {
        return isSelected;
    }

    public void setIsSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof PhotoDirectory))
            return false;

        PhotoDirectory directory = (PhotoDirectory) o;

        if (!id.equals(directory.id))
            return false;
        return name.equals(directory.name);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCoverPath() {
        return coverPath;
    }

    public void setCoverPath(String coverPath) {
        this.coverPath = coverPath;
    }

    public long getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(long dateAdded) {
        this.dateAdded = dateAdded;
    }

    public ArrayList<Photo> getPhotos() {
        return photos;
    }

    public void setPhotos(ArrayList<Photo> photos) {
        this.photos = photos;
    }

    public List<String> getPhotoPaths() {
        List<String> paths = new ArrayList<String>(photos.size());
        for (Photo photo : photos) {
            paths.add(photo.getPath());
        }
        return paths;
    }

    public void addPhoto(int id, String path) {
        photos.add(new Photo(id, path));
    }
}
