package com.zero.hm.effect.timewarpscan;

public interface Listener {
    public void imageSavedSuccessfully(String filePath);

    public void moveScanLine(int pixel, boolean isHorizontal);

    public void quitScan();

}
