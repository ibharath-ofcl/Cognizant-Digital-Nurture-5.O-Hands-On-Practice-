package com.example.employeemanagementsystem.controller;

import com.example.employeemanagementsystem.model.secondary.Project;
import com.example.employeemanagementsystem.repository.secondary.ProjectRepository;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProjectControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProjectRepository projectRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        projectRepository.deleteAll();
    }

    @Test
    void testCreateProject() throws Exception {
        Project project = Project.builder()
                .name("Project Alpha")
                .budget(150000.0)
                .build();

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(project)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("Project Alpha")))
                .andExpect(jsonPath("$.budget", is(150000.0)));
    }

    @Test
    void testGetAllProjects() throws Exception {
        Project p1 = projectRepository.save(Project.builder().name("Project P1").budget(20000.0).build());
        Project p2 = projectRepository.save(Project.builder().name("Project P2").budget(50000.0).build());

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Project P1")))
                .andExpect(jsonPath("$[1].name", is("Project P2")));
    }

    @Test
    void testGetProjectById() throws Exception {
        Project saved = projectRepository.save(Project.builder().name("Project X").budget(99999.0).build());

        mockMvc.perform(get("/api/projects/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Project X")))
                .andExpect(jsonPath("$.budget", is(99999.0)));
    }

    @Test
    void testUpdateProject() throws Exception {
        Project saved = projectRepository.save(Project.builder().name("Old Project").budget(100.0).build());

        Project updatedDetails = Project.builder().name("New Project").budget(200.0).build();

        mockMvc.perform(put("/api/projects/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("New Project")))
                .andExpect(jsonPath("$.budget", is(200.0)));
    }

    @Test
    void testDeleteProject() throws Exception {
        Project saved = projectRepository.save(Project.builder().name("Temp Project").budget(10.0).build());

        mockMvc.perform(delete("/api/projects/" + saved.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/projects/" + saved.getId()))
                .andExpect(status().isNotFound());
    }
}
