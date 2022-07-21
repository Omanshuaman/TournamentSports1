package com.omanshuaman.tournamentsports.models

import com.google.android.gms.maps.model.LatLng

class LocationModel {

    var longitude: String? = null
    var latitude: String? = null
    constructor( longitude: String?, latitude: String?) {

        this.longitude = longitude
        this.latitude = latitude
    }

    constructor() {}
}
