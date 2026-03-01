package com.example.token_learn.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.token_learn.dto.RefreshTokenRequest;
import com.example.token_learn.dto.TokenRequest;
import com.example.token_learn.dto.TokenResponse;
import com.example.token_learn.service.AuthService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * ROPCフローによるトークン取得エンドポイント
     *
     * <p>ユーザー名・パスワードをリクエストボディで受け取り、アクセストークンを返します。</p>
     * <p>取得したアクセストークンはリソースサーバーへの Bearer 認証に使用できます。</p>
     *
     * @param tokenRequest ユーザー名・パスワード（リクエストボディ）
     * @return アクセストークン・有効期限・トークン種別・リフレッシュトークン
     */
    @PostMapping("/token")
    public ResponseEntity<TokenResponse> token(@Valid @RequestBody TokenRequest tokenRequest) {
        TokenResponse tokenResponse = authService.getToken(tokenRequest);
        return ResponseEntity.ok(tokenResponse);
    }

    /**
     * リフレッシュトークンによるトークン更新エンドポイント
     *
     * <p>リフレッシュトークンを使って新しいアクセストークンを取得します。</p>
     *
     * @param refreshTokenRequest リフレッシュトークン（リクエストボディ）
     * @return アクセストークン・有効期限・トークン種別・リフレッシュトークン
     */
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        TokenResponse tokenResponse = authService.refreshToken(refreshTokenRequest);
        return ResponseEntity.ok(tokenResponse);
    }
}
