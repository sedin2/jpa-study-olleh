package com.studyolleh.study;

import com.studyolleh.WithAccount;
import com.studyolleh.account.AccountRepository;
import com.studyolleh.domain.Account;
import com.studyolleh.domain.Study;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class StudyControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    StudyService studyService;

    @Autowired
    StudyRepository studyRepository;

    @Autowired
    AccountRepository accountRepository;

    @AfterEach
    void afterEach() {
        accountRepository.deleteAll();
    }

    @WithAccount("sedin")
    @DisplayName("스터디 추가 폼")
    @Test
    void newStudyForm() throws Exception {
        mockMvc.perform(get("/new-study"))
               .andExpect(status().isOk())
               .andExpect(view().name("study/form"))
               .andExpect(model().attributeExists("account"))
               .andExpect(model().attributeExists("studyForm"));
    }

    @WithAccount("sedin")
    @DisplayName("스터디 추가 - 입력값 정상")
    @Test
    void addNewStudyWithSuccess() throws Exception {
        mockMvc.perform(post("/new-study")
               .param("path", "test-path")
               .param("title", "study title")
               .param("shortDescription", "short")
               .param("fullDescription", "full")
               .with(csrf()))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/study/test-path"));

        Study study = studyRepository.findByPath("test-path");
        assertNotNull(study);
        Account account = accountRepository.findByNickname("sedin");
        assertTrue(study.getManagers().contains(account));
    }

    @WithAccount("sedin")
    @DisplayName("스터디 추가 - 입력값 에러")
    @Test
    void addNewStudyWithFail() throws Exception {
        mockMvc.perform(post("/new-study")
               .param("path", "a")
               .param("title", "study title")
               .param("shortDescription", "short")
               .param("fullDescription", "full")
               .with(csrf()))
               .andExpect(status().isOk())
               .andExpect(view().name("study/form"))
               .andExpect(model().hasErrors())
               .andExpect(model().attributeExists("account"))
               .andExpect(model().attributeExists("studyForm"));

        Study study = studyRepository.findByPath("a");
        assertNull(study);
    }

    @WithAccount("sedin")
    @DisplayName("스터디 조회")
    @Test
    void viewStudy() throws Exception {
        Study study = new Study();
        study.setPath("test-path");
        study.setTitle("test study");
        study.setShortDescription("short description");
        study.setFullDescription("<p>full description</p>");

        Account account = accountRepository.findByNickname("sedin");
        studyService.createNewStudy(study, account);

        mockMvc.perform(get("/study/test-path"))
               .andExpect(status().isOk())
               .andExpect(view().name("study/view"))
               .andExpect(model().attributeExists("account"))
               .andExpect(model().attributeExists("study"));
    }

    @WithAccount("sedin")
    @DisplayName("스터디 조회 - 멤버")
    @Test
    void viewStudyMembers() throws Exception {
        Study study = new Study();
        study.setPath("test-path");
        study.setTitle("test study");
        study.setShortDescription("short description");
        study.setFullDescription("<p>full description</p>");

        Account account = accountRepository.findByNickname("sedin");
        studyService.createNewStudy(study, account);

        mockMvc.perform(get("/study/test-path/members"))
                .andExpect(status().isOk())
                .andExpect(view().name("study/members"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("study"));

        Study createdStudy = studyRepository.findByPath("test-path");
        assertTrue(createdStudy.getManagers().contains(account));
    }

}