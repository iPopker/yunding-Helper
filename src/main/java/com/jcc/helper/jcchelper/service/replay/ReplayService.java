package com.jcc.helper.jcchelper.service.replay;

import com.jcc.helper.jcchelper.api.dto.ReplayTurnResponse;
import com.jcc.helper.jcchelper.domain.GameMemory;
import com.jcc.helper.jcchelper.domain.GameState;
import com.jcc.helper.jcchelper.domain.StateDiff;
import com.jcc.helper.jcchelper.persistence.AdviceTraceRepository;
import com.jcc.helper.jcchelper.persistence.MemorySummaryRepository;
import com.jcc.helper.jcchelper.persistence.RetrievalTraceRepository;
import com.jcc.helper.jcchelper.persistence.StateDiffRepository;
import com.jcc.helper.jcchelper.persistence.TurnStateRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ReplayService {

    private final TurnStateRepository turnStateRepository;
    private final StateDiffRepository stateDiffRepository;
    private final MemorySummaryRepository memorySummaryRepository;
    private final AdviceTraceRepository adviceTraceRepository;
    private final RetrievalTraceRepository retrievalTraceRepository;

    public ReplayService(TurnStateRepository turnStateRepository,
                         StateDiffRepository stateDiffRepository,
                         MemorySummaryRepository memorySummaryRepository,
                         AdviceTraceRepository adviceTraceRepository,
                         RetrievalTraceRepository retrievalTraceRepository) {
        this.turnStateRepository = turnStateRepository;
        this.stateDiffRepository = stateDiffRepository;
        this.memorySummaryRepository = memorySummaryRepository;
        this.adviceTraceRepository = adviceTraceRepository;
        this.retrievalTraceRepository = retrievalTraceRepository;
    }

    public List<ReplayTurnResponse> replay(String gameId) {
        List<GameState> states = turnStateRepository.findByGameId(gameId);
        Map<Integer, StateDiff> diffMap = stateDiffRepository.findByGameId(gameId).stream()
                .collect(Collectors.toMap(StateDiff::toTurn, Function.identity(), (a, b) -> b));
        Map<Integer, GameMemory> memoryMap = memorySummaryRepository.findByGameId(gameId).stream()
                .collect(Collectors.toMap(GameMemory::turnIndex, Function.identity(), (a, b) -> b));
        Map<Integer, AdviceTraceRepository.AdviceTraceRow> adviceMap = adviceTraceRepository.findByGameId(gameId).stream()
                .collect(Collectors.toMap(AdviceTraceRepository.AdviceTraceRow::turnIndex, Function.identity(), (a, b) -> b));
        Map<Integer, RetrievalTraceRepository.RetrievalTraceRow> retrievalMap = retrievalTraceRepository.findByGameId(gameId).stream()
                .collect(Collectors.toMap(RetrievalTraceRepository.RetrievalTraceRow::turnIndex, Function.identity(), (a, b) -> b));

        return states.stream().map(state -> {
            StateDiff diff = diffMap.get(state.turnIndex());
            GameMemory memory = memoryMap.get(state.turnIndex());
            AdviceTraceRepository.AdviceTraceRow advice = adviceMap.get(state.turnIndex());
            RetrievalTraceRepository.RetrievalTraceRow retrieval = retrievalMap.get(state.turnIndex());
            return new ReplayTurnResponse(
                    state.turnIndex(),
                    state.stage(),
                    state.gold(),
                    state.level(),
                    state.hp(),
                    state.stateConfidence(),
                    diff == null ? "" : diff.summary(),
                    memory == null ? "" : memory.summary(),
                    memory == null ? "" : memory.continuityNotes(),
                    advice == null ? "" : advice.adviceJson(),
                    retrieval == null ? "" : retrieval.queryText(),
                    retrieval == null ? "" : retrieval.hitChunksJson(),
                    memory == null ? List.of() : memory.keyRisks()
            );
        }).toList();
    }
}
