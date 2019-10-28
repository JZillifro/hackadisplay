package com.supplyframeproject.hackadisplay.service;

import com.supplyframeproject.hackadisplay.model.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

@Service
public class HackadisplayService {
    //this should be secret
    final String apiKey = "D5xeZrMHcBiWkIXT";

    public ProjectSet gatherProjectsData(String token, int page) {
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
        return projectsPage;
    }

    public Project gatherProjectData(String token, String id) {
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
        return projectPage;
    }
}
