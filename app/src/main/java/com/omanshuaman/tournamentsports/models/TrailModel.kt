package com.example.adminuser.models


class TrailModel {

    var image: String? = null
    var bed: String? = null
    var bedroom: String? = null

    constructor() {}
    constructor(
        image: String?,
        bed: String?, bedroom: String?
    ) {

        this.image = image
        this.bed = bed
        this.bedroom = bedroom
    }

}