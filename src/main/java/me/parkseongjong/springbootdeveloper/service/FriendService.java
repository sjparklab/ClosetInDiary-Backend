package me.parkseongjong.springbootdeveloper.service;

import me.parkseongjong.springbootdeveloper.domain.FriendRequest;
import me.parkseongjong.springbootdeveloper.domain.FriendRequestStatus;
import me.parkseongjong.springbootdeveloper.repository.FriendRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

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
}