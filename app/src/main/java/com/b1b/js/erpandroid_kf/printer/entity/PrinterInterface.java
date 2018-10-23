package com.b1b.js.erpandroid_kf.printer.entity;

import java.io.IOException;

/**
 Created by 张建宇 on 2017/10/31. */

public interface PrinterInterface {
    void setFont(int i) throws IOException;

    void printTextLn(String str) throws IOException;

    void newLine() throws IOException;

    void printCode(String code);

    void printCode(String code, int flag) throws IOException;

    void printText(String data) throws IOException;
}
