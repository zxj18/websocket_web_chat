package kim.ioio.web.config;

import kim.ioio.web.WebSocketMessageHandler;
import kim.ioio.web.interceptors.MyWebSocketHandshakeInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@EnableWebMvc
public class MyWebSocketConfig implements WebSocketConfigurer {

    public MyWebSocketConfig (){
        System.out.println("init MyWebSocketConfig");
    }

    @Autowired
    private WebSocketMessageHandler myWebSocketMessageHandler;

    @Autowired
    private MyWebSocketHandshakeInterceptor myWebSocketHandshakeInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry webSocketHandlerRegistry) {
        webSocketHandlerRegistry.addHandler(myWebSocketMessageHandler, "/websocket").setAllowedOrigins("*")
                .addInterceptors(myWebSocketHandshakeInterceptor);

        webSocketHandlerRegistry.addHandler(myWebSocketMessageHandler, "/sockjs").setAllowedOrigins("*")
                .addInterceptors(myWebSocketHandshakeInterceptor).withSockJS();
    }

}