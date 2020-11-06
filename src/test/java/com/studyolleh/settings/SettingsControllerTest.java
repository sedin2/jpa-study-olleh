package com.studyolleh.settings;

import com.studyolleh.WithAccount;
import com.studyolleh.account.AccountRepository;
import com.studyolleh.account.AccountService;
import com.studyolleh.domain.Account;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SettingsControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AccountService accountService;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @AfterEach
    void afterEach() {
        accountRepository.deleteAll();
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

}