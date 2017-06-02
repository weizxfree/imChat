package com.itutorgroup.tutorchat.phone.domain.response.tcp;

import com.itutorgroup.tutorchat.phone.domain.beans.objectupdate.Instruction;

import java.util.ArrayList;

import io.protostuff.Tag;

/**
 * Created by joyinzhao on 2016/11/18.
 */
public class TcpInstructionResponse {
    @Tag(1)
    public long ReceiptID;

    @Tag(2)
    public ArrayList<Instruction> InstructionList;

    @Override
    public String toString() {
        return "TcpInstructionResponse{" +
                "ReceiptID=" + ReceiptID +
                ", InstructionList=" + InstructionList +
                '}';
    }
}
