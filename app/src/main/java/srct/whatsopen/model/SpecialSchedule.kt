package srct.whatsopen.model

import com.google.gson.annotations.SerializedName

import io.realm.RealmList
import io.realm.RealmObject

open class SpecialSchedule : RealmObject, Schedule {

    @SerializedName("open_times")
    private var mOpenTimesList: RealmList<OpenTimes>? = null

    @SerializedName("valid_start")
    var validStart: String? = null

    @SerializedName("valid_end")
    private var validEnd: String? = null

    constructor(openTimesList: RealmList<OpenTimes>, validStart: String, validEnd: String) {
        mOpenTimesList = openTimesList
        this.validStart = validStart
        this.validEnd = validEnd
    }

    constructor() {}

    override fun getValidEnd(): String? {
        return validEnd
    }

    fun setValidEnd(validEnd: String) {
        this.validEnd = validEnd
    }

    override fun getOpenTimesList(): RealmList<OpenTimes>? {
        return mOpenTimesList
    }

    fun setOpenTimesList(openTimesList: RealmList<OpenTimes>) {
        mOpenTimesList = openTimesList
    }
}
