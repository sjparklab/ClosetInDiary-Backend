package me.parkseongjong.springbootdeveloper.service;

import me.parkseongjong.springbootdeveloper.domain.FriendRequest;
import me.parkseongjong.springbootdeveloper.domain.FriendRequestStatus;
import me.parkseongjong.springbootdeveloper.repository.FriendRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.Optional;

@Service
public class FriendService {
    @Autowired
    private FriendRequestRepository friendRequestRepository;

    public List<FriendRequest> getAcceptedFriends(Long userId) {
        List<FriendRequest> sentRequests = friendRequestRepository.findBySenderIdAndStatus(userId, FriendRequestStatus.ACCEPTED).orElseThrow(()-> new IllegalArgumentException("Not Found!"));
        List<FriendRequest> receivedRequests = friendRequestRepository.findByReceiverIdAndStatus(userId, FriendRequestStatus.ACCEPTED).orElseThrow(()-> new IllegalArgumentException("Not Found!"));

        // 두 목록을 합쳐서 반환하거나 필요한 방식으로 처리
        sentRequests.addAll(receivedRequests);
        return sentRequests;
    }
    public boolean deleteFriend(Long userId, Long friendId) {
        // 친구 요청이 현재 사용자와 특정 친구 간에 존재하고 수락 상태인지 확인
        Optional<FriendRequest> friendRequest = friendRequestRepository
                .findBySenderIdAndReceiverIdAndStatus(userId, friendId, FriendRequestStatus.ACCEPTED)
                .or(() -> friendRequestRepository.findBySenderIdAndReceiverIdAndStatus(friendId, userId, FriendRequestStatus.ACCEPTED));

        if (friendRequest.isPresent()) {
            // 친구 관계 삭제
            friendRequestRepository.delete(friendRequest.get());
            return true;
        } else {
            return false; // 친구 관계가 없거나 찾을 수 없음
        }
    }
}

