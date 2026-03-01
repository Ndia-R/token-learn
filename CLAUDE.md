# Token Learn プロジェクト - Claude Code 作業ガイド

## 重要：作業ルール

**基本的なやりとりは日本語でおこなってください。**

**コード修正前の確認必須**
- ファイルの修正・変更を行う前に、必ずユーザーに修正内容を提示して許可を取る
- 勝手にコードを変更してはいけない
- 修正案を説明し、ユーザーの承認を得てから実行する

## プロジェクト概要

認証プロバイダー（Keycloak等）から **ROPCフロー（Resource Owner Password Credentials）** でアクセストークンを取得する **学習用** Spring Boot アプリケーションです。

### 技術スタック
- **言語**: Java 21
- **フレームワーク**: Spring Boot 4.0.3
- **認証**: Spring Security OAuth2 Client（ClientRegistrationRepository経由でトークンエンドポイントを取得）
- **HTTPクライアント**: Spring WebFlux（WebClient）
- **API仕様**: springdoc-openapi 3.0.1（Swagger UI）
- **ビルド**: Gradle

### ROPCフローの概要
```
クライアント（ブラウザ / Swagger UI）
    ↓ username + password
  BFF (このアプリ: /auth/token)
    ↓ grant_type=password + client_id + client_secret
  認証プロバイダー（Keycloak等）のトークンエンドポイント
    ↓ access_token + expires_in + token_type
  クライアントへレスポンス
```

## プロジェクト構成

```
src/main/java/com/example/token_learn/
├── TokenLearnApplication.java      # メインクラス
├── config/
│   ├── SecurityConfig.java         # Spring Security設定 + OpenAPI Bean
│   └── WebClientConfig.java        # WebClient設定（タイムアウト30秒）
├── controller/
│   └── AuthController.java         # POST /auth/token, POST /auth/refresh
├── service/
│   └── AuthService.java            # ROPCフロー・リフレッシュトークンフロー実装
├── dto/
│   ├── TokenRequest.java           # username + password
│   ├── TokenResponse.java          # access_token + expires_in + token_type + refresh_token
│   ├── RefreshTokenRequest.java    # refresh_token
│   └── ErrorResponse.java          # 統一エラーレスポンス
└── exception/
    └── GlobalExceptionHandler.java # 統一例外ハンドラ
```

## エンドポイント

| メソッド | パス | 説明 |
|---------|------|------|
| `POST` | `/auth/token` | リクエストボディ（JSON）でトークン取得（ROPC） |
| `POST` | `/auth/refresh` | リフレッシュトークンで新しいアクセストークンを取得 |
| `GET` | `/actuator/health` | ヘルスチェック |
| `GET` | `/swagger-ui.html` | Swagger UI |

## 設計原則

1. **必要最小限の構成**: すべてのクラスとメソッドが実際に使用されている
2. **ステートレス**: セッションなし（REST API）
3. **単一責任**: Controller/Service の明確な分離
4. **統一されたエラーハンドリング**: GlobalExceptionHandler で一貫したエラーレスポンス

## コーディングスタイル

### 基本方針
- **型安全**: 具体的なDTOクラスを使用
- **単一責任**: Controller/Service の明確な分離
- **必要最小限**: 未使用のクラス・メソッドは作らない
- **アノテーション活用**: `@NonNull` で明示的なnull制約

### エラーハンドリング

| 例外 | HTTPステータス | エラーコード |
|------|--------------|-------------|
| `WebClientResponseException`（4xx） | 400 | `IDP_CLIENT_ERROR` |
| `WebClientResponseException`（5xx） | 503 | `IDP_SERVER_ERROR` |
| `WebClientException` | 503 | `IDP_CONNECTION_ERROR` |
| `MethodArgumentNotValidException` | 400 | `VALIDATION_FAILED` |
| `Exception` | 500 | `INTERNAL_SERVER_ERROR` |

#### 統一エラーレスポンス

```json
{
  "error": "IDP_CLIENT_ERROR",
  "message": "認証サーバーとの通信でクライアントエラーが発生しました",
  "status": 400,
  "path": "/auth/token",
  "timestamp": "2025-10-12 14:30:45"
}
```

## 重要な技術的注意点

### セキュリティ設定
- **CSRF**: 無効（REST APIのため不要）
- **セッション**: STATELESS（セッション作成なし）
- **許可パス**: `/auth/token`、`/auth/refresh`、`/actuator/health`、`/swagger-ui/**`、`/v3/api-docs/**` のみ
- **その他**: すべて拒否（`denyAll()`）

### ClientRegistrationRepository の使い方
`spring-boot-starter-oauth2-client` が `application.yaml` の OAuth2 設定を読み込み、`ClientRegistrationRepository` を自動生成します。`AuthService` はこれからトークンエンドポイントURL・client_id・client_secret を取得し、ROPCリクエストを組み立てます。

### 環境変数
| 変数名 | 説明 |
|--------|------|
| `IDP_CLIENT_ID` | IdPのクライアントID |
| `IDP_CLIENT_SECRET` | IdPのクライアントシークレット |
| `IDP_ISSUER_URI` | IdPのIssuer URI（OIDCディスカバリ用） |
| `IDP_REDIRECT_URI` | OAuth2リダイレクトURI（ROPC自体は不使用だがSpringの設定に必要） |
| `LOG_LEVEL` | ルートログレベル（デフォルト: INFO） |
| `LOG_LEVEL_SECURITY` | Spring Securityのログレベル（デフォルト: INFO） |

## 開発コマンド

### ビルド・テスト・実行
```bash
# ビルド
./gradlew build

# テスト実行
./gradlew test

# アプリケーション実行
./gradlew bootRun
```

### Docker Compose
```bash
# 環境起動
docker compose up -d

# ログ確認
docker compose logs -f token-learn

# 環境停止
docker compose down

# 環境クリーンアップ（ボリュームも削除）
docker compose down -v
```

### Swagger UI
アプリ起動後、`http://localhost:8888/swagger-ui.html` でエンドポイントを確認・実行できます。
