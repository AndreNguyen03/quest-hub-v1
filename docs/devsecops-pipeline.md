# QuestHub — DevSecOps Pipeline

Security shift-left — bảo mật ở mọi stage của CI/CD, không phải chỉ sau khi deploy

---

**Shift-Left Security:** Thay vì kiểm tra bảo mật sau khi deploy, tích hợp vào ngay từ khi commit code. Phát hiện sớm = fix rẻ hơn. Mỗi stage có security gate — vi phạm nghiêm trọng block build, còn lại warn và track.

---

## Pipeline — 7 stages · GitHub Actions

### Stage 1 — Pre-flight: Secret & License Scan

**Trigger:** every push & PR

**Jobs:**
- Secret Scan — Gitleaks
- License Check — FOSSA / license-checker

**Gates:**
- BLOCK — hardcoded secret found (API key, password, JWT secret)
- WARN — GPL license trong commercial context

---

### Stage 2 — SAST + SCA: Code & Dependency Analysis

**Trigger:** parallel với stage 1

**Jobs:**
- SAST — SonarCloud (free for OSS)
- Dependency CVE — OWASP Dependency-Check
- Auto-update deps — Dependabot (GitHub native)

**Gates:**
- BLOCK — CVSS ≥ 9.0 (Critical) trong dependency
- BLOCK — SonarCloud Quality Gate fail (coverage < 70%, blocker issues)
- WARN — CVSS 7.0–8.9 (High)

---

### Stage 3 — Build: Test & Docker Image

**Trigger:** sau khi stage 1 & 2 pass

**Jobs:**
- Unit + Integration Tests — JUnit 5 · Testcontainers
- Docker Build — Multi-stage Dockerfile
- SBOM Generate — Syft (Software Bill of Materials)

**Gates:**
- BLOCK — any test failure
- PASS — image tagged với git SHA (immutable)

---

### Stage 4 — Image Scan: Container Vulnerability

**Trigger:** sau khi Docker build xong

**Jobs:**
- Image CVE Scan — Trivy (OS packages + app deps)
- Dockerfile Lint — Hadolint

**Gates:**
- BLOCK — CRITICAL vuln trong base image (update base image)
- WARN — HIGH vuln → create GitHub Issue tự động
- PASS — push image to GHCR với digest SHA

---

### Stage 5 — Deploy Staging: IaC Scan + DAST

**Trigger:** merge to main · staging env

**Jobs:**
- K8s Manifest Scan — Trivy IaC / Checkov
- DAST — API Scan — OWASP ZAP (Baseline Scan)
- K8s Hardening — Kubesec

**Gates:**
- BLOCK — container running as root, privileged mode, hostNetwork=true
- BLOCK — ZAP phát hiện OWASP Top 10 (SQLi, XSS, IDOR)
- WARN — missing resource limits, no readiness probe

> **Manual approval required** before proceeding to Stage 6

---

### Stage 6 — Deploy Production: GitOps via ArgoCD

**Trigger:** manual approval → ArgoCD sync

**Jobs:**
- Image Tag Update — Update Helm values.yaml với SHA digest
- GitOps Deploy — ArgoCD auto-sync từ Git
- Image Signing — Cosign (Sigstore) — verify provenance

**Gates:**
- BLOCK — image digest không khớp (supply chain attack prevention)
- PASS — ArgoCD health check green → rollout complete

---

### Stage 7 — Runtime Security: Continuous Monitoring

**Trigger:** always on · production

**Jobs:**
- Behavioral Anomaly — Falco (K8s runtime security)
- Security Dashboard — Grafana + Falco alerts
- Vuln Re-scan — Trivy scheduled weekly

**Alerts:**
- ALERT — shell spawned inside container, unexpected outbound connection
- ALERT — new CVE xuất hiện cho image đang chạy (weekly scan)

---

## Tools — tất cả free / open source

| Tool | Category | Tích hợp | Phát hiện |
|------|----------|----------|-----------|
| Gitleaks (free) | Secret | GitHub Actions pre-commit hook | API keys, JWT secrets, passwords, private keys hardcoded trong code |
| SonarCloud (free for OSS) | SAST | GitHub Actions + PR decoration | SQL injection, XSS, insecure deserialization, code smells, test coverage |
| OWASP Dependency-Check (free) | SCA | Maven plugin + GitHub Actions | CVE trong Maven dependencies (NVD database) |
| Dependabot (free) | SCA | GitHub native (enable trong Settings) | Auto-creates PRs khi có dependency update / security fix |
| Trivy (free) | Image + IaC | GitHub Actions, pre-push hook | CVE trong OS packages + language libs trong Docker image; misconfig trong K8s YAML / Helm |
| Hadolint (free) | IaC | GitHub Actions | Dockerfile bad practices: no COPY --chown, running as root, no health check, latest tag |
| OWASP ZAP (free) | DAST | GitHub Actions (Baseline Scan mode) | OWASP Top 10 trên running app: SQLi, XSS, CSRF, insecure headers, open redirects |
| Kubesec (free) | IaC | GitHub Actions | K8s security score: privileged containers, host mounts, missing securityContext |
| Cosign / Sigstore (free) | Image | GitHub Actions post-build | Sign image digest → verify không ai tamper image trước khi deploy (supply chain) |
| Falco (free) | Runtime | K8s DaemonSet | Anomalous behavior trong container lúc runtime: unexpected shell, file write to /etc, privilege escalation |

---

## GitHub Actions — Workflow structure

**File:** `.github/workflows/devsecops.yml`

```yaml
# Chạy trên mọi push và PR
on: [push, pull_request]

jobs:
  secret-scan:            # Stage 1 — nhanh nhất, chạy đầu tiên
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: gitleaks/gitleaks-action@v2

  sast-and-sca:           # Stage 2 — parallel với secret-scan
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: OWASP Dependency-Check
        run: mvn org.owasp:dependency-check-maven:check -DfailBuildOnCVSS=9
      - name: SonarCloud Scan
        uses: SonarSource/sonarcloud-github-action@master

  build-and-scan:          # Stage 3+4 — chỉ chạy sau khi 1&2 pass
    needs: [secret-scan, sast-and-sca]
    runs-on: ubuntu-latest
    steps:
      - name: Run tests
        run: mvn test
      - name: Build Docker image
        run: docker build -t questhub:${{ github.sha }} .
      - name: Trivy image scan
        uses: aquasecurity/trivy-action@master
        with:
          image-ref: questhub:${{ github.sha }}
          severity: CRITICAL
          exit-code: '1'       # block nếu tìm thấy CRITICAL
      - name: Sign image (Cosign)
        run: cosign sign --key cosign.key questhub:${{ github.sha }}
      - name: Push to GHCR
        run: docker push ghcr.io/questhub:${{ github.sha }}

  staging-security:       # Stage 5 — deploy + DAST (main branch only)
    needs: [build-and-scan]
    if: github.ref == 'refs/heads/main'
    steps:
      - name: Trivy IaC scan (K8s manifests)
        uses: aquasecurity/trivy-action@master
        with: { scan-type: 'config', scan-ref: './k8s' }
      - name: Deploy to staging
        run: kubectl apply -f k8s/staging/
      - name: OWASP ZAP Baseline Scan
        uses: zaproxy/action-baseline@v0.12.0
        with:
          target: https://staging.questhub.app
          fail_action: true
```

---

## Core Concepts

### Shift-Left

Phát hiện security issue sớm nhất có thể. Fix ở commit rẻ hơn 100× so với fix sau khi đã deploy. Pre-commit hook nhanh hơn CI pipeline.

### Supply Chain Security

Cosign sign image sau khi build. Khi deploy, K8s verify signature — đảm bảo image chạy production là đúng image đã pass CI, không ai tamper giữa chừng.

### SBOM

Software Bill of Materials — danh sách đầy đủ mọi component trong image. Khi CVE mới xuất hiện, dùng SBOM để biết ngay image nào bị ảnh hưởng mà không cần rescan.

### Immutable Images

Image được tag bằng git SHA (không dùng `latest`). Mọi deployment đều reproducible — biết chính xác commit nào đang chạy trên production.

### Runtime Security

Falco monitor syscalls trong container. Nếu ai exploit app và spawn shell bên trong container — Falco alert ngay. Bảo vệ lớp cuối khi mọi thứ khác đã fail.

### Security Gates

CRITICAL block build ngay. HIGH tạo GitHub Issue tự động. Medium/Low log vào dashboard để review định kỳ. Không để alert fatigue làm team ignore warning.
