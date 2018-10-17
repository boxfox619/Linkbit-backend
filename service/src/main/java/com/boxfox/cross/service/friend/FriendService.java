package com.boxfox.cross.service.friend;

import com.boxfox.cross.service.JooqReactiveService;
import com.linkbit.android.entity.UserModel;
import io.reactivex.Completable;
import io.reactivex.Single;
import java.util.List;


public class FriendService extends JooqReactiveService implements FriendUsecase {

  private final FriendServiceImpl impl;

  public FriendService() {
    this.impl = new FriendServiceImpl();
  }

  @Override
  public Single<List<UserModel>> loadFriends(String uid) {
    return createSingle(ctx -> impl.loadFriends(ctx, uid));
  }

  @Override
  public Single<UserModel> getUser(String uid) {
    return createSingle(ctx -> impl.getUser(ctx, uid));
  }

  @Override
  public Completable addFriend(String ownUid, String uid) {
    return createCompletable(ctx -> impl.addFriend(ctx, ownUid, uid));
  }

  @Override
  public Completable deleteFriend(String ownUid, String uid) {
    return createCompletable(ctx -> impl.deleteFriend(ctx, ownUid, uid));
  }

  @Override
  public Single<List<UserModel>> serachUsers(String text) {
    return createSingle(ctx -> impl.searchUser(ctx, text));
  }
}
