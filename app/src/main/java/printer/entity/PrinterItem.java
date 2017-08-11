package printer.entity;

import java.io.File;

/**
 * Created by 张建宇 on 2017/6/19.
 */

public class PrinterItem {
    private File file;
    private String flag;

    public PrinterItem() {
    }

    public PrinterItem(File file, String flag) {
        this.file = file;
        this.flag = flag;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    @Override
    public String toString() {
        return file.getAbsolutePath();
    }

}
