package com.boxfox.cross.service;

import com.boxfox.cross.common.data.PostgresConfig;
import com.boxfox.vertx.vertx.service.AbstractService;
import io.one.sys.db.tables.pojos.Locale;
import io.one.sys.db.tables.daos.LocaleDao;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

public class LocaleService extends AbstractService {

    public void getLocaleMoneySymbol(String locale, Handler<AsyncResult<String>> res) {
        doAsync(future -> {
            future.complete(getLocaleMoneySymbol(locale));
        }, res);
    }

    public String getLocaleMoneySymbol(String localeStr){
        String currencySymbol = "KRW";
        LocaleDao localeDao = new LocaleDao(PostgresConfig.create(), getVertx());
        Locale locale = localeDao.findOneById(localeStr).result();
        if (locale!=null) {
            currencySymbol = locale.getCurrency();
        }
        return currencySymbol;
    }
}
