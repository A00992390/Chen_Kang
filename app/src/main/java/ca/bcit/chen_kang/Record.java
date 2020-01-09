package ca.bcit.chen_kang;


import java.util.Date;

public class Record {
    private String recordId;
    private String userId;

    private Date curDate;
    private Double srReading;
    private Double drReading;
    private String condition;


    public Record() {}

    public Record(String recordId, String userId, Date curDate, Double srReading, Double drReading, String condition) {
        this.recordId = recordId;
        this.userId = userId;
        this.curDate = curDate;
        this.srReading = srReading;
        this.drReading = drReading;
        this.condition = condition;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public Date getCurDate() {
        return curDate;
    }

    public void setCurDate(Date curDate) {
        this.curDate = curDate;
    }

    public Double getSrReading() {
        return srReading;
    }

    public void setSrReading(Double srReading) {
        this.srReading = srReading;
    }

    public Double getDrReading() {
        return drReading;
    }

    public void setDrReading(Double drReading) {
        this.drReading = drReading;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }
}
