package com.studyolleh.study;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyolleh.account.CurrentUser;
import com.studyolleh.domain.Account;
import com.studyolleh.domain.Study;
import com.studyolleh.domain.Tag;
import com.studyolleh.domain.Zone;
import com.studyolleh.settings.form.TagForm;
import com.studyolleh.settings.form.ZoneForm;
import com.studyolleh.study.form.StudyDescriptionForm;
import com.studyolleh.tag.TagRepository;
import com.studyolleh.tag.TagService;
import com.studyolleh.zone.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/study/{path}/settings")
@RequiredArgsConstructor
public class StudySettingsController {

    private final StudyService studyService;
    private final TagService tagService;
    private final TagRepository tagRepository;
    private final ZoneRepository zoneRepository;
    private final ModelMapper modelMapper;
    private final ObjectMapper objectMapper;

    @GetMapping("/description")
    public String viewStudySetting(@CurrentUser Account account, @PathVariable String path, Model model) {
        Study study = studyService.getStudyToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(study);
        model.addAttribute(modelMapper.map(study, StudyDescriptionForm.class));
        return "study/settings/description";
    }

    @PostMapping("/description")
    public String updateStudyInfo(@CurrentUser Account account, @PathVariable String path,
                                  @Valid StudyDescriptionForm studyDescriptionForm, Errors errors,
                                  Model model, RedirectAttributes attributes) throws UnsupportedEncodingException {
        Study study = studyService.getStudyToUpdate(account, path);

        if (errors.hasErrors()) {
            model.addAttribute(account);
            model.addAttribute(study);
            return "study/settings/description";
        }
        studyService.updateStudyDescription(study, studyDescriptionForm);
        attributes.addFlashAttribute("message", "스터디 소개를 수정했습니다.");
        return "redirect:/study/" + getPath(path) + "/settings/description";
    }

    @GetMapping("/banner")
    public String studyImageForm(@CurrentUser Account account, @PathVariable String path, Model model) {
        model.addAttribute(account);
        model.addAttribute(studyService.getStudyToUpdate(account, path));
        return "study/settings/banner";
    }

    @PostMapping("/banner")
    public String studyImageSubmit(@CurrentUser Account account, @PathVariable String path,
                                   String image, RedirectAttributes attributes) throws UnsupportedEncodingException {

        Study study = studyService.getStudyToUpdate(account, path);
        studyService.updateStudyImage(study, image);
        attributes.addFlashAttribute("message", "스터디 배너를 수정했습니다.");
        return "redirect:/study/" + getPath(path) + "/settings/banner";
    }

    @PostMapping("/banner/enable")
    public String enableStudyBanner(@CurrentUser Account account, @PathVariable String path)
                                    throws UnsupportedEncodingException {
        studyService.enableStudyBanner(studyService.getStudyToUpdate(account, path));
        return "redirect:/study/" + getPath(path) + "/settings/banner";
    }

    @PostMapping("/banner/disable")
    public String disableStudyBanner(@CurrentUser Account account, @PathVariable String path)
                                     throws UnsupportedEncodingException {
        studyService.disableStudyBanner(studyService.getStudyToUpdate(account, path));
        return "redirect:/study/" + getPath(path) + "/settings/banner";
    }

    @GetMapping("/tags")
    public String studyTagsForm(@CurrentUser Account account,
                                @PathVariable String path, Model model) throws JsonProcessingException {
        Study study = studyService.getStudyToUpdate(account, path);
        List<String> allTagTitlesInStudy = studyService.getAllTags(study).stream()
                                                                         .map(Tag::getTitle)
                                                                         .collect(Collectors.toList());
        List<String> allTagTitles = tagRepository.findAll().stream()
                                                           .map(Tag::getTitle)
                                                           .collect(Collectors.toList());
        model.addAttribute(account);
        model.addAttribute(study);
        model.addAttribute("tags", allTagTitlesInStudy);
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allTagTitles));
        return "study/settings/tags";
    }

    @PostMapping("/tags/add")
    @ResponseBody
    public ResponseEntity addTagToStudy(@CurrentUser Account account, @PathVariable String path,
                                        @RequestBody TagForm tagForm) {

        Study study = studyService.getStudyToUpdateTag(account, path);
        Tag tag = tagService.findOrCreateNew(tagForm.getTagTitle());
        studyService.addTag(study, tag);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/tags/remove")
    @ResponseBody
    public ResponseEntity removeTagToStudy(@CurrentUser Account account, @PathVariable String path,
                                        @RequestBody TagForm tagForm) {
        Study study = studyService.getStudyToUpdateTag(account, path);
        return tagRepository.findByTitle(tagForm.getTagTitle())
                            .filter(tag -> study.getTags().contains(tag))
                            .map(tag -> {
                                            studyService.removeTag(study, tag);
                                            return ResponseEntity.ok().build();
                            })
                            .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @GetMapping("/zones")
    public String studyZonesForm(@CurrentUser Account account,
                                 @PathVariable String path, Model model) throws JsonProcessingException {
        Study study = studyService.getStudyToUpdateZone(account, path);
        List<String> allZonesInStudy = study.getZones().stream()
                                                       .map(Zone::toString)
                                                       .collect(Collectors.toList());
        List<String> allZones = zoneRepository.findAll().stream()
                                                        .map(Zone::toString)
                                                        .collect(Collectors.toList());
        model.addAttribute(account);
        model.addAttribute(study);
        model.addAttribute("zones", allZonesInStudy);
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allZones));
        return "study/settings/zones";
    }

    @PostMapping("/zones/add")
    @ResponseBody
    public ResponseEntity addZone(@CurrentUser Account account, @PathVariable String path,
                                  @RequestBody ZoneForm zoneForm) {
        Study study = studyService.getStudyToUpdateZone(account, path);
        return zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName())
                             .map(zone -> {
                                 studyService.addZone(study, zone);
                                 return ResponseEntity.ok().build();
                             })
                             .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @PostMapping("/zones/remove")
    @ResponseBody
    public ResponseEntity removeZone(@CurrentUser Account account, @PathVariable String path,
                                     @RequestBody ZoneForm zoneForm) {
        Study study = studyService.getStudyToUpdateZone(account, path);
        return zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName())
                             .map(zone -> {
                                 studyService.removeZone(study, zone);
                                 return ResponseEntity.ok().build();
                             })
                             .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @GetMapping("/study")
    public String studySettingsForm(@CurrentUser Account account, @PathVariable String path, Model model) {
        model.addAttribute(account);
        model.addAttribute(studyService.getStudyToUpdate(account, path));
        return "study/settings/study";
    }

    @PostMapping("/study/publish")
    public String publishStudy(@CurrentUser Account account, @PathVariable String path,
                               RedirectAttributes attributes) throws UnsupportedEncodingException {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        studyService.publish(study);
        attributes.addFlashAttribute("message", "스터디를 공개했습니다.");
        return "redirect:/study/" + getPath(path) + "/settings/study";
    }

    @PostMapping("/study/close")
    public String closeStudy(@CurrentUser Account account, @PathVariable String path,
                             RedirectAttributes attributes) throws UnsupportedEncodingException {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        studyService.close(study);
        attributes.addFlashAttribute("message", "스터디를 종료했습니다.");
        return "redirect:/study/" + getPath(path) + "/settings/study";
    }

    @PostMapping("/recruit/start")
    public String startRecruit(@CurrentUser Account account, @PathVariable String path,
                               RedirectAttributes attributes) throws UnsupportedEncodingException {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        if (!study.canUpdateRecruiting()) {
            attributes.addFlashAttribute("message", "1시간 안에 인원 모집 설정을 여러번 변경할 수 없습니다.");
            return "redirect:/study/" + getPath(path) + "/settings/study";
        }
        studyService.startRecruit(study);
        attributes.addFlashAttribute("message", "인원 모집을 시작합니다.");
        return "redirect:/study/" + getPath(path) + "/settings/study";
    }

    @PostMapping("/recruit/stop")
    public String stopRecruit(@CurrentUser Account account, @PathVariable String path,
                              RedirectAttributes attributes) throws UnsupportedEncodingException {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        if (!study.canUpdateRecruiting()) {
            attributes.addFlashAttribute("message", "1시간 안에 인원 모집 설정을 여러번 변경할 수 없습니다.");
            return "redirect:/study/" + getPath(path) + "/settings/study";
        }
        studyService.stopRecruit(study);
        attributes.addFlashAttribute("message", "인원 모집을 종료합니다.");
        return "redirect:/study/" + getPath(path) + "/settings/study";
    }

    public String getPath(String path) throws UnsupportedEncodingException {
        return URLEncoder.encode(path, String.valueOf(StandardCharsets.UTF_8));
    }
}
