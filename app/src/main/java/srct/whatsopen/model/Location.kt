package srct.whatsopen.model

import com.google.gson.annotations.SerializedName
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Location(
        @PrimaryKey @SerializedName("building") var building: String = ""
) : RealmObject()
