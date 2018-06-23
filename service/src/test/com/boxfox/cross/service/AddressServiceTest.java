package com.boxfox.cross.service;

import org.junit.Assert;
import org.junit.Test;

public class AddressServiceTest {

    @Test
    public void createAddressTest(){
        String address = AddressService.createRandomAddress("vKEVPGh2r4h0dVpuONLuZ4Uwuh02");
        Assert.assertNotNull(address);
    }
}
