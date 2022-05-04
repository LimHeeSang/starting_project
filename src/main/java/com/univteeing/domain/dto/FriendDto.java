package com.univteeing.domain.dto;


import com.univteeing.domain.freinds.entity.Friend;
import com.univteeing.domain.freinds.entity.FriendStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class FriendDto {

    private Long friendId;

    private String nickName;

    private FriendStatus friendStatus;

    public FriendDto(Friend friend) {
        this.friendId = friend.getFriendId();
        this.friendStatus = friend.getFriendsStatus();
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }
}