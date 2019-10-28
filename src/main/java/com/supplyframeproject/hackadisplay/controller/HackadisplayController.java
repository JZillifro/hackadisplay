package com.supplyframeproject.hackadisplay.controller;

import com.supplyframeproject.hackadisplay.model.*;
import com.supplyframeproject.hackadisplay.service.HackadisplayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Controller
public class HackadisplayController {

    @Autowired
    private HackadisplayService hackadisplayService;

//    these should be secret
    final String clientID = "8oBvmZozI6op98W1u8qfWnBnsWwJ016M4AQvrHDCNgpIJDPZ";
    final String clientSecret = "t0mqizevgcXrlJYAgzkiSrAWJMHlBfJgGT9ub6nmPyHsHytG";

    @GetMapping("/error")
    public String error() { return "error"; }

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/logIn")
    public RedirectView logIn(RedirectAttributes attributes) {
        attributes.addFlashAttribute("flashAttribute", "redirectWithRedirectView");
        attributes.addAttribute("attribute", "redirectWithRedirectView");
        return new RedirectView("https://hackaday.io/authorize?client_id=" + clientID + "&state=<optional>&response_type=code");
    }

    @GetMapping("/setCookie")
    public String setCookie(HttpServletResponse response, @RequestParam String code) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://hackaday.io/access_token?client_id=" + clientID + "&client_secret=" + clientSecret + "&code=" + code + "&grant_type=authorization_code";
        AccessResponse accessResponse = restTemplate.getForObject(url, AccessResponse.class);
        String token = accessResponse.getAccess_token();

        Cookie cookie = new Cookie("hackadisplay_auth_token", token);
        response.addCookie(cookie);
        return "cookieSuccess";
    }

    @Cacheable("projects")
    @GetMapping("/projects")
    public ModelAndView projects(@CookieValue(value = "hackadisplay_auth_token", defaultValue = "") String token, @RequestParam(value = "page", required = false, defaultValue = "1") int page) {
        ProjectSet projectsPage = hackadisplayService.gatherProjectsData(token, page);
        ModelAndView modelAndView = new ModelAndView("projects");
        modelAndView.addObject("projectsPage", projectsPage);
        return modelAndView;
    }

    @GetMapping("/projects/{id}")
    public ModelAndView projectById(@CookieValue(value = "hackadisplay_auth_token", defaultValue = "") String token, @PathVariable String id) {
        Project projectPage = hackadisplayService.gatherProjectData(token, id);
        ModelAndView modelAndView = new ModelAndView("project");
        modelAndView.addObject("projectPage", projectPage);
        return modelAndView;
    }

}
