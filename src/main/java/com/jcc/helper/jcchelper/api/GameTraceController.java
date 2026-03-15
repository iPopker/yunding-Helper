package com.jcc.helper.jcchelper.api;

import com.jcc.helper.jcchelper.api.dto.RetrievalTraceResponse;
import com.jcc.helper.jcchelper.api.dto.StateDiffResponse;
import com.jcc.helper.jcchelper.domain.StateDiff;
import com.jcc.helper.jcchelper.persistence.RetrievalTraceRepository;
import com.jcc.helper.jcchelper.persistence.StateDiffRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/games")
public class GameTraceController {

    private final StateDiffRepository stateDiffRepository;
    private final RetrievalTraceRepository retrievalTraceRepository;

    public GameTraceController(StateDiffRepository stateDiffRepository,
                               RetrievalTraceRepository retrievalTraceRepository) {
        this.stateDiffRepository = stateDiffRepository;
        this.retrievalTraceRepository = retrievalTraceRepository;
    }

    @GetMapping("/{gameId}/diffs")
    public List<StateDiffResponse> getDiffs(@PathVariable String gameId) {
        return stateDiffRepository.findByGameId(gameId).stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/{gameId}/retrievals")
    public List<RetrievalTraceResponse> getRetrievals(@PathVariable String gameId) {
        return retrievalTraceRepository.findByGameId(gameId).stream()
                .map(row -> new RetrievalTraceResponse(
                        row.turnIndex(),
                        row.queryText(),
                        row.hitChunksJson()
                ))
                .toList();
    }

    private StateDiffResponse toResponse(StateDiff diff) {
        return new StateDiffResponse(
                diff.fromTurn(),
                diff.toTurn(),
                diff.goldDelta(),
                diff.hpDelta(),
                diff.levelDelta(),
                diff.shopChanged(),
                diff.boardChanged(),
                diff.benchChanged(),
                diff.itemChanged(),
                diff.summary()
        );
    }
}
