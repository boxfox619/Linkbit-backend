package com.boxfox.cross.service.friend;

import static io.one.sys.db.Tables.ACCOUNT;
import static io.one.sys.db.Tables.FRIEND;
import static io.one.sys.db.Tables.WALLET;

import com.boxfox.cross.service.ServiceException;
import com.google.api.client.http.HttpStatusCodes;
import com.linkbit.android.entity.UserModel;
import io.one.sys.db.tables.records.AccountRecord;
import io.one.sys.db.tables.records.FriendRecord;
import java.util.ArrayList;
import java.util.List;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;

public class FriendServiceImpl {

  public List<UserModel> loadFriends(DSLContext ctx, String uid) throws ServiceException {
    List<UserModel> friends = new ArrayList();
    Result<FriendRecord> result = ctx.selectFrom(FRIEND).where(FRIEND.UID_1.eq(uid).or(FRIEND.UID_2.eq(uid))).fetch();
    for(FriendRecord r : result){
      String target = r.getUid_1().equals(uid) ? r.getUid_2() : r.getUid_1();
      AccountRecord acc = ctx.selectFrom(ACCOUNT).where(ACCOUNT.UID.eq(target)).fetch().stream().findFirst().orElse(null);
      if(acc == null){
        deleteFriend(ctx, uid, target);
        continue;
      }
      UserModel user = new UserModel();
      user.setName(acc.getName());
      user.setEmail(acc.getEmail());
      user.setProfileUrl(acc.getProfile());
      user.setUid(acc.getUid());
      user.setLinkbitAddress(acc.getAddress());
      friends.add(user);
    }
    return friends;
  }

  public UserModel getUser(DSLContext ctx, String uid) throws ServiceException{
    AccountRecord acc = ctx.selectFrom(ACCOUNT).where(ACCOUNT.UID.eq(uid)).fetch().stream().findFirst().orElse(null);
    if (acc == null) {
      throw new ServiceException(HttpStatusCodes.STATUS_CODE_NOT_FOUND, "User not found");
    }
    UserModel user = new UserModel();
    user.setName(acc.getName());
    user.setEmail(acc.getEmail());
    user.setProfileUrl(acc.getProfile());
    user.setUid(acc.getUid());
    user.setLinkbitAddress(acc.getAddress());
    return user;
  }

  public boolean addFriend(DSLContext ctx, String ownUid, String uid) throws ServiceException{
    if (ctx.selectFrom(ACCOUNT).where(ACCOUNT.UID.eq(uid)).fetch().size() == 0) {
      throw new ServiceException(HttpStatusCodes.STATUS_CODE_NOT_FOUND, "Target user can not found");
    } else {
      int result = ctx.insertInto(FRIEND, FRIEND.UID_1, FRIEND.UID_2).values(ownUid, uid)
          .execute();
      if (result == 1) {
        return true;
      } else {
        throw new ServiceException(HttpStatusCodes.STATUS_CODE_SERVER_ERROR, "Update fail");
      }
    }
  }

  public boolean deleteFriend(DSLContext ctx, String ownUid, String uid) throws ServiceException {
    AccountRecord accountRecord = ctx.selectFrom(ACCOUNT).where(ACCOUNT.UID.eq(ownUid)).fetch().stream().findFirst().orElse(null);
      if (accountRecord==null) {
        throw new ServiceException(HttpStatusCodes.STATUS_CODE_NOT_FOUND, "Target user can not found");
      } else {
        int result = ctx
            .deleteFrom(FRIEND)
            .where(FRIEND.UID_1.equal(ownUid).and(FRIEND.UID_2.equal(uid))).execute();
        if (result == 1) {
          return true;
        } else {
          throw new ServiceException(HttpStatusCodes.STATUS_CODE_SERVER_ERROR, "Update fail");
        }
      }
  }

  public List<UserModel> searchUser(DSLContext ctx, String text) throws ServiceException {
    List<UserModel> accounts = new ArrayList();
    Result<Record> records = ctx.selectFrom(ACCOUNT.join(WALLET).on(ACCOUNT.UID.eq(WALLET.UID)))
        .where(
            ACCOUNT.ADDRESS.like(text)
                .or(WALLET.ADDRESS.like(text))
                .or(WALLET.CROSSADDRESS.like(text))
                .or(ACCOUNT.EMAIL.like(text))
                .or(ACCOUNT.NAME.like(text))
        ).fetch();
    records.forEach(r -> {
      UserModel user = new UserModel();
      user.setName(r.getValue(ACCOUNT.NAME));
      user.setEmail(r.getValue(ACCOUNT.EMAIL));
      user.setLinkbitAddress(r.getValue(ACCOUNT.ADDRESS));
      user.setUid(r.getValue(ACCOUNT.UID));
      user.setProfileUrl(r.getValue(ACCOUNT.PROFILE));
      accounts.add(user);
    });
    return accounts;
  }
}
