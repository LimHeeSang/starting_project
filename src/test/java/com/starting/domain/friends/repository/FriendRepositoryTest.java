package com.starting.domain.friends.repository;

import com.starting.domain.friends.entity.Friend;
import com.starting.domain.friends.entity.FriendStatus;
import com.starting.domain.member.entity.UserMember;
import com.starting.domain.member.repository.UserMemberRepository;
import com.starting.test.TestUserMemberFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class FriendRepositoryTest {

    @Autowired
    private FriendRepository friendRepository;
    @Autowired
    private UserMemberRepository userMemberRepository;

    UserMember userMember1;
    UserMember userMember2;
    UserMember userMember3;
    Friend friend1;
    Friend friend2;

    @BeforeEach
    void setUp() {
        userMember1 = TestUserMemberFactory.create();
        userMember2 = TestUserMemberFactory.create();
        userMember3 = TestUserMemberFactory.create();

        userMemberRepository.save(userMember1);
        userMemberRepository.save(userMember2);
        userMemberRepository.save(userMember3);

        friend1 = Friend.builder().userMember(userMember1)
                .friendId(userMember2.getId())
                .friendStatus(FriendStatus.REQUEST)
                .build();
        friend2 = Friend.builder().userMember(userMember1)
                .friendId(userMember3.getId())
                .friendStatus(FriendStatus.REQUEST)
                .build();

        friendRepository.save(friend1);
        friendRepository.save(friend2);
    }

    @Test
    void findByUserMemberAndFriendId() {
        Friend findFriend = friendRepository.findByUserMemberAndFriendId(userMember1, userMember2.getId()).get();

        assertThat(findFriend.getUserMember()).isEqualTo(userMember1);
        assertThat(findFriend.getFriendId()).isEqualTo(userMember2.getId());
    }

    @Test
    void existsByUserMemberAndFriendId() {
        boolean result = friendRepository.existsByUserMemberAndFriendId(userMember1, userMember2.getId());
        assertThat(result).isTrue();
    }

    @Test
    void findAllByUserMember() {
        List<Friend> friends = friendRepository.findAllByUserMember(userMember1);
        assertThat(friends).contains(friend1, friend2);
    }

    @Test
    void findAllByUserMemberAndFriendStatus() {
        List<Friend> friends = friendRepository.findAllByUserMemberAndFriendStatus(userMember1, FriendStatus.REQUEST);
        assertThat(friends).contains(friend1, friend2);
    }
}