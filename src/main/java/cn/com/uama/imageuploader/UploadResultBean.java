package cn.com.uama.imageuploader;

import java.util.List;

/**
 * Created by liwei on 2017/7/3 15:02
 * Email: liwei@uama.com.cn
 * Description: 上传结果实体类
 */
public class UploadResultBean {
    private int status;
    private String msg;
    private List<String> data;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public List<String> getData() {
        return data;
    }

    public void setData(List<String> data) {
        this.data = data;
    }
}
