package com.boxfox.linkbit.common.entity.coin

class CoinPriceModel : CoinModel{
    constructor()
    constructor(c: CoinModel) {
        this.name = c.name
        this.symbol = c.symbol
        this.themeColor = c.themeColor
    }
    var price: Double = 0.toDouble()
}