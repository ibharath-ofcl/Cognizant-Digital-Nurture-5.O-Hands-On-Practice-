package com.example.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.reactivestreams.Publisher;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;

/**
 * Custom Caching Gateway Filter.
 * Caches successful downstream GET responses in the configured Caffeine cache.
 * Avoids redundant network calls to customer/billing services.
 */
@Component
public class CachingGatewayFilterFactory 
        extends AbstractGatewayFilterFactory<CachingGatewayFilterFactory.Config> {

    private static final Logger log = LoggerFactory.getLogger(CachingGatewayFilterFactory.class);
    private final CacheManager cacheManager;

    public CachingGatewayFilterFactory(CacheManager cacheManager) {
        super(Config.class);
        this.cacheManager = cacheManager;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            // 1. Caching should only occur on HTTP GET requests to prevent side-effects
            if (request.getMethod() != HttpMethod.GET) {
                return chain.filter(exchange);
            }

            String cacheKey = request.getURI().toString();
            Cache cache = cacheManager.getCache(config.getCacheName());

            if (cache == null) {
                log.warn("Cache named '{}' not initialized.", config.getCacheName());
                return chain.filter(exchange);
            }

            // 2. Query cache for an existing response
            CachedResponse cachedResponse = cache.get(cacheKey, CachedResponse.class);
            if (cachedResponse != null) {
                log.info("[Cache HIT] Serving cached payload for key: {}", cacheKey);
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatusCode.valueOf(cachedResponse.getStatusCode()));
                
                if (cachedResponse.getContentType() != null) {
                    response.getHeaders().setContentType(MediaType.valueOf(cachedResponse.getContentType()));
                }
                
                // Add header to clearly identify cache hits
                response.getHeaders().add("X-Cache", "HIT");
                
                byte[] bodyBytes = cachedResponse.getBody().getBytes(StandardCharsets.UTF_8);
                DataBuffer buffer = response.bufferFactory().wrap(bodyBytes);
                return response.writeWith(Mono.just(buffer));
            }

            log.info("[Cache MISS] Forwarding request to downstream for key: {}", cacheKey);

            // 3. Request interception to cache the downstream response on departure
            ServerHttpResponse originalResponse = exchange.getResponse();
            DataBufferFactory bufferFactory = originalResponse.bufferFactory();

            // Decorate response to capture the body buffer stream
            ServerHttpResponseDecorator responseDecorator = new ServerHttpResponseDecorator(originalResponse) {
                @Override
                public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                    if (body instanceof Flux) {
                        Flux<? extends DataBuffer> flux = (Flux<? extends DataBuffer>) body;

                        return super.writeWith(flux.buffer().map(dataBuffers -> {
                            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                            for (DataBuffer dataBuffer : dataBuffers) {
                                byte[] bytes = new byte[dataBuffer.readableByteCount()];
                                dataBuffer.read(bytes);
                                DataBufferUtils.release(dataBuffer); // Release buffer to avoid memory leaks
                                outputStream.writeBytes(bytes);
                            }

                            byte[] bodyBytes = outputStream.toByteArray();
                            String bodyString = new String(bodyBytes, StandardCharsets.UTF_8);

                            // Only cache successful 200 OK responses
                            HttpStatusCode statusCode = getStatusCode();
                            if (statusCode != null && statusCode.is2xxSuccessful()) {
                                String contentType = getHeaders().getFirst("Content-Type");
                                CachedResponse responseToStore = new CachedResponse(
                                    statusCode.value(),
                                    contentType,
                                    bodyString
                                );
                                cache.put(cacheKey, responseToStore);
                                log.info("[Cache POPULATE] Cached response for key: {}", cacheKey);
                            }

                            return bufferFactory.wrap(bodyBytes);
                        }));
                    }
                    return super.writeWith(body);
                }
            };

            // Execute the chain using our decorated response wrapper
            return chain.filter(exchange.mutate().response(responseDecorator).build());
        };
    }

    /**
     * Cache Config settings mapped from application.yml
     */
    public static class Config {
        private String cacheName = "customerCache";

        public String getCacheName() {
            return cacheName;
        }

        public void setCacheName(String cacheName) {
            this.cacheName = cacheName;
        }
    }

    /**
     * DTO containing the response status, content type, and body payload.
     */
    public static class CachedResponse implements Serializable {
        private static final long serialVersionUID = 1L;
        private int statusCode;
        private String contentType;
        private String body;

        public CachedResponse() {}

        public CachedResponse(int statusCode, String contentType, String body) {
            this.statusCode = statusCode;
            this.contentType = contentType;
            this.body = body;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public void setStatusCode(int statusCode) {
            this.statusCode = statusCode;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }
    }
}
