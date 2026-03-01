package com.example.token_learn.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.reactive.function.BodyInserters;

import com.example.token_learn.dto.RefreshTokenRequest;
import com.example.token_learn.dto.TokenRequest;
import com.example.token_learn.dto.TokenResponse;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * 認証サービス
 *
 * <p>ROPCフロー（Resource Owner Password Credentials）でアクセストークンを取得する。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final WebClient webClient;
    private final ClientRegistrationRepository clientRegistrationRepository;

    /**
     * ROPCフローで認証プロバイダーからトークンを取得する
     *
     * @param tokenRequest ユーザー名・パスワード
     * @return アクセストークン・有効期限・トークン種別
     */
    public TokenResponse getToken(TokenRequest tokenRequest) {
        ClientRegistration reg = clientRegistrationRepository.findByRegistrationId("idp");
        String tokenUri = reg.getProviderDetails().getTokenUri();
        String clientId = reg.getClientId();
        String clientSecret = reg.getClientSecret();

        log.debug("ROPCトークン取得: tokenUri={}", tokenUri);

        return webClient.post()
            .uri(tokenUri)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(
                BodyInserters.fromFormData("grant_type", "password")
                    .with("username", tokenRequest.getUsername())
                    .with("password", tokenRequest.getPassword())
                    .with("client_id", clientId)
                    .with("client_secret", clientSecret)
                    .with("scope", "openid")
            )
            .retrieve()
            .bodyToMono(TokenResponse.class)
            .block();
    }

    /**
     * リフレッシュトークンで新しいアクセストークンを取得する
     *
     * @param refreshTokenRequest リフレッシュトークン
     * @return アクセストークン・有効期限・トークン種別・リフレッシュトークン
     */
    public TokenResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        ClientRegistration reg = clientRegistrationRepository.findByRegistrationId("idp");
        String tokenUri = reg.getProviderDetails().getTokenUri();
        String clientId = reg.getClientId();
        String clientSecret = reg.getClientSecret();

        log.debug("リフレッシュトークンによるトークン更新: tokenUri={}", tokenUri);

        return webClient.post()
            .uri(tokenUri)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(
                BodyInserters.fromFormData("grant_type", "refresh_token")
                    .with("refresh_token", refreshTokenRequest.getRefreshToken())
                    .with("client_id", clientId)
                    .with("client_secret", clientSecret)
            )
            .retrieve()
            .bodyToMono(TokenResponse.class)
            .block();
    }

}
