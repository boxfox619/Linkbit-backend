package com.boxfox.linkbit.service;

import com.boxfox.linkbit.common.data.PostgresConfig;
import com.boxfox.vertx.service.AbstractService;
import io.one.sys.db.tables.pojos.Locale;
import io.one.sys.db.tables.daos.LocaleDao;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

public class LocaleService extends AbstractService {

    public void getMoneySymbol(String locale, Handler<AsyncResult<String>> res) {
        doAsync(future -> {
            future.complete(getMoneySymbol(locale));
        }, res);
    }

    public String getMoneySymbol(String localeStr){
        String currencySymbol = "KRW";
        LocaleDao localeDao = new LocaleDao(PostgresConfig.create());
        Locale locale = localeDao.findById(localeStr);
        if (locale!=null) {
            currencySymbol = locale.getCurrency();
        }
        return currencySymbol;
    }
}
