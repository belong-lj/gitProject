package entity;

import java.io.Serializable;
import java.util.List;

public class PageResult implements Serializable{
    //当前页结果
    private List rows;
    //总记录数
    private long total;

    public PageResult(List rows,long total) {
        this.rows = rows;
        this.total = total;
    }

    public List getRows() {
        return rows;
    }

    public void setRows(List rows) {
        this.rows = rows;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }
}
