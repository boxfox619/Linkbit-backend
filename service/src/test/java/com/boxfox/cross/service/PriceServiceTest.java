package com.boxfox.cross.service;

import org.junit.Assert;
import org.junit.Test;

public class PriceServiceTest {

    private PriceService priceService = new PriceService();

    @Test
    public void ethereumPriceTest(){
        double price = priceService.getPrice("ETH");
        System.out.println(price);
        Assert.assertTrue(price>0);
    }
}
