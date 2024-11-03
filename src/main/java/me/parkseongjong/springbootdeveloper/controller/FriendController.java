package me.parkseongjong.springbootdeveloper.controller;

import me.parkseongjong.springbootdeveloper.domain.FriendRequest;
import me.parkseongjong.springbootdeveloper.service.FriendService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import me.parkseongjong.springbootdeveloper.domain.User;

import java.util.List;

@RestController
public class FriendController {

    @Autowired
    private FriendService friendService;

    @GetMapping("/api/friends")
    public ResponseEntity<?> getFriends(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        List<FriendRequest> friends = friendService.getAcceptedFriends(user.getId());
        List<User> friendList = friends.stream()
                .map(friendRequest -> {
                    if (friendRequest.getSender().getId().equals(user.getId())) {
                        return friendRequest.getReceiver();
                    } else {
                        return friendRequest.getSender();
                    }
                })
                .toList();

        return ResponseEntity.ok(friendList);
    }

}