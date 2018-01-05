package srct.whatsopen.model

import com.google.gson.annotations.SerializedName

import io.realm.RealmObject

open class OpenTimes : RealmObject {

    @SerializedName("start_day")
    var startDay: Int = 0

    @SerializedName("end_day")
    var endDay: Int = 0

    @SerializedName("start_time")
    var startTime: String? = null

    @SerializedName("end_time")
    var endTime: String? = null

    constructor(startDay: Int, endDay: Int, startTime: String, endTime: String) {
        this.startDay = startDay
        this.endDay = endDay
        this.startTime = startTime
        this.endTime = endTime
    }

    constructor() {}

    override fun toString(): String {
        return "OpenTimes{" +
                "startDay=" + startDay +
                ", endDay=" + endDay +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                '}'
    }
}
