package com.starting.domain.friends.service;

import com.starting.domain.friends.dto.FriendListResponseDto;
import com.starting.domain.friends.entity.Friend;
import com.starting.domain.friends.entity.FriendStatus;
import com.starting.domain.friends.repository.FriendRepository;
import com.starting.domain.member.entity.UserMember;
import com.starting.domain.member.repository.UserMemberRepository;
import com.starting.test.TestUserMemberFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class FriendServiceTest {

    @Mock
    UserMemberRepository userMemberRepository;
    @Mock
    FriendRepository friendRepository;
    @InjectMocks
    FriendService friendService;

    UserMember member1;
    UserMember member2;
    UserMember member3;

    Long userMemberId1;
    Long userMemberId2;
    Long userMemberId3;

    @BeforeEach
    void setUp() {
        member1 = TestUserMemberFactory.create();
        member2 = TestUserMemberFactory.create();
        member3 = TestUserMemberFactory.create();

        userMemberId1 = 1L;
        userMemberId2 = 2L;
        userMemberId3 = 3L;

        ReflectionTestUtils.setField(member1, "id", userMemberId1);
        ReflectionTestUtils.setField(member2, "id", userMemberId2);
        ReflectionTestUtils.setField(member3, "id", userMemberId3);
    }
    
    @Test
    void 친구추가_처음요청() {
        given(userMemberRepository.findById(userMemberId1)).willReturn(Optional.ofNullable(member1));
        given(userMemberRepository.findByNickName("userB")).willReturn(Optional.ofNullable(member2));
        given(friendRepository.existsByUserMemberAndFriendId(member1, member2.getId())).willReturn(false);

        friendService.requestFriend(member1.getId(), "userB");

        Friend friend1 = member1.getFriends().get(0);
        Friend friend2 = member2.getFriends().get(0);

        assertThat(friend1.getFriendId()).isEqualTo(member2.getId());
        assertThat(friend1.getFriendStatus()).isEqualTo(FriendStatus.REQUEST);

        assertThat(friend2.getFriendId()).isEqualTo(member1.getId());
        assertThat(friend2.getFriendStatus()).isEqualTo(FriendStatus.RESPONSE);
    }

    @Test
    void 친구추가_재요청() {
        addFriend(member1, member2, FriendStatus.REJECT);
        addFriend(member2, member1, FriendStatus.REJECT);

        given(userMemberRepository.findById(member1.getId())).willReturn(Optional.ofNullable(member1));
        given(userMemberRepository.findByNickName("userB")).willReturn(Optional.ofNullable(member2));

        given(friendRepository.existsByUserMemberAndFriendId(member1, member2.getId())).willReturn(true);
        given(friendRepository.findByUserMemberAndFriendId(member1, member2.getId()))
                .willReturn(Optional.ofNullable(member1.getFriends().get(0)));
        given(friendRepository.findByUserMemberAndFriendId(member2, member1.getId()))
                .willReturn(Optional.ofNullable(member2.getFriends().get(0)));

        friendService.requestFriend(member1.getId(), "userB");

        Friend friend1 = member1.getFriends().get(0);
        Friend friend2 = member2.getFriends().get(0);

        assertThat(friend1.getFriendId()).isEqualTo(member2.getId());
        assertThat(friend1.getFriendStatus()).isEqualTo(FriendStatus.REQUEST);

        assertThat(friend2.getFriendId()).isEqualTo(member1.getId());
        assertThat(friend2.getFriendStatus()).isEqualTo(FriendStatus.RESPONSE);
    }

    @Test
    void 친구요청_수락() {
        addFriend(member1, member2, FriendStatus.REQUEST);
        addFriend(member2, member1, FriendStatus.RESPONSE);

        given(userMemberRepository.findById(member2.getId())).willReturn(Optional.ofNullable(member2));
        given(userMemberRepository.findByNickName("userA")).willReturn(Optional.ofNullable(member1));

        given(friendRepository.findByUserMemberAndFriendId(member1, member2.getId()))
                .willReturn(Optional.ofNullable(member1.getFriends().get(0)));
        given(friendRepository.findByUserMemberAndFriendId(member2, member1.getId()))
                .willReturn(Optional.ofNullable(member2.getFriends().get(0)));

        friendService.acceptFriend(member2.getId(), "userA");

        Friend friend1 = member1.getFriends().get(0);
        Friend friend2 = member2.getFriends().get(0);

        assertThat(friend1.getFriendId()).isEqualTo(member2.getId());
        assertThat(friend1.getFriendStatus()).isEqualTo(FriendStatus.ACCEPT);

        assertThat(friend2.getFriendId()).isEqualTo(member1.getId());
        assertThat(friend2.getFriendStatus()).isEqualTo(FriendStatus.ACCEPT);
    }

    @Test
    void 친구요청_거절() {
        addFriend(member1, member2, FriendStatus.REQUEST);
        addFriend(member2, member1, FriendStatus.RESPONSE);

        given(userMemberRepository.findById(member2.getId())).willReturn(Optional.ofNullable(member2));
        given(userMemberRepository.findByNickName("userA")).willReturn(Optional.ofNullable(member1));

        given(friendRepository.findByUserMemberAndFriendId(member1, member2.getId()))
                .willReturn(Optional.ofNullable(member1.getFriends().get(0)));
        given(friendRepository.findByUserMemberAndFriendId(member2, member1.getId()))
                .willReturn(Optional.ofNullable(member2.getFriends().get(0)));

        friendService.rejectFriend(member2.getId(), "userA");

        Friend friend1 = member1.getFriends().get(0);
        Friend friend2 = member2.getFriends().get(0);

        assertThat(friend1.getFriendId()).isEqualTo(member2.getId());
        assertThat(friend1.getFriendStatus()).isEqualTo(FriendStatus.REJECT);

        assertThat(friend2.getFriendId()).isEqualTo(member1.getId());
        assertThat(friend2.getFriendStatus()).isEqualTo(FriendStatus.REJECT);
    }

    @Test
    void 친구요청_삭제() {
        addFriend(member1, member2, FriendStatus.ACCEPT);
        addFriend(member2, member1, FriendStatus.ACCEPT);

        given(userMemberRepository.findById(member2.getId())).willReturn(Optional.ofNullable(member2));
        given(userMemberRepository.findByNickName("userA")).willReturn(Optional.ofNullable(member1));

        given(friendRepository.findByUserMemberAndFriendId(member1, member2.getId()))
                .willReturn(Optional.ofNullable(member1.getFriends().get(0)));
        given(friendRepository.findByUserMemberAndFriendId(member2, member1.getId()))
                .willReturn(Optional.ofNullable(member2.getFriends().get(0)));

        friendService.deleteFriend(member2.getId(), "userA");

        Friend friend1 = member1.getFriends().get(0);
        Friend friend2 = member2.getFriends().get(0);

        assertThat(friend1.getFriendId()).isEqualTo(member2.getId());
        assertThat(friend1.getFriendStatus()).isEqualTo(FriendStatus.DELETE);

        assertThat(friend2.getFriendId()).isEqualTo(member1.getId());
        assertThat(friend2.getFriendStatus()).isEqualTo(FriendStatus.DELETE);
    }

    @Test
    void 친구리스트_조회() {
        addFriend(member1, member2, FriendStatus.ACCEPT);
        addFriend(member1, member3, FriendStatus.ACCEPT);
        addFriend(member2, member1, FriendStatus.ACCEPT);
        addFriend(member3, member1, FriendStatus.ACCEPT);

        given(userMemberRepository.findById(member1.getId())).willReturn(Optional.ofNullable(member1));
        given(friendRepository.findAllByUserMember(member1)).willReturn(member1.getFriends());
        given(userMemberRepository.findNicknamesByIdList(mapFriendsToIdList(member1.getFriends())))
                .willReturn(Arrays.asList("userB", "userC"));

        List<FriendListResponseDto.FriendResponseDto> friendsList = friendService.getFriendsList(member1.getId()).getResult();

        List<Long> ids = Arrays.asList(member2.getId(), member3.getId());
        List<String> nicknames = Arrays.asList("userB", "userC");
        int idx = 0;
        for (FriendListResponseDto.FriendResponseDto friendResponseDto : friendsList) {
            assertThat(friendResponseDto.getFriendId()).isEqualTo(ids.get(idx));
            assertThat(friendResponseDto.getFriendStatus()).isEqualTo(FriendStatus.ACCEPT);
            assertThat(friendResponseDto.getNickname()).isEqualTo(nicknames.get(idx++));
        }
    }

    private List<Long> mapFriendsToIdList(List<Friend> friends) {
        List<Long> idList = friends.stream()
                .map(Friend::getFriendId)
                .collect(Collectors.toList());
        return idList;
    }

    private void addFriend(UserMember toMember, UserMember fromMember, FriendStatus friendStatus) {
        toMember.getFriends().add(Friend.builder()
                .userMember(fromMember)
                .friendId(fromMember.getId())
                .friendStatus(friendStatus)
                .build());
    }
}