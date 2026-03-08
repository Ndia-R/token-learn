package com.example.token_learn.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Value("${spring.security.oauth2.client.provider.idp.issuer-uri}")
    private String issuerUri;

    @Bean
    public OpenAPI customOpenAPI() {
        String logoutUrl = issuerUri + "/protocol/openid-connect/logout";

        return new OpenAPI()
            .info(
                new Info()
                    .title("Token Learn API")
                    .description(
                        "ROPCフローでアクセストークンを取得する学習用API\n\n" +
                            "---\n" +
                            "## 認証プロバイダー\n" +
                            "### ユーザーの新規登録\n" +
                            "1. **[新規登録用ページ](https://localhost/bff/auth/signup)** からユーザーを新規登録（認証プロバイダーにユーザーを追加）\n" +
                            "2. 新規登録した場合、認証プロバイダーに認証済みの情報が残るので、**[認証プロバイダーのログアウト画面](" + logoutUrl + ")** でログアウトしておく\n" +
                            "---\n"
                    )
                    .version("1.0.0")
            );
    }
}
