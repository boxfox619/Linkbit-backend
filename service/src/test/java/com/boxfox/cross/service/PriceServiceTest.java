package com.boxfox.cross.service;

import org.junit.Assert;
import org.junit.Test;

public class PriceServiceTest {

    @Test
    public void ethereumPriceTest(){
        double price = PriceService.getPrice("ETH");
        System.out.println(price);
        Assert.assertTrue(price>0);
    }
}
