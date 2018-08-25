package com.boxfox.cross.service;

import com.boxfox.cross.common.data.PostgresConfig;
import com.boxfox.cross.common.vertx.service.AbstractService;
import io.one.sys.db.tables.pojos.Locale;
import io.one.sys.db.tables.daos.LocaleDao;

import java.util.List;

public class LocaleService extends AbstractService {

    public String getLocaleMoneySymbol(String locale) {
        String currencySymbol = "KRW";
        LocaleDao localeDao = new LocaleDao(PostgresConfig.create());
        List<Locale> results = localeDao.fetchByCountry(locale);
        if (results.size() > 0) {
            currencySymbol = results.get(0).getCurrency();
        }
        return currencySymbol;
    }
}
