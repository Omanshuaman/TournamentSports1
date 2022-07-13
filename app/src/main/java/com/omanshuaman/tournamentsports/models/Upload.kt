package com.example.adminuser.models

class Upload {
    var imageUrl: String? = null
    var name: String? = null
    var longitude: String? = null
    var latitude: String? = null
    var Id: String? = null


    constructor(
        Id: String?,
        name: String?, imageUrl: String?, longitude: String?, latitude: String?
    ) {
        this.Id = Id
        this.imageUrl = imageUrl
        this.name = name
        this.longitude = longitude
        this.latitude = latitude
    }

    constructor() {}
}
