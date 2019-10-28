package com.supplyframeproject.hackadisplay.controller;

import com.supplyframeproject.hackadisplay.model.*;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

@Controller
public class HackadisplayController {
//    these should be secret
    final String clientID = "8oBvmZozI6op98W1u8qfWnBnsWwJ016M4AQvrHDCNgpIJDPZ";
    final String clientSecret = "t0mqizevgcXrlJYAgzkiSrAWJMHlBfJgGT9ub6nmPyHsHytG";
    final String apiKey = "D5xeZrMHcBiWkIXT";

    @GetMapping("/logIn")
    public RedirectView logIn(RedirectAttributes attributes) {
        attributes.addFlashAttribute("flashAttribute", "redirectWithRedirectView");
        attributes.addAttribute("attribute", "redirectWithRedirectView");
        return new RedirectView("https://hackaday.io/authorize?client_id=" + clientID + "&state=<optional>&response_type=code");
    }

    @GetMapping("/error")
    public String error() { return "error"; }

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/setCookie")
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

    @Cacheable("projects")
    @GetMapping("/projects")
    public ModelAndView projects(@CookieValue(value = "hackadisplay_auth_token", defaultValue = "") String token, @RequestParam(value = "page", required = false, defaultValue = "1") int page) {
        RestTemplate restTemplate = new RestTemplate();
        final HttpHeaders headers = new HttpHeaders();
        StringBuilder authTokenBuilder = new StringBuilder("token ");
        authTokenBuilder.append(token);
        String authToken = authTokenBuilder.toString();
        headers.set("Authorization", authToken);
        final HttpEntity<String> entity = new HttpEntity<String>(headers);
        StringBuilder urlBuilder = new StringBuilder("https://api.hackaday.io/v1/projects?per_page=48&api_key=");
        urlBuilder.append(apiKey);
        urlBuilder.append("&page=");
        urlBuilder.append(page);
        String url = urlBuilder.toString();

        ResponseEntity<ProjectSet> projectsResponse = restTemplate.exchange(url, HttpMethod.GET, entity, ProjectSet.class);
        ProjectSet projectsPage = projectsResponse.getBody();

        urlBuilder = new StringBuilder("https://api.hackaday.io/v1/users/batch/");
        urlBuilder.append("?api_key=");
        urlBuilder.append(apiKey);
        urlBuilder.append("&ids=");
        for(Project project: projectsPage.getProjects()) {
            urlBuilder.append(project.getOwner_id());
            urlBuilder.append(",");
        }
        url = urlBuilder.toString();

        ResponseEntity<UserSet> usersResponse = restTemplate.exchange(url, HttpMethod.GET, entity, UserSet.class);
        UserSet userSet = usersResponse.getBody();
        Map<Integer, User> userMap = new HashMap<>();
        for(User user: userSet.getUsers()) {
            userMap.put(user.getId(), user);
        }
        User defUser = new User();
        defUser.setScreen_name("Not Found");
        for(Project project: projectsPage.getProjects()) {
            project.setOwner(userMap.getOrDefault(project.getOwner_id(), defUser));
        }
        ModelAndView modelAndView = new ModelAndView("projects");
        modelAndView.addObject("projectsPage", projectsPage);
        return modelAndView;
    }

    @GetMapping("/projects/{id}")
    public ModelAndView projectById(@CookieValue(value = "hackadisplay_auth_token", defaultValue = "") String token, @PathVariable String id) {
        RestTemplate restTemplate = new RestTemplate();
        final HttpHeaders headers = new HttpHeaders();
        StringBuilder authTokenBuilder = new StringBuilder("token ");
        authTokenBuilder.append(token);
        String authToken = authTokenBuilder.toString();
        headers.set("Authorization", authToken);
        final HttpEntity<String> entity = new HttpEntity<String>(headers);
        StringBuilder urlBuilder = new StringBuilder("https://api.hackaday.io/v1/projects/");
        urlBuilder.append(id);
        urlBuilder.append("?api_key=");
        urlBuilder.append(apiKey);
        String url = urlBuilder.toString();

        ResponseEntity<Project> projectResponse = restTemplate.exchange(url, HttpMethod.GET, entity, Project.class);
        Project projectPage = projectResponse.getBody();

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        projectPage.setCreatedFormatted(format.format(projectPage.getCreated()));
        projectPage.setUpdatedFormatted(format.format(projectPage.getUpdated()));

        urlBuilder = new StringBuilder("https://api.hackaday.io/v1/users/");
        int owner_id = projectPage.getOwner_id();
        urlBuilder.append(owner_id);
        urlBuilder.append("?api_key=");
        urlBuilder.append(apiKey);
        url = urlBuilder.toString();

        ResponseEntity<User> userResponse = restTemplate.exchange(url, HttpMethod.GET, entity, User.class);
        User owner = userResponse.getBody();
        projectPage.setOwner(owner);

        urlBuilder = new StringBuilder("https://api.hackaday.io/v1/projects/");
        urlBuilder.append(id);
        urlBuilder.append("/images");
        urlBuilder.append("?api_key=");
        urlBuilder.append(apiKey);
        url = urlBuilder.toString();

        ResponseEntity<ImageSet> imagesResponse = restTemplate.exchange(url, HttpMethod.GET, entity, ImageSet.class);
        ImageSet images = imagesResponse.getBody();
        projectPage.setImageSet(images);

        ModelAndView modelAndView = new ModelAndView("project");
        modelAndView.addObject("projectPage", projectPage);
        return modelAndView;
    }

}
