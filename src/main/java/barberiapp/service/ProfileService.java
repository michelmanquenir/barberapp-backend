package barberiapp.service;

import barberiapp.model.Profile;
import barberiapp.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;

    public Profile getProfile(String userId) {
        return profileRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));
    }

    public Profile updateProfile(String userId, Profile updateData) {
        Profile existingProfile = getProfile(userId);

        existingProfile.setFullName(updateData.getFullName());
        existingProfile.setPhone(updateData.getPhone());
        existingProfile.setAddress(updateData.getAddress());
        existingProfile.setBirthdate(updateData.getBirthdate());

        return profileRepository.save(existingProfile);
    }
}
