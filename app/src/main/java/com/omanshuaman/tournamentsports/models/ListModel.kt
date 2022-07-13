package com.example.adminuser.models

class ListModel {
    var bed: String? = null
    var bedroom: String? = null
    var id: String? = null
    var image: String? = null
    var oldprice: String? = null
    var title: String? = null
    var totalPrice: String? = null
    var type: String? = null

    constructor(
        bed: String?,
        bedroom: String?,
        id: String?,
        image: String?,
        oldprice: String?,
        title: String?,
        totalPrice: String?,
        type: String?
    ) {
        this.bed = bed
        this.bedroom = bedroom
        this.id = id
        this.image = image
        this.oldprice = oldprice
        this.title = title
        this.totalPrice = totalPrice
        this.type = type
    }
}