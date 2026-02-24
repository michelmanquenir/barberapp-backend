package barberiapp.controller;

import barberiapp.model.Profile;
import barberiapp.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public Profile getProfile(@RequestParam String userId) {
        return profileService.getProfile(userId);
    }

    @PutMapping
    public Profile updateProfile(@RequestParam String userId, @RequestBody Profile profileData) {
        return profileService.updateProfile(userId, profileData);
    }
}
