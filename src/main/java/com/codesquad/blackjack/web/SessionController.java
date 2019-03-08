package com.codesquad.blackjack.web;

import com.codesquad.blackjack.MessageType;
import com.codesquad.blackjack.domain.Game;
import com.codesquad.blackjack.domain.card.Deck;
import com.codesquad.blackjack.domain.player.User;
import com.codesquad.blackjack.domain.player.UserRepository;
import com.codesquad.blackjack.dto.BettingDto;
import com.codesquad.blackjack.dto.DealerTurnDto;
import com.codesquad.blackjack.dto.ResultDto;
import com.codesquad.blackjack.security.WebSocketSessionUtils;
import com.codesquad.blackjack.service.GameService;
import com.codesquad.blackjack.service.UserService;
import com.codesquad.blackjack.socket.GameSession;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

import static com.codesquad.blackjack.MessageType.BLACKJACK;
import static com.codesquad.blackjack.MessageType.INIT;
import static com.codesquad.blackjack.domain.Game.DOUBLE_SELECTION;
import static com.codesquad.blackjack.domain.Game.HIT_SELECTION;
import static com.codesquad.blackjack.domain.Game.STAND_SELECTION;


@Component
public class SessionController {
    private static final Logger log = LoggerFactory.getLogger(SessionController.class);

    @Autowired
    private GameService gameService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    public void readyToGame(WebSocketSession webSocketSession, GameSession gameSession) throws IOException {
        gameSession.addSession(webSocketSession);

        User user = WebSocketSessionUtils.userFromSession(webSocketSession);
        for (WebSocketSession ws : gameSession.getSessions()) {
            ws.sendMessage(new TextMessage(objectMapper.writeValueAsString(user._toChatDto("JOIN"))));
        }
    }

    public void startGame(GameSession gameSession) throws IOException {
        Game game = gameService.findById(gameSession.getGameId());

        game.init(100);

        for (WebSocketSession ws : gameSession.getSessions()) {
            ws.sendMessage(new TextMessage(objectMapper.writeValueAsString(game.getDealerDto(INIT))));
            ws.sendMessage(new TextMessage(objectMapper.writeValueAsString(game.getUserDto(INIT))));
        }

        if (game.isBlackjack()) {
            for (WebSocketSession ws : gameSession.getSessions()) {
                ws.sendMessage(new TextMessage(objectMapper
                        .writeValueAsString(new ResultDto(BLACKJACK, game.end(game.getBlackjackPrize())))));
            }

            game.initializeGame();

            //TODO: 질문!!
            userRepository.save(game.getUser());
        }
    }

    public void playerTurnGame(GameSession gameSession, int bettingChip) throws IOException {
        Game game = gameService.findById(gameSession.getGameId());

        if (game.isGameProcess()) {
            if(game.hasGamerEnoughChip(bettingChip)) {
                //유저에게 turn은 더블까지 보이는 상태를 만들어줘야해
                for (WebSocketSession ws : gameSession.getSessions()) {
                    ws.sendMessage(new TextMessage(objectMapper.writeValueAsString(new BettingDto("DOUBLE"))));
                }
            }
            for (WebSocketSession ws : gameSession.getSessions()) {
                ws.sendMessage(new TextMessage(objectMapper.writeValueAsString(new BettingDto("NONE_DOUBLE"))));
            }
        }
    }

    public void playerSelect(GameSession gameSession, int turn) throws IOException {
        Game game = gameService.findById(gameSession.getGameId());


        if (game.isGameProcess() && turn != STAND_SELECTION) {
            game.hit();

            for (WebSocketSession ws : gameSession.getSessions()) {
                ws.sendMessage(new TextMessage(objectMapper.writeValueAsString(game.getDealerDto(INIT))));
                ws.sendMessage(new TextMessage(objectMapper.writeValueAsString(game.getUserDto(INIT))));
            }

            if (game.isBurst()) {
                game.initializeGame();
                game.stopGame();
                //출력해줘야됨 결과
                return;
            }

            if (game.isBlackjack() || turn == DOUBLE_SELECTION) {
                for (WebSocketSession ws : gameSession.getSessions()) {
                    ws.sendMessage(new TextMessage(objectMapper.writeValueAsString(new DealerTurnDto("DELAERTURN"))));
                }
                return;
            }

            if (turn == HIT_SELECTION) {
                for (WebSocketSession ws : gameSession.getSessions()) {
                    ws.sendMessage(new TextMessage(objectMapper.writeValueAsString(new BettingDto("NONE_DOUBLE"))));
                }
            }
        }

        else {
            for (WebSocketSession ws : gameSession.getSessions()) {
                ws.sendMessage(new TextMessage(objectMapper.writeValueAsString(new DealerTurnDto("DEALERTURN"))));
            }
        }
    }

    public void dealerTurnGame(GameSession gameSession) throws IOException {
        Game game = gameService.findById(gameSession.getGameId());

        game.dealerTurn();

        if (game.isBurst()) {
            game.endByPlayerWin(game.getNormalPrize());
            return;
        }

        for (WebSocketSession ws : gameSession.getSessions()) {
            ws.sendMessage(new TextMessage(objectMapper.writeValueAsString(game.getDealerDto(INIT))));
            ws.sendMessage(new TextMessage(objectMapper.writeValueAsString(game.getUserDto(INIT))));
        }
    }


}