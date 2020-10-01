package com.studyolleh.account;

import com.studyolleh.domain.Account;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @MockBean
    JavaMailSender javaMailSender;

    @DisplayName("회원 가입 화면 보이는지 테스트")
    @Test
    void signUpForm() throws Exception {
        mockMvc.perform(get("/sign-up"))
               .andExpect(status().isOk())
               .andExpect(view().name("account/sign-up"))
               .andExpect(model().attributeExists("signUpForm"));
    }

    @DisplayName("회원 가입 처리 - 입력값 오류")
    @Test
    void signUpSubmit_with_wrong_input() throws Exception {
        mockMvc.perform(post("/sign-up")
                        .param("nickname", "sedin")
                        .param("email", "email,,,,,")
                        .param("password", "12345")
                        .with(csrf()))
                        .andExpect(status().isOk())
                        .andExpect(view().name("account/sign-up"));
    }

    @DisplayName("회원 가입 처리 - 입력값 정상")
    @Test
    void signUpSubmit_with_correct_input() throws Exception {
        mockMvc.perform(post("/sign-up")
                        .param("nickname", "sedin")
                        .param("email", "sedin@kakao.com")
                        .param("password", "12345678")
                        .with(csrf()))
                        .andExpect(status().is3xxRedirection())
                        .andExpect(view().name("redirect:/"));

        Account account = accountRepository.findByEmail("sedin@kakao.com");
        assertNotNull(account);
        assertNotEquals(account.getPassword(), "12345678");

        assertTrue(accountRepository.existsByEmail("sedin@kakao.com"));
        then(javaMailSender).should().send(any(SimpleMailMessage.class));
    }
}