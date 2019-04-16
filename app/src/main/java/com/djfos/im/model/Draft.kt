package com.djfos.im.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.djfos.im.filter.IFilter

@Entity
data class Draft(
        val sourceImageUriString: String
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    var history: MutableList<IFilter> = mutableListOf()
}
