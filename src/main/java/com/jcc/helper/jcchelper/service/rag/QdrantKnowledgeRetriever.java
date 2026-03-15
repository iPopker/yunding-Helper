package com.jcc.helper.jcchelper.service.rag;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.jcc.helper.jcchelper.config.QdrantProperties;
import com.jcc.helper.jcchelper.domain.GameState;
import com.jcc.helper.jcchelper.domain.RetrievalChunk;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class QdrantKnowledgeRetriever implements KnowledgeRetriever {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final QdrantProperties qdrantProperties;

    public QdrantKnowledgeRetriever(RestClient.Builder restClientBuilder,
                                    ObjectMapper objectMapper,
                                    QdrantProperties qdrantProperties) {
        this.restClient = restClientBuilder
                .baseUrl("http://" + qdrantProperties.host() + ":" + qdrantProperties.port())
                .build();
        this.objectMapper = objectMapper;
        this.qdrantProperties = qdrantProperties;
    }

    @Override
    public List<RetrievalChunk> retrieve(String query, GameState state, int topK) {
        try {
            List<RetrievalChunk> chunks = searchFromQdrant(query, state, topK);
            if (!chunks.isEmpty()) {
                return chunks;
            }
        } catch (Exception ignored) {
            // Fallback to local knowledge when Qdrant is unavailable.
        }
        return fallbackKnowledge(state, topK);
    }

    private List<RetrievalChunk> searchFromQdrant(String query, GameState state, int topK) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("query", query);
        payload.put("limit", topK);
        payload.put("with_payload", true);

        Map<String, Object> filter = Map.of(
                "must", List.of(
                        Map.of(
                                "key", "phase",
                                "match", Map.of("value", stageBucket(state.stage()))
                        )
                )
        );
        payload.put("filter", filter);

        String responseBody = restClient.post()
                .uri("/collections/{collection}/points/query", qdrantProperties.collection())
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .body(String.class);

        if (responseBody == null || responseBody.isBlank()) {
            return List.of();
        }

        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode points = root.path("result").path("points");
        if (!points.isArray()) {
            return List.of();
        }

        List<RetrievalChunk> chunks = new ArrayList<>();
        for (JsonNode point : points) {
            JsonNode payloadNode = point.path("payload");
            String text = payloadNode.path("text").asText("");
            if (text.isBlank()) {
                continue;
            }
            Map<String, Object> metadata = objectMapper.convertValue(payloadNode, new TypeReference<>() {});
            chunks.add(new RetrievalChunk(
                    point.path("id").asText(),
                    text,
                    point.path("score").asDouble(0.0),
                    metadata
            ));
        }
        return chunks;
    }

    private List<RetrievalChunk> fallbackKnowledge(GameState state, int topK) {
        List<RetrievalChunk> all = List.of(
                new RetrievalChunk(
                        "fallback-eco-1",
                        "前中期若血量健康且经济可控，优先吃利息再补关键质量。",
                        0.61,
                        Map.of("source", "fallback", "phase", "mid")
                ),
                new RetrievalChunk(
                        "fallback-hp-1",
                        "出现连续掉血时，优先提升当前战力，避免贪经济导致出局风险。",
                        0.66,
                        Map.of("source", "fallback", "phase", "mid")
                ),
                new RetrievalChunk(
                        "fallback-item-1",
                        "装备优先服务当前可上场主力，减少因等完美组件导致的空窗期。",
                        0.58,
                        Map.of("source", "fallback", "phase", "early")
                )
        );
        String targetBucket = stageBucket(state.stage());
        return all.stream()
                .filter(c -> targetBucket.equals(c.metadata().get("phase")))
                .sorted(Comparator.comparingDouble(RetrievalChunk::score).reversed())
                .limit(topK)
                .toList();
    }

    private String stageBucket(String stage) {
        if (stage == null || stage.isBlank()) {
            return "mid";
        }
        int chapter = Character.getNumericValue(stage.charAt(0));
        if (chapter <= 2) {
            return "early";
        }
        if (chapter >= 5) {
            return "late";
        }
        return "mid";
    }
}
