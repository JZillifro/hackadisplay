package com.supplyframeproject.hackadisplay.controller;

import com.supplyframeproject.hackadisplay.model.AccessResponse;
import com.supplyframeproject.hackadisplay.model.ProjectSet;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class HackadisplayController {
    @GetMapping("/logIn")
    public RedirectView logIn(RedirectAttributes attributes) {
        String clientID = "8oBvmZozI6op98W1u8qfWnBnsWwJ016M4AQvrHDCNgpIJDPZ";
        attributes.addFlashAttribute("flashAttribute", "redirectWithRedirectView");
        attributes.addAttribute("attribute", "redirectWithRedirectView");
        return new RedirectView("https://hackaday.io/authorize?client_id=" + clientID + "&state=<optional>&response_type=code");
    }

    @RequestMapping("/")
    public String home() {
        return "home";
    }

    @RequestMapping("/projects")
    public String p(@RequestParam String code) {
        String clientID = "8oBvmZozI6op98W1u8qfWnBnsWwJ016M4AQvrHDCNgpIJDPZ";
        String clientSecret = "t0mqizevgcXrlJYAgzkiSrAWJMHlBfJgGT9ub6nmPyHsHytG";
        String apiKey = "D5xeZrMHcBiWkIXT";

        RestTemplate restTemplate = new RestTemplate();

        String url = "https://hackaday.io/access_token?client_id=" + clientID + "&client_secret=" + clientSecret + "&code=" + code + "&grant_type=authorization_code";
        AccessResponse accessResponse = restTemplate.getForObject(url, AccessResponse.class);
        String token = accessResponse.getAccess_token();
        System.out.println("access token: " + token);

        final HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + token);
        System.out.println("header: " + "token " + token);
        final HttpEntity<String> entity = new HttpEntity<String>(headers);
        url = "https://api.hackaday.io/v1/projects?api_key=" + apiKey;
        ResponseEntity<ProjectSet> response = restTemplate.exchange(url, HttpMethod.GET, entity, ProjectSet.class);
        System.out.println(response.getBody().getProjects()[0].getName());
        return "projects";
    }

}
