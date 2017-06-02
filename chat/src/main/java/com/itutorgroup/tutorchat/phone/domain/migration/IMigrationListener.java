package com.itutorgroup.tutorchat.phone.domain.migration;

/**
 * Created by joyinzhao on 2016/11/19.
 */
public interface IMigrationListener<T, R> {
    R migrate(T src);
}
