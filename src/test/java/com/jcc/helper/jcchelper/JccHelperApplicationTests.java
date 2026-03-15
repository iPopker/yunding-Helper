package com.jcc.helper.jcchelper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
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
        String gameId = "game_" + UUID.randomUUID();
        MockMultipartFile image = new MockMultipartFile(
                "image",
                "board.png",
                MediaType.IMAGE_PNG_VALUE,
                "mock-image".getBytes()
        );

        mockMvc.perform(
                        multipart("/api/v1/analyze")
                                .file(image)
                                .param("gameId", gameId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(gameId))
                .andExpect(jsonPath("$.turnIndex").value(1))
                .andExpect(jsonPath("$.summary", notNullValue()))
                .andExpect(jsonPath("$.actions[0]", notNullValue()))
                .andExpect(jsonPath("$.reasons[0]", notNullValue()))
                .andExpect(header().exists("X-Trace-Id"));
    }

    @Test
    void sameGameShouldBuildContinuousTurnsAndDiffs() throws Exception {
        String gameId = "game_" + UUID.randomUUID();
        MockMultipartFile first = new MockMultipartFile(
                "image",
                "first.png",
                MediaType.IMAGE_PNG_VALUE,
                "first".getBytes()
        );
        MockMultipartFile second = new MockMultipartFile(
                "image",
                "second.png",
                MediaType.IMAGE_PNG_VALUE,
                "second".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/analyze").file(first).param("gameId", gameId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.turnIndex").value(1));

        mockMvc.perform(multipart("/api/v1/analyze").file(second).param("gameId", gameId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.turnIndex").value(2));

        mockMvc.perform(get("/api/v1/games/{gameId}/diffs", gameId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].toTurn").value(1))
                .andExpect(jsonPath("$[1].toTurn").value(2));

        mockMvc.perform(get("/api/v1/games/{gameId}/retrievals", gameId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].turnIndex").value(1))
                .andExpect(jsonPath("$[1].turnIndex").value(2))
                .andExpect(jsonPath("$[0].queryText", notNullValue()));

        mockMvc.perform(get("/api/v1/games/{gameId}/replay", gameId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].turnIndex").value(1))
                .andExpect(jsonPath("$[1].turnIndex").value(2))
                .andExpect(jsonPath("$[0].adviceJson", notNullValue()))
                .andExpect(jsonPath("$[0].retrievalQuery", notNullValue()));
    }

    @Test
    void lowConfidenceShouldUseConservativeAdviceAndExposeMetrics() throws Exception {
        String gameId = "game_" + UUID.randomUUID();
        MockMultipartFile lowConfidenceImage = new MockMultipartFile(
                "image",
                "low.png",
                MediaType.IMAGE_PNG_VALUE,
                new byte[0]
        );

        mockMvc.perform(multipart("/api/v1/analyze").file(lowConfidenceImage).param("gameId", gameId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").value("识别置信度偏低，已切换保守策略。"))
                .andExpect(jsonPath("$.uncertainties[0]").value("视觉识别置信度较低，建议人工确认关键信息。"));

        mockMvc.perform(get("/api/v1/metrics/quality"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAnalyzeCount", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.lowConfidenceRatio", greaterThanOrEqualTo(0.0)))
                .andExpect(jsonPath("$.latencyP50Ms", greaterThanOrEqualTo(0.0)));
    }
}
