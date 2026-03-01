# Token Learn

![Java](https://img.shields.io/badge/Java-21-blue) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.3-brightgreen)

認証プロバイダー（Keycloak等）から **ROPCフロー（Resource Owner Password Credentials）** でアクセストークンを取得する **学習用** Spring Boot アプリケーションです。

> **注意**: ROPCフローはセキュリティ上の理由から本番環境での使用は推奨されません。このアプリはOAuth2/OIDCの学習を目的としています。

---

## 機能

- **ROPCフロー**: ユーザー名・パスワードでアクセストークンを取得
- **リフレッシュトークンフロー**: リフレッシュトークンで新しいアクセストークンを取得
- **Swagger UI**: ブラウザからAPIを確認・実行
- **統一エラーレスポンス**: エラー情報を一貫したJSON形式で返却

---

## ROPCフロー概要

```
クライアント（ブラウザ / Swagger UI）
    │
    │  POST /auth/token
    │  { "username": "...", "password": "..." }
    ▼
  Token Learn (このアプリ: BFF)
    │
    │  POST {tokenEndpoint}
    │  grant_type=password & client_id & client_secret & username & password
    ▼
  認証プロバイダー（Keycloak等）
    │
    │  { "access_token": "...", "expires_in": 300, "refresh_token": "..." }
    ▼
  クライアントへレスポンス
```

---

## 技術スタック

| 技術 | バージョン / 詳細 |
|------|-----------------|
| Java | 21 |
| Spring Boot | 4.0.3 |
| Spring Security | OAuth2 Client（ClientRegistrationRepository） |
| HTTP クライアント | Spring WebFlux（WebClient + Reactor Netty） |
| API ドキュメント | springdoc-openapi 3.0.1（Swagger UI） |
| バリデーション | Jakarta Validation |
| ビルドツール | Gradle 9.3.1 |
| コンテナ | Docker（マルチステージビルド） |

---

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

---

## 前提条件

- **Java 21** または **Docker**
- OIDC に対応した IdP（Keycloak等）へのアクセス

---

## セットアップ

### 1. 環境変数の設定

`.env.example` をコピーして `.env` を作成し、各値を設定します。

```bash
cp .env.example .env
```

`.env` を編集します：

```properties
# Identity Provider (IdP) Configuration
IDP_CLIENT_ID=your-client-id
IDP_CLIENT_SECRET=your-client-secret
IDP_ISSUER_URI=https://keycloak.example.com/auth/realms/your-realm

# Logging Configuration（任意）
LOG_LEVEL=INFO
LOG_LEVEL_SECURITY=INFO
```

**Keycloak を使用する場合の `IDP_ISSUER_URI` 例:**

```
https://{keycloakホスト}/auth/realms/{レルム名}
```

### 2. 起動方法

#### Docker Compose で起動（推奨）

```bash
# コンテナを起動
docker compose up -d

# コンテナ内でアプリを起動
docker compose exec token-learn ./gradlew bootRun
```

#### ローカルで起動

```bash
# 環境変数をエクスポートしてから実行
export IDP_CLIENT_ID=your-client-id
export IDP_CLIENT_SECRET=your-client-secret
export IDP_ISSUER_URI=https://keycloak.example.com/auth/realms/your-realm

./gradlew bootRun
```

起動後、 http://localhost:8888/swagger-ui.html （Docker Compose の場合）または http://localhost:8080/swagger-ui.html （ローカルの場合）にアクセスします。

---

## エンドポイント

| メソッド | パス | 説明 |
|---------|------|------|
| `POST` | `/auth/token` | ROPCフローでアクセストークンを取得 |
| `POST` | `/auth/refresh` | リフレッシュトークンで新しいアクセストークンを取得 |
| `GET` | `/actuator/health` | ヘルスチェック |
| `GET` | `/swagger-ui.html` | Swagger UI |

### POST `/auth/token` - トークン取得

**リクエスト:**

```bash
curl -s -X POST http://localhost:8888/auth/token \
  -H "Content-Type: application/json" \
  -d '{"username": "user@example.com", "password": "password"}' | jq .
```

**レスポンス（成功）:**

```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5...",
  "expires_in": 300,
  "token_type": "Bearer",
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5..."
}
```

### POST `/auth/refresh` - トークン更新

**リクエスト:**

```bash
curl -s -X POST http://localhost:8888/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refresh_token": "eyJhbGciOiJIUzI1NiIsInR5..."}' | jq .
```

**レスポンス（成功）:** `/auth/token` と同じ形式

---

## Swagger UI

アプリ起動後、以下のURLでAPIを確認・実行できます：

- Docker Compose 使用時: http://localhost:8888/swagger-ui.html
- ローカル実行時: http://localhost:8080/swagger-ui.html

---

## エラーハンドリング

すべてのエラーは以下の統一形式で返されます：

```json
{
  "error": "IDP_CLIENT_ERROR",
  "message": "認証サーバーとの通信でクライアントエラーが発生しました",
  "status": 400,
  "path": "/auth/token",
  "timestamp": "2025-10-12 14:30:45"
}
```

| エラーコード | HTTP ステータス | 説明 |
|------------|---------------|------|
| `IDP_UNAUTHORIZED` | 401 | ユーザー名またはパスワードが正しくない |
| `IDP_CLIENT_ERROR` | 400 | IdP へのリクエストエラー（4xx） |
| `IDP_SERVER_ERROR` | 503 | IdP 側のサーバーエラー（5xx） |
| `IDP_CONNECTION_ERROR` | 503 | IdP への接続失敗 |
| `VALIDATION_FAILED` | 400 | リクエストのバリデーションエラー |
| `INTERNAL_SERVER_ERROR` | 500 | 予期しないエラー |

---

## 環境変数

| 変数名 | 説明 | 必須 | デフォルト |
|--------|------|:----:|-----------|
| `IDP_CLIENT_ID` | IdP のクライアントID | ✅ | - |
| `IDP_CLIENT_SECRET` | IdP のクライアントシークレット | ✅ | - |
| `IDP_ISSUER_URI` | IdP の Issuer URI（OIDC ディスカバリ用） | ✅ | - |
| `LOG_LEVEL` | ルートログレベル | - | `INFO` |
| `LOG_LEVEL_SECURITY` | Spring Security のログレベル | - | `INFO` |

---

## 開発コマンド

```bash
# ビルド
./gradlew build

# テスト実行
./gradlew test

# アプリケーション実行
./gradlew bootRun

# Docker Compose 操作
docker compose up -d          # 起動
docker compose logs -f token-learn  # ログ確認
docker compose down           # 停止
docker compose down -v        # 停止 + ボリューム削除
```

---

## 本番向け Docker イメージのビルド

```bash
# production ステージのみビルド（軽量 Alpine イメージ）
docker build --target production -t token-learn:latest .

# 実行例
docker run -p 8080:8080 \
  -e IDP_CLIENT_ID=your-client-id \
  -e IDP_CLIENT_SECRET=your-client-secret \
  -e IDP_ISSUER_URI=https://keycloak.example.com/auth/realms/your-realm \
  token-learn:latest
```
