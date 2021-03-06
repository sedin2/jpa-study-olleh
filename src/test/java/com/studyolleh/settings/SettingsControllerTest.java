package com.studyolleh.settings;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyolleh.WithAccount;
import com.studyolleh.account.AccountRepository;
import com.studyolleh.account.AccountService;
import com.studyolleh.domain.Account;
import com.studyolleh.domain.Tag;
import com.studyolleh.domain.Zone;
import com.studyolleh.settings.form.TagForm;
import com.studyolleh.settings.form.ZoneForm;
import com.studyolleh.tag.TagRepository;
import com.studyolleh.zone.ZoneRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SettingsControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AccountService accountService;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    TagRepository tagRepository;

    @Autowired
    ZoneRepository zoneRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    ObjectMapper objectMapper;

    private Zone testZone = Zone.builder().city("Doremi").localNameOfCity("도레미").province("doremi").build();

    @BeforeEach
    void beforeEach() {
        zoneRepository.save(testZone);
    }

    @AfterEach
    void afterEach() {
        accountRepository.deleteAll();
        zoneRepository.deleteAll();
    }

    @WithAccount("sedin")
    @DisplayName("프로필 수정 폼")
    @Test
    void updateProfileForm() throws Exception {
        mockMvc.perform(get(SettingsController.SETTINGS_PROFILE_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"));
    }

    @WithAccount("sedin")
    @DisplayName("프로필 수정 하기 - 입력값 정상")
    @Test
    void updateProfile() throws Exception {
        String bio = "짧은 소개를 수정 하는 경우";
        mockMvc.perform(post(SettingsController.SETTINGS_PROFILE_URL)
                .param("bio", bio)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SettingsController.SETTINGS_PROFILE_URL))
                .andExpect(flash().attributeExists("message"));

        Account account = accountRepository.findByNickname("sedin");
        assertEquals(bio, account.getBio());
    }

    @WithAccount("sedin")
    @DisplayName("프로필 수정 하기 - 입력값 에러")
    @Test
    void updateProfileWithError() throws Exception {
        String bio = "길게 소개를 수정 하는 경우. 길게 소개를 수정 하는 경우. 길게 소개를 수정 하는 경우. 길게 소개를 수정 하는 경우. 길게 소개를 수정 하는 경우. ";
        mockMvc.perform(post(SettingsController.SETTINGS_PROFILE_URL)
                .param("bio", bio)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.SETTINGS_PROFILE_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"))
                .andExpect(model().hasErrors());

        Account account = accountRepository.findByNickname("sedin");
        assertNull(account.getBio());
    }

    @WithAccount("sedin")
    @DisplayName("패스워드 수정 폼")
    @Test
    void updatePasswordForm() throws Exception {
        mockMvc.perform(get(SettingsController.SETTINGS_PASSWORD_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("passwordForm"));
    }

    @WithAccount("sedin")
    @DisplayName("패스워드 수정 - 입력값 정상")
    @Test
    void updatePasswordSuccess() throws  Exception {
        mockMvc.perform(post(SettingsController.SETTINGS_PASSWORD_URL)
                .param("newPassword", "12345678")
                .param("newPasswordConfirm", "12345678")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SettingsController.SETTINGS_PASSWORD_URL))
                .andExpect(flash().attributeExists("message"));

        Account account = accountRepository.findByNickname("sedin");
        assertTrue(passwordEncoder.matches("12345678", account.getPassword()));
    }

    @WithAccount("sedin")
    @DisplayName("패스워드 수정 - 입력값 에러 - 패스워드 불일치")
    @Test
    void updatePasswordFail() throws  Exception {
        mockMvc.perform(post(SettingsController.SETTINGS_PASSWORD_URL)
                .param("newPassword", "12345678")
                .param("newPasswordConfirm", "87654321")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.SETTINGS_PASSWORD_VIEW_NAME))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("passwordForm"));
    }

    @WithAccount("sedin")
    @DisplayName("알림 수정 폼")
    @Test
    void updateNotificationsForm() throws Exception {
        mockMvc.perform(get(SettingsController.SETTINGS_NOTIFICATION_URL))
               .andExpect(status().isOk())
               .andExpect(model().attributeExists("account"))
               .andExpect(model().attributeExists("notifications"));
    }

    @WithAccount("sedin")
    @DisplayName("알림 수정 - 입력값 정상")
    @Test
    void updateNotificationsSuccess() throws Exception {
        mockMvc.perform(post(SettingsController.SETTINGS_NOTIFICATION_URL)
               .param("studyCreatedByEmail", String.valueOf(true))
               .param("studyCreatedByWeb", String.valueOf(true))
               .param("studyEnrollmentResultByEmail", String.valueOf(true))
               .param("studyEnrollmentResultByWeb", String.valueOf(true))
               .param("studyUpdatedByEmail", String.valueOf(true))
               .param("studyUpdatedByWeb", String.valueOf(true))
               .with(csrf()))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl(SettingsController.SETTINGS_NOTIFICATION_URL))
               .andExpect(flash().attributeExists("message"));

        Account account = accountRepository.findByNickname("sedin");
        assertTrue(account.isStudyCreatedByEmail());
        assertTrue(account.isStudyCreatedByWeb());
        assertTrue(account.isStudyEnrollmentResultByEmail());
        assertTrue(account.isStudyEnrollmentResultByWeb());
        assertTrue(account.isStudyUpdatedByEmail());
        assertTrue(account.isStudyUpdatedByWeb());
    }

    @WithAccount("sedin")
    @DisplayName("알림 수정 - 입력값 에러 - boolean 값이 아닌 값이 들어 올 때")
    @Test
    void updateNotificationsFail() throws Exception {
        mockMvc.perform(post(SettingsController.SETTINGS_NOTIFICATION_URL)
                .param("studyCreatedByEmail", "asdf")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.SETTINGS_NOTIFICATION_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("notifications"))
                .andExpect(model().hasErrors());
    }

    @WithAccount("sedin")
    @DisplayName("닉네임 수정 폼")
    @Test
    void updateNicknameForm() throws Exception {
        mockMvc.perform(get(SettingsController.SETTINGS_ACCOUNT_URL))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.SETTINGS_ACCOUNT_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("nicknameForm"));
    }

    @WithAccount("sedin")
    @DisplayName("닉네임 수정 - 입력값 정상")
    @Test
    void updateNicknameSuccess() throws Exception {
        String newNickname = "mocha";
        mockMvc.perform(post(SettingsController.SETTINGS_ACCOUNT_URL)
                .param("nickname", newNickname)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SettingsController.SETTINGS_ACCOUNT_URL))
                .andExpect(flash().attributeExists("message"));

        assertNotNull(accountRepository.findByNickname(newNickname));
    }

    @WithAccount("sedin")
    @DisplayName("닉네임 수정 - 입력값 에러 - ")
    @Test
    void updateNicknameFail() throws Exception {
        String newNickname = "¯\\_(ツ)_/¯";
        mockMvc.perform(post(SettingsController.SETTINGS_ACCOUNT_URL)
                .param("nickname", newNickname)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.SETTINGS_ACCOUNT_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("nicknameForm"))
                .andExpect(model().hasErrors());
    }

    @WithAccount("sedin")
    @DisplayName("태그 수정 폼")
    @Test
    void updateTagsForm() throws Exception {
        mockMvc.perform(get(SettingsController.SETTINGS_TAGS_URL))
                .andExpect(view().name(SettingsController.SETTINGS_TAGS_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("tags"))
                .andExpect(model().attributeExists("whitelist"));
    }

    @WithAccount("sedin")
    @DisplayName("계정에 태그 추가")
    @Test
    void addTag() throws Exception {
        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");

        mockMvc.perform(post(SettingsController.SETTINGS_TAGS_URL + "/add")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(tagForm))
                       .with(csrf()))
                .andExpect(status().isOk());
        Optional<Tag> newTag = tagRepository.findByTitle("newTag");
        assertNotNull(newTag);
        assertTrue(accountRepository.findByNickname("sedin").getTags().contains(newTag.get()));
    }

    @WithAccount("sedin")
    @DisplayName("계정에 태그 삭제 - 입력값 정상")
    @Test
    void removeTagWithSuccess() throws Exception {
        Account loginUser = accountRepository.findByNickname("sedin");
        Tag newTag = tagRepository.save(Tag.builder().title("newTag").build());
        accountService.addTag(loginUser, newTag);

        assertTrue(loginUser.getTags().contains(newTag));

        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");

        mockMvc.perform(post(SettingsController.SETTINGS_TAGS_URL + "/remove")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagForm))
                .with(csrf()))
                .andExpect(status().isOk());
        assertFalse(loginUser.getTags().contains(newTag));
    }

    @WithAccount("sedin")
    @DisplayName("계정에 태그 삭제 - 입력값 에러")
    @Test
    void removeTagWithFail() throws Exception {
        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");

        mockMvc.perform(post(SettingsController.SETTINGS_TAGS_URL + "/remove")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(tagForm))
                       .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @WithAccount("sedin")
    @DisplayName("지역 수정 폼")
    @Test
    void updateZonesForm() throws Exception {
        mockMvc.perform(get(SettingsController.SETTINGS_ZONES_URL))
                .andExpect(view().name(SettingsController.SETTINGS_ZONES_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("zones"))
                .andExpect(model().attributeExists("whitelist"));
    }

    @WithAccount("sedin")
    @DisplayName("지역 추가")
    @Test
    void addZone() throws Exception {
        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());
        mockMvc.perform(post(SettingsController.SETTINGS_ZONES_URL + "/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(zoneForm))
                        .with(csrf()))
                .andExpect(status().isOk());
        Account loginUser = accountRepository.findByNickname("sedin");
        Optional<Zone> zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());
        assertTrue(loginUser.getZones().contains(zone.get()));
    }

    @WithAccount("sedin")
    @DisplayName("지역 삭제 - 입력값 정상")
    @Test
    void removeZoneWithSuccess() throws Exception {
        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());
        mockMvc.perform(post(SettingsController.SETTINGS_ZONES_URL + "/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(zoneForm))
                        .with(csrf()))
                .andExpect(status().isOk());
        Account loginUser = accountRepository.findByNickname("sedin");
        Optional<Zone> zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());
        assertFalse(loginUser.getZones().contains(zone.get()));
    }

    @WithAccount("sedin")
    @DisplayName("지역 삭제 - 입력값 에러")
    @Test
    void removeZoneWithFail() throws Exception {
        mockMvc.perform(post(SettingsController.SETTINGS_ZONES_URL + "/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("zoneName: Invalid Zone Name")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }
}
