package com.jcc.helper.jcchelper.service;

import com.jcc.helper.jcchelper.api.dto.AnalyzeResponse;
import com.jcc.helper.jcchelper.domain.GameMemory;
import com.jcc.helper.jcchelper.domain.GameState;
import com.jcc.helper.jcchelper.domain.Observation;
import com.jcc.helper.jcchelper.domain.RetrievalChunk;
import com.jcc.helper.jcchelper.domain.StateDiff;
import com.jcc.helper.jcchelper.persistence.AdviceTraceRepository;
import com.jcc.helper.jcchelper.persistence.GameSessionRepository;
import com.jcc.helper.jcchelper.persistence.MemorySummaryRepository;
import com.jcc.helper.jcchelper.persistence.RetrievalTraceRepository;
import com.jcc.helper.jcchelper.persistence.StateDiffRepository;
import com.jcc.helper.jcchelper.persistence.TurnStateRepository;
import com.jcc.helper.jcchelper.service.advice.AdviceComposer;
import com.jcc.helper.jcchelper.service.advice.StructuredAdvice;
import com.jcc.helper.jcchelper.service.advice.StructuredAdviceValidator;
import com.jcc.helper.jcchelper.service.rag.KnowledgeRetriever;
import com.jcc.helper.jcchelper.service.rag.QueryPlanner;
import com.jcc.helper.jcchelper.service.state.MemoryEngine;
import com.jcc.helper.jcchelper.service.state.StateEngine;
import com.jcc.helper.jcchelper.service.vision.VisionAdapter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Service
public class AnalyzeService {

    private final VisionAdapter visionAdapter;
    private final GameSessionRepository gameSessionRepository;
    private final TurnStateRepository turnStateRepository;
    private final StateEngine stateEngine;
    private final StateDiffRepository stateDiffRepository;
    private final MemorySummaryRepository memorySummaryRepository;
    private final MemoryEngine memoryEngine;
    private final QueryPlanner queryPlanner;
    private final KnowledgeRetriever knowledgeRetriever;
    private final RetrievalTraceRepository retrievalTraceRepository;
    private final AdviceComposer adviceComposer;
    private final StructuredAdviceValidator structuredAdviceValidator;
    private final AdviceTraceRepository adviceTraceRepository;

    public AnalyzeService(VisionAdapter visionAdapter,
                          GameSessionRepository gameSessionRepository,
                          TurnStateRepository turnStateRepository,
                          StateEngine stateEngine,
                          StateDiffRepository stateDiffRepository,
                          MemorySummaryRepository memorySummaryRepository,
                          MemoryEngine memoryEngine,
                          QueryPlanner queryPlanner,
                          KnowledgeRetriever knowledgeRetriever,
                          RetrievalTraceRepository retrievalTraceRepository,
                          AdviceComposer adviceComposer,
                          StructuredAdviceValidator structuredAdviceValidator,
                          AdviceTraceRepository adviceTraceRepository) {
        this.visionAdapter = visionAdapter;
        this.gameSessionRepository = gameSessionRepository;
        this.turnStateRepository = turnStateRepository;
        this.stateEngine = stateEngine;
        this.stateDiffRepository = stateDiffRepository;
        this.memorySummaryRepository = memorySummaryRepository;
        this.memoryEngine = memoryEngine;
        this.queryPlanner = queryPlanner;
        this.knowledgeRetriever = knowledgeRetriever;
        this.retrievalTraceRepository = retrievalTraceRepository;
        this.adviceComposer = adviceComposer;
        this.structuredAdviceValidator = structuredAdviceValidator;
        this.adviceTraceRepository = adviceTraceRepository;
    }

    @Transactional
    public AnalyzeResponse analyze(String gameId, MultipartFile image) {
        int turnIndex = gameSessionRepository.nextTurnIndex(gameId);
        Optional<GameState> previousState = turnStateRepository.findLatestByGameId(gameId);
        Optional<GameMemory> previousMemory = memorySummaryRepository.findLatestByGameId(gameId);

        Observation observation = visionAdapter.analyze(image);
        GameState currentState = stateEngine.buildGameState(gameId, turnIndex, observation);
        StateDiff diff = stateEngine.calculateDiff(previousState.orElse(null), currentState);
        GameMemory memory = memoryEngine.buildMemory(previousMemory.orElse(null), currentState, diff);

        turnStateRepository.save(currentState);
        stateDiffRepository.save(diff);
        memorySummaryRepository.save(memory);

        String retrievalQuery = queryPlanner.plan(currentState, diff);
        java.util.List<RetrievalChunk> chunks = knowledgeRetriever.retrieve(retrievalQuery, currentState, 3);
        retrievalTraceRepository.save(gameId, turnIndex, retrievalQuery, chunks);

        StructuredAdvice validatedAdvice = structuredAdviceValidator.validateOrFallback(
                adviceComposer.compose(currentState, diff, memory, chunks)
        );

        AnalyzeResponse response = new AnalyzeResponse(
                gameId,
                turnIndex,
                validatedAdvice.summary(),
                validatedAdvice.compDirection(),
                validatedAdvice.actions(),
                validatedAdvice.reasons(),
                validatedAdvice.risks(),
                validatedAdvice.uncertainties(),
                validatedAdvice.confidence()
        );
        adviceTraceRepository.save(gameId, turnIndex, response);
        return response;
    }
}
