package srct.whatsopen.model;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;

public class OpenTimes extends RealmObject {
    @Override
    public String toString() {
        return "OpenTimes{" +
                "startDay=" + startDay +
                ", endDay=" + endDay +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                '}';
    }

    @SerializedName("start_day")
    private int startDay;

    @SerializedName("end_day")
    private int endDay;

    @SerializedName("start_time")
    private String startTime;

    @SerializedName("end_time")
    private String endTime;

    public int getEndDay() {
        return endDay;
    }

    public void setEndDay(int endDay) {
        this.endDay = endDay;
    }

    public int getStartDay() {
        return startDay;
    }

    public void setStartDay(int startDay) {
        this.startDay = startDay;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}
