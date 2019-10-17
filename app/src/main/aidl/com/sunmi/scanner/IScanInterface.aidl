package com.sunmi.scanner;


 interface IScanInterface {
 /**
 
     */
    void sendKeyEvent(in KeyEvent key);
    /**
  
     */
    void scan();
    /**
   
     */
    void stop();
    /**
   
     * 100-->NONE
     * 101-->P2Lite
     * 102-->l2-newland
     * 103-->l2-zabra
     */
    int getScannerModel();
}
