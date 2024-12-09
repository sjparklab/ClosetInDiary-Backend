package me.parkseongjong.springbootdeveloper.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import me.parkseongjong.springbootdeveloper.domain.FriendRequest;
import me.parkseongjong.springbootdeveloper.domain.FriendRequestStatus;
import me.parkseongjong.springbootdeveloper.domain.User;
import me.parkseongjong.springbootdeveloper.repository.FriendRequestRepository;
import me.parkseongjong.springbootdeveloper.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FriendRequestService {

    private final FriendRequestRepository friendRequestRepository;
    private final UserRepository userRepository;

    public void sendFriendRequest(Long senderId, Long receiverId) {
        boolean requestExists = friendRequestRepository.existsBySenderIdAndReceiverIdAndStatus(senderId, receiverId, FriendRequestStatus.PENDING);
        boolean alreadyFriends = friendRequestRepository.existsBySenderIdAndReceiverIdAndStatus(senderId, receiverId, FriendRequestStatus.ACCEPTED) ||
                friendRequestRepository.existsBySenderIdAndReceiverIdAndStatus(receiverId, senderId, FriendRequestStatus.ACCEPTED);

        if (requestExists) {
            throw new IllegalArgumentException("이미 보낸 친구 요청이 있습니다.");
        }

        if (alreadyFriends) {
            throw new IllegalArgumentException("이미 친구입니다.");
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new UsernameNotFoundException("Sender not found"));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new UsernameNotFoundException("Receiver not found"));

        FriendRequest request = new FriendRequest();
        request.setSender(sender);
        request.setReceiver(receiver);
        request.setStatus(FriendRequestStatus.PENDING);

        friendRequestRepository.save(request);
    }

    @Transactional
    public boolean acceptFriendRequest(Long userId, Long requestId) {
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        if (request.getReceiver().getId().equals(userId)) {
            request.setStatus(FriendRequestStatus.ACCEPTED);
            friendRequestRepository.save(request);
            return true;
        }
        return false;
    }

    @Transactional
    public boolean declineFriendRequest(Long userId, Long requestId) {
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        if (request.getReceiver().getId().equals(userId)) {
            request.setStatus(FriendRequestStatus.DECLINED);
            friendRequestRepository.save(request);
            return true;
        }
        return false;
    }

    public List<FriendRequest> getReceivedFriendRequests(Long userId) {
        return friendRequestRepository.findByReceiverIdAndStatus(userId, FriendRequestStatus.PENDING).orElse(List.of());
    }

    public List<FriendRequest> getSentFriendRequests(Long userId) {
        return friendRequestRepository.findBySenderIdAndStatus(userId, FriendRequestStatus.PENDING).orElse(List.of());
    }

    public void sendFriendRequestById(Long senderId, Long receiverId) {
        sendFriendRequest(senderId, receiverId);
    }

    public void sendFriendRequestByUsername(Long senderId, String username) {
        User receiver = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found with username: " + username));
        sendFriendRequest(senderId, receiver.getId());
    }

    public void sendFriendRequestByEmail(Long senderId, String email) {
        User receiver = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));
        sendFriendRequest(senderId, receiver.getId());
    }
}
