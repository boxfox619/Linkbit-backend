package com.boxfox.cross.service.friend;

import com.linkbit.android.entity.UserModel;
import io.reactivex.Completable;
import io.reactivex.Single;
import java.util.List;

public interface FriendUsecase {

  Single<List<UserModel>> loadFriends(String uid);

  Single<UserModel> getUser(String uid);

  Completable addFriend(String ownUid, String uid);

  Completable deleteFriend(String ownUid, String uid);

  Single<List<UserModel>> serachUsers(String text);
}
