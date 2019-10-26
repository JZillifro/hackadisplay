package com.supplyframeproject.hackadisplay.controller;

import com.supplyframeproject.hackadisplay.model.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HackadisplayController {
    String clientID = "8oBvmZozI6op98W1u8qfWnBnsWwJ016M4AQvrHDCNgpIJDPZ";
    String clientSecret = "t0mqizevgcXrlJYAgzkiSrAWJMHlBfJgGT9ub6nmPyHsHytG";
    String apiKey = "D5xeZrMHcBiWkIXT";

    @GetMapping("/logIn")
    public RedirectView logIn(RedirectAttributes attributes) {
        attributes.addFlashAttribute("flashAttribute", "redirectWithRedirectView");
        attributes.addAttribute("attribute", "redirectWithRedirectView");
        return new RedirectView("https://hackaday.io/authorize?client_id=" + clientID + "&state=<optional>&response_type=code");
    }

    @RequestMapping("/")
    public String home() {
        return "home";
    }

    @RequestMapping("/setCookie")
    public String setCookie(HttpServletResponse response, @RequestParam String code) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://hackaday.io/access_token?client_id=" + clientID + "&client_secret=" + clientSecret + "&code=" + code + "&grant_type=authorization_code";
        AccessResponse accessResponse = restTemplate.getForObject(url, AccessResponse.class);
        String token = accessResponse.getAccess_token();
        // create a cookie
        Cookie cookie = new Cookie("hackadisplay_auth_token", token);
        //add cookie to response
        response.addCookie(cookie);
        return "cookieSuccess";
    }

    @RequestMapping("/projects")
    public ModelAndView projects(@CookieValue(value = "hackadisplay_auth_token", defaultValue = "") String token, @RequestParam(value = "page", required = false, defaultValue = "1") int page) {
        RestTemplate restTemplate = new RestTemplate();
        final HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + token);
        final HttpEntity<String> entity = new HttpEntity<String>(headers);
        StringBuilder urlBuilder = new StringBuilder("https://api.hackaday.io/v1/projects?per_page=48&api_key=");
        urlBuilder.append(apiKey);
        urlBuilder.append("&page=");
        urlBuilder.append(page);
        String url = urlBuilder.toString();

        ResponseEntity<ProjectSet> projectsResponse = restTemplate.exchange(url, HttpMethod.GET, entity, ProjectSet.class);
        ProjectSet projectPage = projectsResponse.getBody();

        urlBuilder = new StringBuilder("https://api.hackaday.io/v1/users/batch/");
        urlBuilder.append("?api_key=");
        urlBuilder.append(apiKey);
        urlBuilder.append("&ids=");
        for(Project project: projectPage.getProjects()) {
            urlBuilder.append(project.getOwner_id());
            urlBuilder.append(",");
        }
        url = urlBuilder.toString();

        ResponseEntity<UserBatch> usersResponse = restTemplate.exchange(url, HttpMethod.GET, entity, UserBatch.class);
        UserBatch userBatch = usersResponse.getBody();
        Map<Integer, User> userMap = new HashMap<>();
        for(User user: userBatch.getUsers()) {
            userMap.put(user.getId(), user);
        }
        User defUser = new User();
        defUser.setScreen_name("Not Found");
        for(Project project: projectPage.getProjects()) {
            project.setOwner(userMap.getOrDefault(project.getOwner_id(), defUser));
        }
        ModelAndView modelAndView = new ModelAndView("projects");
        modelAndView.addObject("projectPage", projectPage);
        return modelAndView;
    }

}
