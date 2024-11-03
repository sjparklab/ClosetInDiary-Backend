package me.parkseongjong.springbootdeveloper.repository;

import me.parkseongjong.springbootdeveloper.domain.FriendRequest;
import me.parkseongjong.springbootdeveloper.domain.FriendRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
    Optional<List<FriendRequest>> findByReceiverIdAndStatus(Long receiverId, FriendRequestStatus status);
    Optional<List<FriendRequest>> findBySenderIdAndStatus(Long senderId, FriendRequestStatus status);
}
