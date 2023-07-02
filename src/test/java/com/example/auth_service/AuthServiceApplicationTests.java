package com.example.auth_service;

import com.example.auth_service.api.JwtRequest;
import com.example.auth_service.api.Person;
import com.example.auth_service.config.Role;
import com.example.auth_service.controller.Controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Клас проверяет при подключении 2 микросервиса.
 */

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class AuthServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private Controller controller;

    @Test
    public void test() {
        assertThat(controller).isNotNull();
    }

    public String getToken() throws Exception {
        JwtRequest request = new JwtRequest();
        request.setLogin("xxx@mail.com");
        request.setPassword("fdf");
        var greeting = "Whatever the service returns";
        MvcResult m = mockMvc.perform(post("/api/auth/login")
                        .content(asJsonString(request))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn();
        String[] splitResult = m.getResponse().getContentAsString().split(":");
        String[] token = splitResult[2].split(",");
        return token[0].substring(1, token[0].length() - 1);
    }

    @Test
    public void getTestGetAll() throws Exception {
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/all")
                                .header("authorization", "Bearer " + getToken()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andDo(print());
    }

    @Test
    public void getTestGetId() throws Exception {
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/1")
                                .header("authorization", "Bearer " + getToken()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andDo(print());
    }

    @Test
    public void getTestCreate() throws Exception {
        Person person = new Person();
        person.setLastName("Admin");
        person.setFirstName("Admin");
        person.setRole(Role.ADMIN);
        person.setBirthday(LocalDate.of(2010, 2, 2));
        person.setPassword("123A");
        person.setEmail("admin@mail.com");
        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api")
                                .header("authorization", "Bearer " + getToken())
                                .content(asJsonString(person))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andDo(print());
    }

    @Test
    public void getTestUpdate() throws Exception {
        Person person = new Person();
        person.setLastName("Admin");
        person.setFirstName("UpdateAdmin");
        person.setRole(Role.ADMIN);
        person.setBirthday(LocalDate.of(2010, 2, 2));
        person.setPassword("fdf");
        person.setEmail("xxx@mail.com");
        mockMvc.perform(
                        MockMvcRequestBuilders.put("/api/1")
                                .header("authorization", "Bearer " + getToken())
                                .content(asJsonString(person))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andDo(print());
    }

    @Test
    public void getTestUpdateRole() throws Exception {
        mockMvc.perform(
                        MockMvcRequestBuilders.put("/api/1/role?role=ADMIN")
                                .header("authorization", "Bearer " + getToken())
                                .content(asJsonString(Role.ADMIN))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andDo(print());
    }

    private static String asJsonString(final Object obj) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule((new JavaTimeModule()));
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
