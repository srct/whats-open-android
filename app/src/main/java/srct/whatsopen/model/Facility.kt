package srct.whatsopen.model

import com.google.gson.annotations.SerializedName

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Facility : RealmObject {

    @PrimaryKey
    @SerializedName("facility_name")
    var name: String? = null

    @SerializedName("facility_location")
    var location: Location? = null

    @SerializedName("main_schedule")
    var mainSchedule: MainSchedule? = null

    @SerializedName("special_schedules")
    var specialSchedules: RealmList<SpecialSchedule>? = null

    var isOpen: Boolean = false
    var isFavorited: Boolean = false
    var statusDuration: String? = null

    constructor(name: String, location: Location, mainSchedule: MainSchedule,
                specialSchedules: RealmList<SpecialSchedule>, isOpen: Boolean,
                isFavorited: Boolean, statusDuration: String) {
        this.name = name
        this.location = location
        this.mainSchedule = mainSchedule
        this.specialSchedules = specialSchedules
        this.isOpen = isOpen
        this.isFavorited = isFavorited
        this.statusDuration = statusDuration
    }

    constructor() {}
}
