package com.omanshuaman.tournamentsports.models

class Upload {
    var imageUrl: String? = null
    var tournamentName: String? = null
    var longitude: String? = null
    var latitude: String? = null
    var Id: String? = null
    var SportsType: String? = null
    var entryFee: String? = null
    var prizeMoney: String? = null
    var uid: String? = null


    constructor(
        Id: String?,
        tournamentName: String?,
        imageUrl: String?,
        longitude: String?,
        latitude: String?,
        SportsType: String?,
        entryFee: String?,
        prizeMoney: String?,
        uid: String?
    ) {
        this.Id = Id
        this.imageUrl = imageUrl
        this.tournamentName = tournamentName
        this.longitude = longitude
        this.latitude = latitude
        this.SportsType = SportsType
        this.entryFee = entryFee
        this.prizeMoney = prizeMoney
        this.uid = uid


    }

    constructor() {}
}
