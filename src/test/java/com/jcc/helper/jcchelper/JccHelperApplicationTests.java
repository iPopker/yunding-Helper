package com.jcc.helper.jcchelper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
class JccHelperApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
    }

    @Test
    void healthEndpointShouldReturnUp() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.application").value("jccHelper"))
                .andExpect(header().exists("X-Trace-Id"));
    }

    @Test
    void analyzeEndpointShouldReturnMockResult() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image",
                "board.png",
                MediaType.IMAGE_PNG_VALUE,
                "mock-image".getBytes()
        );

        mockMvc.perform(
                        multipart("/api/v1/analyze")
                                .file(image)
                                .param("gameId", "game_001")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value("game_001"))
                .andExpect(jsonPath("$.summary", notNullValue()))
                .andExpect(jsonPath("$.actions[0]", notNullValue()))
                .andExpect(header().exists("X-Trace-Id"));
    }
}
