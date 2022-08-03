package com.omanshuaman.tournamentsports.models

class ParticipantInfo {
    var yourname: String? = null
    var phonenumber: String? = null


    constructor(
        yourname: String?,
        phonenumber: String?,
        ) {
        this.yourname = yourname
        this.phonenumber = phonenumber

    }

    constructor() {}
}