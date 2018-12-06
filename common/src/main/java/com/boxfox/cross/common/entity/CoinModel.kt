package com.boxfox.cross.common.entity

class CoinModel {
    constructor()
    constructor(symbol: String, name: String){
        this.symbol = symbol
        this.name = name
    }
    lateinit var symbol: String
    lateinit var name: String
}