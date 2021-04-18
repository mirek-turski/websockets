package com.mturski.example.websocket;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.mturski.example.websocket.ChatMessage.MessageType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ChatControllerIntegrationTest {

  @LocalServerPort
  private Integer port;

  private WebSocketStompClient webSocketStompClient;

  @BeforeEach
  public void setup() {
    this.webSocketStompClient = new WebSocketStompClient(new SockJsClient(
      List.of(new WebSocketTransport(new StandardWebSocketClient()))));
    this.webSocketStompClient.setMessageConverter(new MappingJackson2MessageConverter());

  }

  @Test
  public void verifyAddUser() throws Exception {

    BlockingQueue<ChatMessage> messageQueue = new ArrayBlockingQueue<>(1);

    StompSession session = createStompSession("/topic/public", messageQueue);

    session.send("/app/chat.addUser", ChatMessage.builder()
    .sender("Mike")
    .type(MessageType.JOIN)
    .build());

    ChatMessage message = messageQueue.poll(1, SECONDS);
    assertNotNull(message, "Response message to chat.addUser request not received");
    assertEquals("Mike", message.getSender());
    assertEquals(MessageType.JOIN, message.getType());
  }

  @Test
  public void verifySendMessage() throws Exception {
    BlockingQueue<ChatMessage> messageQueue = new ArrayBlockingQueue<>(4);

    StompSession session1 = createStompSession("/topic/public", messageQueue);
    StompSession session2 = createStompSession("/topic/public", messageQueue);

    ChatMessage sentMessage = ChatMessage.builder()
    .sender("Mike")
    .type(MessageType.CHAT)
    .content("Hello, it's Mike")
    .build();
    
    session1.send("/app/chat.sendMessage", sentMessage);

    ChatMessage message = messageQueue.poll(1, SECONDS);
    assertNotNull(message, "Response message to /chat.sendMessage request not received");
    assertEquals(sentMessage, message);
    message = messageQueue.poll(1, SECONDS);
    assertNotNull(message, "Response message to /chat.sendMessage request not received");
    assertEquals(sentMessage, message);

    sentMessage = ChatMessage.builder()
    .sender("Jake")
    .type(MessageType.CHAT)
    .content("Hi, Jake's here")
    .build();
    
    session2.send("/app/chat.sendMessage", sentMessage);

    message = messageQueue.poll(1, SECONDS);
    assertNotNull(message, "Response message to /chat.sendMessage request not received");
    assertEquals(sentMessage, message);
    message = messageQueue.poll(1, SECONDS);
    assertNotNull(message, "Response message to /chat.sendMessage request not received");
    assertEquals(sentMessage, message);
  }

  private StompSession createStompSession(final String destination, final BlockingQueue<ChatMessage> messageQueue) throws Exception {

    StompSession session = webSocketStompClient
      .connect(getWsPath(), new StompSessionHandlerAdapter() {})
      .get(1, SECONDS);

    session.subscribe(destination, new StompFrameHandler() {

      @Override
      public Type getPayloadType(StompHeaders headers) {
        return ChatMessage.class;
      }

      @Override
      public void handleFrame(StompHeaders headers, Object payload) {
        log.debug("Received payload {}", payload.toString());
        messageQueue.add((ChatMessage) payload);
      }
    });

    return session;
  }

  private String getWsPath() {
    return String.format("ws://localhost:%d/ws", port);
  }
}
